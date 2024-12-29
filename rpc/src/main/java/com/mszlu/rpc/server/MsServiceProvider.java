package com.mszlu.rpc.server;

import com.mszlu.rpc.annontation.MsService;
import com.mszlu.rpc.factory.SingletonFactory;
import com.mszlu.rpc.netty.NettyServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class MsServiceProvider {


    private final Map<String, Object> serviceMap;

    public MsServiceProvider(){
        //发布的服务 都在这里
        serviceMap = new ConcurrentHashMap<>();
    }

    public void publishService(MsService msService,Object service) {
        registerService(msService,service);
        //检测到有服务发布的注解，启动NettyServer
        NettyServer nettyServer = SingletonFactory.getInstance(NettyServer.class);
        nettyServer.setMsServiceProvider(this);
        if (!nettyServer.isRunning()){
            nettyServer.run();
        }
    }

    private void registerService(MsService msService, Object service) {
        //service要进行注册, 先创建一个map进行存储
        String serviceName = service.getClass().getInterfaces()[0].getCanonicalName()+msService.version();
        serviceMap.put(serviceName,service);
        log.info("发现服务{}并注册",serviceName);
    }

    public Object getService(String serviceName) {
        return serviceMap.get(serviceName);
    }

}
