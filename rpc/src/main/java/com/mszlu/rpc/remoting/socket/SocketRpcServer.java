package com.mszlu.rpc.remoting.socket;

import com.mszlu.rpc.remoting.MsServer;
import com.mszlu.rpc.server.MsServiceProvider;
import com.mszlu.rpc.utils.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

@Slf4j
public class SocketRpcServer implements MsServer {

    private final ExecutorService threadPool;
    private MsServiceProvider msServiceProvider;

    private boolean isRunning;

    public SocketRpcServer() {
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), msServiceProvider.getMsRpcConfig().getProviderPort()));
            isRunning = true;
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run() {
                    stopSocketServer();
                }
            });
            Socket socket;
            while (true) {
                if((socket = server.accept()) != null) {
                    log.info("client connected [{}]", socket.getInetAddress());
                    threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
                }
                else {
                    // 如果没有连接，休眠 200 毫秒
                    Thread.sleep(200);
                }
            }
        }
        catch (IOException | InterruptedException e) {
            log.error("Error in accepting connection or sleeping", e);
        }
    }

    @Override
    public void stop() {
        threadPool.shutdown();
    }

    private void stopSocketServer() {
        if (threadPool != null){
            threadPool.shutdown();
        }
    }

    @Override
    public void setMsServiceProvider(MsServiceProvider msServiceProvider) {
        this.msServiceProvider = msServiceProvider;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}