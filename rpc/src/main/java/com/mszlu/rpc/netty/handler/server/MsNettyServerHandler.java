package com.mszlu.rpc.netty.handler.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MsNettyServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //这里 接收到 请求的信息，然后根据请求，找到对应的服务提供者，调用，获取结果，然后返回
        //消费方 会启动一个 客户端，用户接收返回的数据

    }

}