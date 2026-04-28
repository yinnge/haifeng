package com.haifeng.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 海峰用户端启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.haifeng.common", "com.haifeng.app"})
@MapperScan("com.haifeng.app.mapper")
public class HaiFengAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(HaiFengAppApplication.class, args);
    }
}
