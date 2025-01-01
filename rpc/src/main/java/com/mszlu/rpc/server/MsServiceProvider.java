package com.mszlu.rpc.server;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.mszlu.rpc.annontation.MsService;
import com.mszlu.rpc.factory.ServerFactory;
import com.mszlu.rpc.factory.SingletonFactory;
import com.mszlu.rpc.register.nacos.NacosTemplate;
import com.mszlu.rpc.remoting.MsServer;
import com.mszlu.rpc.remoting.netty.NettyServer;
import com.mszlu.rpc.remoting.socket.SocketRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class MsServiceProvider {

    private final Map<String, Object> serviceMap;
    private NacosTemplate nacosTemplate;

    public MsServiceProvider(){
        //发布的服务 都在这里
        serviceMap = new ConcurrentHashMap<>();
        nacosTemplate = SingletonFactory.getInstance(NacosTemplate.class);
    }

    public void publishService(MsService msService,Object service) {
        registerService(msService, service);
        //检测到有服务发布的注解，启动Server
//        MsServer server = SingletonFactory.getInstance(NettyServer.class);
        MsServer server = ServerFactory.getServer();
        server.setMsServiceProvider(this);
        if (!server.isRunning()){
            server.run();
        }
    }

    private void registerService(MsService msService, Object service) {
        //service要进行注册, 先创建一个map进行存储
        String serviceName = service.getClass().getInterfaces()[0].getCanonicalName() + msService.version();
        serviceMap.put(serviceName,service);
        //将服务注册到nacos上
        try {
            Instance instance = new Instance();
            instance.setPort(NettyServer.PORT);
            instance.setIp(InetAddress.getLocalHost().getHostAddress());
            instance.setClusterName(NettyServer.groupName);
            instance.setServiceName(serviceName);
//            nacosTemplate.registerServer(instance);
            nacosTemplate.registerServer(NettyServer.groupName, instance);
        } catch (Exception e) {
            log.error("nacos 注册服务失败:",e);
        }
        log.info("发现服务{}并注册",serviceName);
    }

    public Object getService(String serviceName) {
        return serviceMap.get(serviceName);
    }

}
