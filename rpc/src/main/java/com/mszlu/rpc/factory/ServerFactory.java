package com.mszlu.rpc.factory;

import com.mszlu.rpc.enums.RpcType;
import com.mszlu.rpc.remoting.MsServer;

import java.io.IOException;
import java.util.Properties;

public class ServerFactory {

    private static RpcType rpcType;

    static {
        // 读取配置文件
        Properties properties = new Properties();
        try {
            properties.load(ServerFactory.class.getResourceAsStream("/application.properties"));
            String rpcTypeStr = properties.getProperty("rpc.type", "netty"); // 默认值为 netty
            rpcType = RpcType.fromString(rpcTypeStr);
        } catch (IOException e) {
            e.printStackTrace();
            rpcType = RpcType.NETTY; // 默认选择 netty
        }
    }

    public static MsServer getServer() {
        // 获取服务端实例
        return (MsServer) SingletonFactory.getInstance(rpcType.getServerClass());
    }
}
