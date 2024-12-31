package com.mszlu.rpc.netty;

import com.mszlu.rpc.message.MsRequest;

import java.util.concurrent.ExecutionException;

public interface MsClient {

    /**
     * 发送请求，并接收数据
     * @param msRequest
     * @param host
     * @param port
     * @return
     */
    Object sendRequest(MsRequest msRequest,String host,int port);
}