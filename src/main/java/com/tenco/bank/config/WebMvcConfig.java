package com.tenco.bank.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.tenco.bank.handler.AuthInterceptor;

// @Configuration --> 스프링부트 설정 클래스이다.
@Configuration // IoC 대상 : 2개 이상의 IoC (Bean) 만들 때 사용
public class WebMvcConfig implements WebMvcConfigurer{

	@Autowired // DI
	private AuthInterceptor authInterceptor;
	
	// 요청이 올때 마다 domain URI 검사를 할 예정
	// /account/xxx 으로 들어오는 도메인을 다 검사해!!!
	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(authInterceptor)
				.addPathPatterns("/account/**")
				.addPathPatterns("/auth/**");
				

		WebMvcConfigurer.super.addInterceptors(registry);
	}
}
