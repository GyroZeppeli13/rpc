package com.mszlu.rpc.bean;

//import com.mszlu.rpc.proxy.MsRpcClientProxy;
import org.springframework.beans.factory.FactoryBean;
//FactoryBean是一个工厂Bean，可以生成某一个类型Bean实例，它最大的一个作用是：可以让我们自定义Bean的创建过程。
public class MsHttpClientFactoryBean<T> implements FactoryBean<T> {

    private Class<T> interfaceClass;
    //返回的对象实例
    @Override
    public T getObject() throws Exception {
        return  null;
//        return new MsHttpClientProxy().getProxy(interfaceClass);
    }
    //Bean的类型
    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    //true是单例，false是非单例  在Spring5.0中此方法利用了JDK1.8的新特性变成了default方法，返回true
    @Override
    public boolean isSingleton() {
        return true;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }
}