package com.weishao.SpringFlowable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * （1）初始化无法建表的问题解决方法：
 * https://blog.csdn.net/houyj1986/article/details/85546680
 * （2）flowable中文使用手册
 * https://tkjohn.github.io/flowable-userguide/
 * 
 * @author Tang
 *
 */
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
public class SpringActivitApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(SpringActivitApplication.class, args);
	}
}
