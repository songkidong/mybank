package com.tenco.bank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tenco.bank.handler.exception.CustomPageException;
import com.tenco.bank.handler.exception.CustomRestfulException;

@Controller
@RequestMapping("/test1")
public class TestController {
	
	// 주소 설계
	// http://localhost:80/test1/main
	@GetMapping("/main") public String mainPage() {
		// 예외 발생
		throw new CustomRestfulException("페이지가 없네요", HttpStatus.NOT_FOUND);
//		return "layout/main";
	}
}
