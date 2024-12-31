package com.mszlu.rpc.remoting.socket;

import com.mszlu.rpc.exception.MsRpcException;
import com.mszlu.rpc.message.MsRequest;
import com.mszlu.rpc.message.MsResponse;
import com.mszlu.rpc.remoting.MsClient;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SocketRpcClient implements MsClient {

    public SocketRpcClient() {
    }

    @Override
    public Object sendRequest(MsRequest rpcRequest, String host, int port) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
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
}