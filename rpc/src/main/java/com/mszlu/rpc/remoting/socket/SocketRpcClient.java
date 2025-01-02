package com.mszlu.rpc.remoting.socket;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.mszlu.rpc.config.MsRpcConfig;
import com.mszlu.rpc.exception.MsRpcException;
import com.mszlu.rpc.factory.SingletonFactory;
import com.mszlu.rpc.message.MsRequest;
import com.mszlu.rpc.message.MsResponse;
import com.mszlu.rpc.register.nacos.NacosTemplate;
import com.mszlu.rpc.remoting.MsClient;
import com.mszlu.rpc.remoting.netty.NettyServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class SocketRpcClient implements MsClient {

    private NacosTemplate nacosTemplate;
    private MsRpcConfig msRpcConfig;
    private static final Set<String> SERVICES = new CopyOnWriteArraySet<>();

    public SocketRpcClient() {
        this.nacosTemplate = SingletonFactory.getInstance(NacosTemplate.class);
    }

    @Override
    public Object sendRequest(MsRequest rpcRequest) {
        //通过注册中心获取主机和端口
        String serviceName = rpcRequest.getInterfaceName() + rpcRequest.getVersion();

        InetSocketAddress inetSocketAddress = null;
        String ipAndPort = null;
        if (!SERVICES.isEmpty()){
            //有缓存的服务提供者服务器，直接获取
            //随机获取一个
            Optional<String> optional = SERVICES.stream().skip(SERVICES.size() - 1).findFirst();
            if (optional.isPresent()){
                ipAndPort = optional.get();
                String[] split = ipAndPort.split(",");
                inetSocketAddress = new InetSocketAddress(split[0],Integer.parseInt(split[1]));
                log.info("走了缓存的服务提供者地址，省去了连接nacos的过程...");
            }
        }
        if (inetSocketAddress == null){
            Instance oneHealthyInstance = null;
            try {
                //根据组 进行获取健康实例，服务提供方和消费方 不在一个组内 无法获取实例
                oneHealthyInstance = nacosTemplate.getOneHealthyInstance(serviceName,msRpcConfig.getNacosGroup());
            } catch (Exception e) {
                throw new MsRpcException("没有获取到可用的服务提供者");
            }
            //从nacos获取实例后，将其缓存起来
            ipAndPort = oneHealthyInstance.getIp()+","+oneHealthyInstance.getPort();
            SERVICES.add(ipAndPort);
            inetSocketAddress = new InetSocketAddress(oneHealthyInstance.getIp(), oneHealthyInstance.getPort());
        }
        //连接
        CompletableFuture<MsResponse<Object>> resultFuture = new CompletableFuture<>();
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // Send data to the server through the output stream
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // Read RpcResponse from the input stream
            resultFuture.complete((MsResponse<Object>) objectInputStream.readObject());
        } catch (IOException e) {
            //与选择服务端实例IO失败 从缓存中去除
            if(ipAndPort != null) {
                SERVICES.remove(ipAndPort);
                log.info("删除provider服务缓存成功...");
            }
            throw new MsRpcException("连接服务器失败");
        } catch (ClassNotFoundException e) {
            throw new MsRpcException("ClassNotFoundException", e);
        }
        return resultFuture;
    }

    @Override
    public void setMsRpcConfig(MsRpcConfig msRpcConfig) {
        this.msRpcConfig = msRpcConfig;
    }
}