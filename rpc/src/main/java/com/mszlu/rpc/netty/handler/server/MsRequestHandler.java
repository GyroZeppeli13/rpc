package com.mszlu.rpc.netty.handler.server;

import com.mszlu.rpc.exception.MsRpcException;
import com.mszlu.rpc.factory.SingletonFactory;
import com.mszlu.rpc.message.MsRequest;
import com.mszlu.rpc.server.MsServiceProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MsRequestHandler {

    private  MsServiceProvider serviceProvider;

    public MsRequestHandler(){
        this.serviceProvider = SingletonFactory.getInstance(MsServiceProvider.class);
    }

    public  Object handler(MsRequest msRequest){
        String interfaceName = msRequest.getInterfaceName();
        String version = msRequest.getVersion();
        String serviceName = interfaceName + version;
        Object service = serviceProvider.getService(serviceName);
        if(service == null) {
            throw new MsRpcException("未找到对应服务提供方");
        }
        try {
            Method method = service.getClass().getMethod(msRequest.getMethodName(), msRequest.getParamTypes());
            Object invoke = method.invoke(service, msRequest.getParameters());
            return invoke;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new MsRpcException("服务调用出现问题:"+e.getMessage(),e);
        }
    }
}