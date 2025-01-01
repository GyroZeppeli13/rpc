package com.mszlu.rpc.remoting;

import com.mszlu.rpc.config.MsRpcConfig;
import com.mszlu.rpc.message.MsRequest;

public interface MsClient {

    /**
     * 发送请求，并接收数据
     * @param msRequest
     * @return
     */
    Object sendRequest(MsRequest msRequest);

    void setMsRpcConfig(MsRpcConfig msRpcConfig);
}