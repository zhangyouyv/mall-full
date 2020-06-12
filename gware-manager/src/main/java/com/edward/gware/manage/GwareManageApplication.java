package com.edward.gware.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.edward.gware.manage.gware.mapper")
public class GwareManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(GwareManageApplication.class, args);
    }

}
