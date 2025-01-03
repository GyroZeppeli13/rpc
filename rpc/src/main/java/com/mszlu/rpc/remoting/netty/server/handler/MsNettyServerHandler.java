package com.mszlu.rpc.remoting.netty.server.handler;

import com.mszlu.rpc.constants.MsRpcConstants;
import com.mszlu.rpc.enums.MessageTypeEnum;
import com.mszlu.rpc.exception.MsRpcException;
import com.mszlu.rpc.factory.SingletonFactory;
import com.mszlu.rpc.message.MsMessage;
import com.mszlu.rpc.message.MsRequest;
import com.mszlu.rpc.message.MsResponse;
import com.mszlu.rpc.remoting.handler.MsRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MsNettyServerHandler extends ChannelInboundHandlerAdapter {
    private MsRequestHandler requestHandler;

    public MsNettyServerHandler(){
        this.requestHandler = SingletonFactory.getInstance(MsRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            //这里 接收到 请求的信息，然后根据请求，找到对应的服务提供者，调用，获取结果，然后返回
            //消费方 会启动一个 客户端，用户接收返回的数据
            if (msg instanceof MsMessage){
                MsMessage msMessage = (MsMessage) msg;
                byte messageType = msMessage.getMessageType();
                if (messageType == MessageTypeEnum.HEARTBEAT_PING.getCode()){
                    //心跳包 返回PONG
                    msMessage.setMessageType(MessageTypeEnum.HEARTBEAT_PONG.getCode());
                    msMessage.setData(MsRpcConstants.PONG);
                }
                else if (messageType== MessageTypeEnum.REQUEST.getCode()){
                    //客户端请求
                    MsRequest msRequest = (MsRequest) msMessage.getData();
                    Object handler = requestHandler.handler(msRequest);
                    msMessage.setMessageType(MessageTypeEnum.RESPONSE.getCode());
                    if (ctx.channel().isActive() && ctx.channel().isWritable()){
                        MsResponse<Object> msResponse = MsResponse.success(handler, msRequest.getRequestId());
                        msMessage.setData(msResponse);
                    }else{
                        MsResponse<Object> msResponse = MsResponse.fail("net fail");
                        msMessage.setData(msResponse);
                    }
                    log.info("服务端收到数据，并处理完成{}:",msMessage);
                    //写完数据 并关闭通道
                    ctx.writeAndFlush(msMessage).addListener(ChannelFutureListener.CLOSE);
                }
            }
        }catch (Exception e){
            throw new MsRpcException("数据读取异常",e);
        }finally {
            //释放 以防内存泄露
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 如果10s没有读请求，关闭连接，以免连接过多，每个都回复会造成网络压力
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("客户端10s 未发送读请求，判定失效，进行关闭");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        //出现异常 关闭连接
        ctx.close();
    }
}