package com.tenco.bank.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.AccountSaveFormDto;
import com.tenco.bank.dto.WithdrawFormDto;
import com.tenco.bank.handler.exception.CustomRestfulException;
import com.tenco.bank.repository.entity.Account;
import com.tenco.bank.repository.entity.History;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.utils.Define;

@Service // IoC의 대상 + 싱글톤
public class AccountService {

	@Autowired
	private AccountRepository accountRepository; // DI

	@Autowired
	private HistoryRepository historyRepository;

	// 계좌 생성
	@Transactional
	public void createAccount(AccountSaveFormDto dto, Integer principalId) {
		// 계좌 번호 중복 확인, 예외 처리
		if (readAccount(dto.getNumber()) != null) {
			throw new CustomRestfulException(Define.EXIST_ACCOUNT, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		Account account = new Account();
		account.setNumber(dto.getNumber());
		account.setPassword(dto.getPassword());
		account.setBalance(dto.getBalance());
		account.setUserId(principalId);

		int resultRowCount = accountRepository.insert(account);
		if (resultRowCount != 1) {
			throw new CustomRestfulException(Define.FAIL_TO_CREATE_ACCOUNT, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 단일계좌 검색 기능
	public Account readAccount(String number) {
		return accountRepository.findByNumber(number.trim());
	}

	// 계좌목록 보기 기능
	public List<Account> readAccountListByUserId(Integer principalId) {
		return accountRepository.findAllByUserId(principalId);
	}

	// 출금 기능 만들기
	// 7. 트랜잭션 처리
	@Transactional
	public void updateAccountWithdraw(WithdrawFormDto dto, Integer principalId) {
		Account accountEntity = accountRepository.findByNumber(dto.getWAccountNumber());
		// 1. 계좌 존재 여부 확인 - select
		if (accountEntity == null) {
			throw new CustomRestfulException(Define.NOT_EXIST_ACCOUNT, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// 2. 본인 계좌 여부 확인 - select
		accountEntity.checkOwner(principalId);

		// 3. 계좌 비밀번호 확인
		accountEntity.checkPassword(dto.getWAccountPassword());

		// 4. 잔액 여부 확인
		accountEntity.checkBalance(dto.getAmount());

		// 5. 출금 처리 - update
		accountEntity.withdraw(dto.getAmount());
		accountRepository.updateById(accountEntity);

		// 6. 거래 내역 등록 - insert
		History history = new History();
		history.setAmount(dto.getAmount());
		history.setWBalance(accountEntity.getBalance());
		history.setDBalance(null);
		history.setWAccountId(accountEntity.getId());
		history.setDAccountId(null);

		int rowResultCount = historyRepository.insert(history);
		if (rowResultCount != 1) {
			throw new CustomRestfulException("정상처리 되지 않았습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}