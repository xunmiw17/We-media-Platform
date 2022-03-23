package com.imooc.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.imooc.user.mapper")
@ComponentScan("com.imooc")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
