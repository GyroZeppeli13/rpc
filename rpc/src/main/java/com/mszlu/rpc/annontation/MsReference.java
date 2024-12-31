package com.mszlu.rpc.annontation;


import java.lang.annotation.*;
//可用于构造方法和字段上
@Target({ElementType.CONSTRUCTOR,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MsReference {

    String host();

    int port();

    String version() default "1.0";
}