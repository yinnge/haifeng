package com.haifeng.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 海峰管理端启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.haifeng.common", "com.haifeng.admin"})
@MapperScan("com.haifeng.common.mapper")
public class HaiFengAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(HaiFengAdminApplication.class, args);
    }
}
