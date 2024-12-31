package com.mszlu.rpc.remoting.netty.client;

import com.mszlu.rpc.enums.MessageTypeEnum;
import com.mszlu.rpc.factory.SingletonFactory;
import com.mszlu.rpc.message.MsMessage;
import com.mszlu.rpc.message.MsResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

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
}