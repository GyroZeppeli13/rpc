package com.mszlu.rpc.factory;

import com.mszlu.rpc.enums.RpcType;
import com.mszlu.rpc.remoting.MsClient;

import java.io.IOException;
import java.util.Properties;

public class ClientFactory {

    private static RpcType rpcType;

    static {
        // 读取配置文件
        Properties properties = new Properties();
        try {
            properties.load(ClientFactory.class.getResourceAsStream("/application.properties"));
            String rpcTypeStr = properties.getProperty("rpc.type", "netty"); // 默认值为 netty
            rpcType = RpcType.fromString(rpcTypeStr);
        } catch (IOException e) {
            e.printStackTrace();
            rpcType = RpcType.NETTY; // 默认选择 netty
        }
    }

    public static MsClient getClient() {
        // 获取客户端实例
        return (MsClient) SingletonFactory.getInstance(rpcType.getClientClass());
    }
}
