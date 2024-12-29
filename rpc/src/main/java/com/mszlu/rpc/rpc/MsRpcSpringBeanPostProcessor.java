package com.mszlu.rpc.rpc;

import com.mszlu.rpc.annontation.MsReference;
import com.mszlu.rpc.annontation.MsService;
import com.mszlu.rpc.proxy.MsRpcClientProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class MsRpcSpringBeanPostProcessor implements BeanPostProcessor {

    ////bean初始化方法前被调用
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
      
        return bean;
    }

    //bean初始化方法调用后被调用
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
          //在这里判断bean上有没有加MsService注解
        //如果有，将其发布为服务
        if (bean.getClass().isAnnotationPresent(MsService.class)){
            MsService msService = bean.getClass().getAnnotation(MsService.class);
//            serviceProvider.publishService(msService);
        }
        
        //在这里判断bean里面的字段有没有加@MsRefrence注解
        //如果有 识别并生成代理实现类，发起网络请求
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            MsReference annotation = declaredField.getAnnotation(MsReference.class);
            if (annotation != null){
                //代理实现类，调用方法的时候 会触发invoke方法，在其中实现网络调用
                MsRpcClientProxy msRpcClientProxy = new MsRpcClientProxy(annotation);
                Object proxy = msRpcClientProxy.getProxy(declaredField.getType());
                //当isAccessible()的结果是false时不允许通过反射访问该字段
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
