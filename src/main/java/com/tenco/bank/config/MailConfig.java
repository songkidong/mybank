package com.tenco.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
	@Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl jms = new JavaMailSenderImpl();
        // 아래의 설정은 .yml 파일에 있는 설정을 그대로 대입하면 된다!
        jms.setHost("smtp.gmail.com"); // smtp 기본 설정(그대로 넣으면 됨)
        jms.setPort(587); // 지메일 서버가 사용하는 포트 번호
        jms.setUsername("indiefliker@gmail.com"); // 내가 설정한 발신 이메일
        jms.setPassword("scvfdnsdqlavzjxj"); // 앱 비밀번호

        Properties properties = new Properties(); // 속성 정보를 담는 객체
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.timeout", "5000");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        jms.setJavaMailProperties(properties);
        return jms;
    }

}