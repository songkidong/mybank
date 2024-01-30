package com.tenco.bank.dto;

import lombok.Data;

@Data
public class AccountSaveFormDto {

	private String number;		// 계좌번호
	private String password;	// 비밀번호
	private Long balance;		// 금액
	
}
