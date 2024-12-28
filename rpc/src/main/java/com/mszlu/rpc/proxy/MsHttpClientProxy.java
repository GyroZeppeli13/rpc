package com.mszlu.rpc.proxy;

import com.mszlu.rpc.annontation.MsMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 每一个动态代理类的调用处理程序都必须实现InvocationHandler接口，
// 并且每个代理类的实例都关联到了实现该接口的动态代理类调用处理程序中，
// 当我们通过动态代理对象调用一个方法时候，
// 这个方法的调用就会被转发到实现InvocationHandler接口类的invoke方法来调用
public class MsHttpClientProxy implements InvocationHandler {

    public MsHttpClientProxy(){

    }

    /**
     * proxy:代理类代理的真实代理对象com.sun.proxy.$Proxy0
     * method:我们所要调用某个对象真实的方法的Method对象
     * args:指代代理对象方法传递的参数
     */
    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        //在这里实现调用
        MsMapping msMapping = method.getAnnotation(MsMapping.class);
        if (msMapping != null){
            RestTemplate restTemplate = new RestTemplate();
            String api = msMapping.api();
            Pattern compile = Pattern.compile("(\\{\\w+})");
            Matcher matcher = compile.matcher(api);
            if (matcher.find()){
                //简单判断一下 代表有路径参数需要替换
                int x = matcher.groupCount();
                for (int i = 0; i< x; i++){
                    String group = matcher.group(i);
                    api = api.replace(group, args[i].toString());
                }
            }
            ResponseEntity forEntity = restTemplate.getForEntity(msMapping.url()+ api, method.getReturnType());
            return forEntity.getBody();
        }
        return null;
    }

    /**
     * 获取代理对象
     * @param clazz
     * @return
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

}
