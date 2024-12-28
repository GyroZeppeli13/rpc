package com.mszlu.rpc.consumer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

	//定义restTemplate，spring提供
    ////发起http请求，传递参数，解析返回值（ Class<T> responseType）
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}