package com.zzq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication // spring 启动注解
public class Application {

    public static void main(String[] args) {
        SpringApplication.run( Application.class , args );
    }

}
