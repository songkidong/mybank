package com.tenco.bank.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
//json 형식에 코딩 컨벤션 스네이크 케이스를 카멜 노테이션으로 변경하기
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Response {
	private String id;
	private String profileImage;
	private String name;
}
