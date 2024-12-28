package com.mszlu.rpc.annontation;

import java.lang.annotation.*;

//可用于方法上
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MsMapping {

    String api() default "";

    String url() default "";
}