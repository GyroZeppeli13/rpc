package com.mszlu.rpc.remoting.netty.client;

import com.mszlu.rpc.constants.MsRpcConstants;
import com.mszlu.rpc.enums.CompressTypeEnum;
import com.mszlu.rpc.enums.MessageTypeEnum;
import com.mszlu.rpc.enums.SerializationTypeEnum;
import com.mszlu.rpc.factory.SingletonFactory;
import com.mszlu.rpc.message.MsMessage;
import com.mszlu.rpc.message.MsResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MsNettyClientHandler extends ChannelInboundHandlerAdapter {

    private  UnprocessedRequests unprocessedRequests;

    public MsNettyClientHandler(){
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof MsMessage){
                MsMessage msMessage = (MsMessage) msg;
                byte messageType = msMessage.getMessageType();
                //读取数据 如果是response的消息类型，拿到数据，标识为完成
                if (messageType == MessageTypeEnum.RESPONSE.getCode()){
                    MsResponse<Object> data = (MsResponse<Object>) msMessage.getData();
                    unprocessedRequests.complete(data);
                }
            }
        }finally {
            //释放ByteBuf 避免内存泄露
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("3s未收到写请求，发起心跳,地址：{}", ctx.channel().remoteAddress());
                MsMessage rpcMessage = new MsMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(MsRpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(MsRpcConstants.PING);
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //代表通道已连接
        //表示channel活着
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //代表连接关闭了
        log.info("服务端连接关闭:{}",ctx.channel().remoteAddress());
        //需要将缓存清除掉

        //标识channel不活着
        ctx.fireChannelInactive();
    }
}