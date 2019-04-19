package com.weishao.SpringActivit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
public class SpringActivitApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(SpringActivitApplication.class, args);
	}
}
