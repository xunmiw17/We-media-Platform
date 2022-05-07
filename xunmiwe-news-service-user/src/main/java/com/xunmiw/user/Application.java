package com.xunmiw.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = MongoAutoConfiguration.class)
@MapperScan(basePackages = "com.xunmiw.user.mapper")
@ComponentScan(basePackages = {"com.xunmiw", "org.n3r.idworker"})
@EnableEurekaClient     // 开启Eureka client，注册到server中
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
