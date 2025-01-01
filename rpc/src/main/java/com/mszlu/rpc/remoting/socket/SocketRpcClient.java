package com.mszlu.rpc.remoting.socket;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.mszlu.rpc.config.MsRpcConfig;
import com.mszlu.rpc.exception.MsRpcException;
import com.mszlu.rpc.factory.SingletonFactory;
import com.mszlu.rpc.message.MsRequest;
import com.mszlu.rpc.message.MsResponse;
import com.mszlu.rpc.register.nacos.NacosTemplate;
import com.mszlu.rpc.remoting.MsClient;
import com.mszlu.rpc.remoting.netty.NettyServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SocketRpcClient implements MsClient {

    private NacosTemplate nacosTemplate;
    private MsRpcConfig msRpcConfig;

    public SocketRpcClient() {
        this.nacosTemplate = SingletonFactory.getInstance(NacosTemplate.class);
    }

    @Override
    public Object sendRequest(MsRequest rpcRequest) {
//        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        //通过注册中心获取主机和端口
        String serviceName = rpcRequest.getInterfaceName() + rpcRequest.getVersion();
        Instance oneHealthyInstance = null;
        try {
            oneHealthyInstance = nacosTemplate.getOneHealthyInstance(msRpcConfig.getNacosGroup(), serviceName);
        } catch (Exception e) {
            throw new MsRpcException("没有获取到可用的服务提供者");
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(oneHealthyInstance.getIp(), oneHealthyInstance.getPort());
        CompletableFuture<MsResponse<Object>> resultFuture = new CompletableFuture<>();
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // Send data to the server through the output stream
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // Read RpcResponse from the input stream
            resultFuture.complete((MsResponse<Object>) objectInputStream.readObject());
        } catch (IOException | ClassNotFoundException e) {
            resultFuture.completeExceptionally(e);
            throw new MsRpcException("调用服务失败:", e);
        }
        return resultFuture;
    }

    @Override
    public void setMsRpcConfig(MsRpcConfig msRpcConfig) {
        this.msRpcConfig = msRpcConfig;
    }
}