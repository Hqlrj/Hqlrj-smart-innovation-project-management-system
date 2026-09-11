package org.example.springbootproject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类
 * @MapperScan 注解用于扫描Mapper接口，自动注册为Spring Bean
 */
@SpringBootApplication
@MapperScan("org.example.springbootproject.mapper")
public class SpringBootProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootProjectApplication.class, args);
    }

}
