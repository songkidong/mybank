package com.tenco.bank.dto;

import lombok.Data;

@Data
public class TransferFormDto {

	private Long amount;
	private String dAccountNumber;		// 출금 계좌
	private String wAccountNumber;		// 입금 계좌
	private String password;
}
