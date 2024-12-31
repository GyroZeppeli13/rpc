package com.mszlu.rpc.enums;

import com.mszlu.rpc.remoting.netty.NettyClient;
import com.mszlu.rpc.remoting.netty.NettyServer;
import com.mszlu.rpc.remoting.socket.SocketRpcClient;
import com.mszlu.rpc.remoting.socket.SocketRpcServer;

public enum RpcType {
    NETTY("netty", NettyClient.class, NettyServer.class),
    SOCKET("socket", SocketRpcClient.class, SocketRpcServer.class);

    private final String type;
    private final Class<?> clientClass;
    private final Class<?> serverClass;

    RpcType(String type, Class<?> clientClass, Class<?> serverClass) {
        this.type = type;
        this.clientClass = clientClass;
        this.serverClass = serverClass;
    }

    public String getType() {
        return type;
    }

    public Class<?> getClientClass() {
        return clientClass;
    }

    public Class<?> getServerClass() {
        return serverClass;
    }

    // 根据配置返回对应的 RpcType
    public static RpcType fromString(String type) {
        for (RpcType rpcType : values()) {
            if (rpcType.getType().equalsIgnoreCase(type)) {
                return rpcType;
            }
        }
        throw new IllegalArgumentException("Unsupported rpc.type: " + type);
    }
}
