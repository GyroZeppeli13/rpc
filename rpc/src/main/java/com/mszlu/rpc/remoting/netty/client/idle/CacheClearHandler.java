package com.mszlu.rpc.remoting.netty.client.idle;

import java.net.InetSocketAddress;

public interface CacheClearHandler {

    /**
     * 清理缓存
     */
    void clear(InetSocketAddress inetSocketAddress);
}