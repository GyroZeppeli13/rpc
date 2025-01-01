package com.mszlu.rpc.spring;

import com.mszlu.rpc.annontation.EnableRpc;
import com.mszlu.rpc.annontation.MsReference;
import com.mszlu.rpc.annontation.MsService;
import com.mszlu.rpc.config.MsRpcConfig;
import com.mszlu.rpc.factory.ClientFactory;
import com.mszlu.rpc.factory.SingletonFactory;
import com.mszlu.rpc.register.nacos.NacosTemplate;
import com.mszlu.rpc.remoting.MsClient;
import com.mszlu.rpc.remoting.netty.NettyClient;
import com.mszlu.rpc.proxy.MsRpcClientProxy;
import com.mszlu.rpc.remoting.socket.SocketRpcClient;
import com.mszlu.rpc.server.MsServiceProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Component
@Slf4j
public class MsRpcSpringBeanPostProcessor implements BeanPostProcessor, BeanFactoryPostProcessor {

    private MsServiceProvider msServiceProvider;
    private MsClient client;
    private NacosTemplate nacosTemplate;
    private MsRpcConfig msRpcConfig;

    public MsRpcSpringBeanPostProcessor(){
        msServiceProvider = SingletonFactory.getInstance(MsServiceProvider.class);
        //创建客户端
//        client = SingletonFactory.getInstance(NettyClient.class);
        client = ClientFactory.getClient();
        nacosTemplate = SingletonFactory.getInstance(NacosTemplate.class);
    }
    ////bean初始化方法前被调用
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        EnableRpc enableRpc = bean.getClass().getAnnotation(EnableRpc.class);
        if (enableRpc != null){
            if (msRpcConfig == null){
                log.info("EnableRpc会先于所有的bean初始化之前执行，在这里我们进行配置的加载");
                msRpcConfig = new MsRpcConfig();
                msRpcConfig.setNacosGroup(enableRpc.nacosGroup());
                msRpcConfig.setNacosHost(enableRpc.nacosHost());
                msRpcConfig.setNacosPort(enableRpc.nacosPort());
                msRpcConfig.setProviderPort(enableRpc.serverPort());
                msServiceProvider.init(msRpcConfig);
                //nacos 根据配置进行初始化
                nacosTemplate.init(msRpcConfig.getNacosHost(),msRpcConfig.getNacosPort());
                //客户端加载配置
                client.setMsRpcConfig(msRpcConfig);
            }
        }
        return bean;
    }

    //bean初始化方法调用后被调用
    @SneakyThrows
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        MsService msService = bean.getClass().getAnnotation(MsService.class);
        if (msService != null){
            //发布服务，如果netty服务未启动进行启动
            msServiceProvider.publishService(msService, bean);
        }
        //在这里判断bean里面的字段有没有加@MsRefrence注解
        //如果有 识别并生成代理实现类，发起网络请求
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            MsReference msReference = declaredField.getAnnotation(MsReference.class);
            if (msReference != null){
                //代理实现类，调用方法的时候 会触发invoke方法，在其中实现网络调用
                MsRpcClientProxy msRpcClientProxy = new MsRpcClientProxy(msReference, client);
                Object proxy = msRpcClientProxy.getProxy(declaredField.getType());
                //当isAccessible()的结果是false时不允许通过反射访问该字段
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof BeanDefinitionRegistry) {
            try {
                // init scanner
                Class<?> scannerClass = ClassUtils.forName ( "org.springframework.context.annotation.ClassPathBeanDefinitionScanner",
                        MsRpcSpringBeanPostProcessor.class.getClassLoader () );
                Object scanner = scannerClass.getConstructor ( new Class<?>[]{BeanDefinitionRegistry.class, boolean.class} )
                        .newInstance ( new Object[]{(BeanDefinitionRegistry) beanFactory, true} );
                // add filter
                Class<?> filterClass = ClassUtils.forName ( "org.springframework.core.type.filter.AnnotationTypeFilter",
                        MsRpcSpringBeanPostProcessor.class.getClassLoader () );
                Object filter = filterClass.getConstructor ( Class.class ).newInstance ( EnableRpc.class );
                Method addIncludeFilter = scannerClass.getMethod ( "addIncludeFilter",
                        ClassUtils.forName ( "org.springframework.core.type.filter.TypeFilter", MsRpcSpringBeanPostProcessor.class.getClassLoader () ) );
                addIncludeFilter.invoke ( scanner, filter );
                // scan packages
                Method scan = scannerClass.getMethod ( "scan", new Class<?>[]{String[].class} );
                scan.invoke ( scanner, new Object[]{"com.mszlu.rpc.annontation"} );
            } catch (Throwable e) {
                // spring 2.0
            }
        }
    }
}