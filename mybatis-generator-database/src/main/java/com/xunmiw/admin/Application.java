package com.xunmiw.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.xunmiw.user.mapper")
@ComponentScan("com.xunmiw")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
