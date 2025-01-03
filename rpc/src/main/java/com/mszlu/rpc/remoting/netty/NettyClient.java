package com.mszlu.rpc.remoting.netty;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.mszlu.rpc.config.MsRpcConfig;
import com.mszlu.rpc.enums.CompressTypeEnum;
import com.mszlu.rpc.enums.MessageTypeEnum;
import com.mszlu.rpc.enums.SerializationTypeEnum;
import com.mszlu.rpc.exception.MsRpcException;
import com.mszlu.rpc.factory.SingletonFactory;
import com.mszlu.rpc.message.MsMessage;
import com.mszlu.rpc.message.MsRequest;
import com.mszlu.rpc.message.MsResponse;
import com.mszlu.rpc.register.nacos.NacosTemplate;
import com.mszlu.rpc.remoting.MsClient;
import com.mszlu.rpc.remoting.netty.client.MsNettyClientHandler;
import com.mszlu.rpc.remoting.netty.client.UnprocessedRequests;
import com.mszlu.rpc.remoting.netty.client.idle.ConnectionWatchdog;
import com.mszlu.rpc.remoting.netty.codec.MsRpcDecoder;
import com.mszlu.rpc.remoting.netty.codec.MsRpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClient implements MsClient {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private UnprocessedRequests unprocessedRequests;
    private NacosTemplate nacosTemplate;
    private MsRpcConfig msRpcConfig;
    private static final Set<String> SERVICES = new CopyOnWriteArraySet<>();
    protected final HashedWheelTimer timer = new HashedWheelTimer();

    public NettyClient(){
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nacosTemplate = SingletonFactory.getInstance(NacosTemplate.class);
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //超时时间设置
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
//                .handler(new ChannelInitializer<SocketChannel>() {
//                    @Override
//                    protected void initChannel(SocketChannel ch) throws Exception {
//                        //3s 没收到写请求，进行心跳检测
//                        ch.pipeline().addLast(new IdleStateHandler(0, 3, 0, TimeUnit.SECONDS));
//                        ch.pipeline ().addLast ( "decoder",new MsRpcDecoder() );
//                        ch.pipeline ().addLast ( "encoder",new MsRpcEncoder());
//                        ch.pipeline ().addLast ( "handler",new MsNettyClientHandler() );
//                    }
//                })
        ;
    }

    public Object sendRequest(MsRequest msRequest) {
        //发送数据
//        //1. 连接netty服务，获取channel
//        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        //通过注册中心获取主机和端口
        String serviceName = msRequest.getInterfaceName() + msRequest.getVersion();
        InetSocketAddress inetSocketAddress = null;
        String ipAndPort = null;
        if (!SERVICES.isEmpty()){
            //有缓存的服务提供者服务器，直接获取
            //随机获取一个
            Optional<String> optional = SERVICES.stream().skip(SERVICES.size() - 1).findFirst();
            if (optional.isPresent()){
                ipAndPort = optional.get();
                String[] split = ipAndPort.split(",");
                inetSocketAddress = new InetSocketAddress(split[0], Integer.parseInt(split[1]));
                log.info("走了缓存的服务提供者地址，省去了连接nacos的过程...");
            }
        }
        if (inetSocketAddress == null){
            Instance oneHealthyInstance = null;
            try {
                //根据组 进行获取健康实例，服务提供方和消费方 不在一个组内 无法获取实例
                oneHealthyInstance = nacosTemplate.getOneHealthyInstance(serviceName, msRpcConfig.getNacosGroup());
            } catch (Exception e) {
                throw new MsRpcException("没有获取到可用的服务提供者");
            }
            //从nacos获取实例后，将其缓存起来
            ipAndPort = oneHealthyInstance.getIp()+","+oneHealthyInstance.getPort();
            SERVICES.add(ipAndPort);
            inetSocketAddress = new InetSocketAddress(oneHealthyInstance.getIp(), oneHealthyInstance.getPort());
        }
        //连接
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        final ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap, timer, inetSocketAddress, completableFuture, true) {
            @Override
            public void clear(InetSocketAddress ia) {
                SERVICES.remove(ia.getHostName()+","+ia.getPort());
                log.info("链路检测狗 触发: 删除provider服务缓存成功...");
            }

            public ChannelHandler[] handlers() {
                return new ChannelHandler[] {
                        this,
                        new IdleStateHandler(0, 3, 0, TimeUnit.SECONDS),
                        new MsRpcDecoder(),
                        new MsRpcEncoder(),
                        new MsNettyClientHandler()
                };
            }
        };
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(watchdog.handlers());
            }
        });
        String finalIpAndPort = ipAndPort;
        bootstrap.connect(inetSocketAddress).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()){
                    //代表连接成功，将channel放入任务中
                    completableFuture.complete(future.channel());
                }else {
                    //连接失败 从缓存中去除
                    if(finalIpAndPort != null) {
                        SERVICES.remove(finalIpAndPort);
                        log.info("删除provider服务缓存成功...");
                    }
                    completableFuture.completeExceptionally(future.cause());
                    throw new MsRpcException("连接服务器失败");
                }
            }
        });
        //结果获取的任务
        CompletableFuture<MsResponse<Object>> resultFuture = new CompletableFuture<>();
        try {
            Channel channel = completableFuture.get();

            if (channel.isActive()){
                //将任务 存起来，和请求id对应，便于后续读取到数据后，可以根据请求id，将任务标识完成
                unprocessedRequests.put(msRequest.getRequestId(), resultFuture);
                //构建发送的数据
                MsMessage msMessage = MsMessage.builder()
                        .messageType(MessageTypeEnum.REQUEST.getCode())
                        .codec(SerializationTypeEnum.PROTOSTUFF.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .data(msRequest)
                        .build();
                //请求,并添加监听
                channel.writeAndFlush(msMessage).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()){
                            //任务完成
                            log.info("发送数据成功:{}",msMessage);
                        }else{
                            //发送数据失败
                            future.channel().close();
                            //任务标识为完成 有异常
                            resultFuture.completeExceptionally(future.cause());
                            log.info("发送数据失败:",future.cause());
                        }
                    }
                });
            }
        } catch (Exception e) {
            throw new MsRpcException("获取Channel失败",e);
        }

        return resultFuture;
    }

    @Override
    public void setMsRpcConfig(MsRpcConfig msRpcConfig) {
        this.msRpcConfig = msRpcConfig;
    }
}