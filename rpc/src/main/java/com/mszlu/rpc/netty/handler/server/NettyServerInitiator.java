package com.mszlu.rpc.netty.handler.server;

import com.mszlu.rpc.netty.codec.MsRpcDecoder;
import com.mszlu.rpc.netty.codec.MsRpcEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;

public class NettyServerInitiator extends ChannelInitializer<SocketChannel> {

    private EventExecutorGroup eventExecutors;

    public NettyServerInitiator(EventExecutorGroup eventExecutors) {
        this.eventExecutors = eventExecutors;
    }

    protected void initChannel(SocketChannel ch) throws Exception {
        //解码器
        ch.pipeline ().addLast ( "decoder",new MsRpcDecoder() );
        //编码器
        ch.pipeline ().addLast ( "encoder",new MsRpcEncoder());
        //消息处理器，线程池处理
        ch.pipeline ().addLast ( eventExecutors,"handler",new MsNettyServerHandler() );
    }
}
