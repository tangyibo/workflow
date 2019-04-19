package com.weishao.SpringActivit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * SpringBoot整合Swagger2配置类
 * 通过@Configuration注解，让Spring来加载该类配置。
 * 参考地址：https://www.cnblogs.com/jtlgb/p/8532433.html
 * 再通过@EnableSwagger2注解来启用Swagger2。
 * 
 * @author Tang Yibo
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig implements WebMvcConfigurer  {
	
	@Value("${swagger.show}")
    private boolean swaggerShow;
	
    @Bean
	public Docket createRestApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.enable(this.swaggerShow)// 是否开启swagger  
				.groupName("工作流")
				.apiInfo(this.apiInfo())
				.select()
				.apis(RequestHandlerSelectors
						.basePackage("com.weishao.SpringActivit.controller")) // 对该包下的api进行监控
				.paths(PathSelectors.any()) // 对该包下的所有路径进行监控
				.build();
	}
    
    
    private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("工作流服务api文档")
				.description("为smartDM构建的基于activiti6.0构建的工作流服务api文档")
				.termsOfServiceUrl("http://127.0.0.1:8080")
				.version("1.0")
				.build();
    }
    
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}

