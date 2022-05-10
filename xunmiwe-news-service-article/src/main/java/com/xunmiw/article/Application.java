package com.xunmiw.article;

import com.ribbon.rule.RibbonRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.xunmiw.article.mapper")
@ComponentScan(basePackages = {"com.xunmiw", "org.n3r.idworker"})
@EnableEurekaClient
@RibbonClient(name = "service-user", configuration = RibbonRule.class)
@EnableFeignClients({"com.xunmiw"})
@EnableHystrix
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
