package com.edward.gmall.order;

import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@DubboComponentScan(basePackages = "com.edward.gmall.order.service.impl")
@MapperScan(basePackages = "com.edward.gmall.order.mapper")
@ComponentScan(basePackages = "com.edward.gmall")
public class GmallOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallOrderServiceApplication.class, args);
    }

}
