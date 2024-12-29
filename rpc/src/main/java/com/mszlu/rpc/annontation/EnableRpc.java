package com.mszlu.rpc.annontation;

import com.mszlu.rpc.rpc.MsRpcSpringBeanPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({MsRpcSpringBeanPostProcessor.class})
public @interface EnableRpc {

    String basePackage();
}