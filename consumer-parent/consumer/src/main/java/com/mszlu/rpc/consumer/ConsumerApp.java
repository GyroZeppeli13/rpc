package com.mszlu.rpc.consumer;

import com.mszlu.rpc.annontation.EnableHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableHttpClient(basePackage = "com.mszlu.rpc.consumer.rpc")
public class ConsumerApp {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApp.class,args);
    }
}
