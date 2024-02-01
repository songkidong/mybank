package com.tenco.bank.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.AccountSaveFormDto;
import com.tenco.bank.dto.DepositFormDto;
import com.tenco.bank.dto.TransferFormDto;
import com.tenco.bank.dto.WithdrawFormDto;
import com.tenco.bank.handler.exception.CustomRestfulException;
import com.tenco.bank.repository.entity.Account;
import com.tenco.bank.repository.entity.CustomHistoryEntity;
import com.tenco.bank.repository.entity.History;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.utils.Define;

@Service // IoC의 대상 + 싱글톤
public class AccountService {

	@Autowired
	private AccountRepository accountRepository; // DI

	@Autowired
	private HistoryRepository historyRepository; // DI

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

	// 입금 기능 만들기 - ATM
	@Transactional
	public void updateAccountDeposit(DepositFormDto dto, Integer principalId) {
		// 계좌 존재 여부 확인
		Account accountEntity = accountRepository.findByNumber(dto.getDAccountNumber());
		if (accountEntity == null) {
			throw new CustomRestfulException(Define.NOT_EXIST_ACCOUNT, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// 본인 계좌 여부 확인
		accountEntity.checkOwner(principalId);

		// 입금처리
		accountEntity.deposit(dto.getAmount());
		accountRepository.updateById(accountEntity);

		// history 에 거래내역 등록
		History history = new History();
		history.setAmount(dto.getAmount());
		history.setWBalance(null); // 출금 계좌의 잔액을 가져와야하기 때문에
		history.setDBalance(accountEntity.getBalance());
		history.setWAccountId(null);
		history.setDAccountId(accountEntity.getId());

		int rowResultCount = historyRepository.insert(history);
		if (rowResultCount != 1) {
			throw new CustomRestfulException("정상 처리 되지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 이체 기능만들기
	// 1. 출금 계좌 존재 여부
	// 2. 입금 계좌 존재 확인
	// 3. 출금 계좌 본인 소유 확인
	// 4. 출금 계좌 비번 확인
	// 5. 출금 계좌 잔액 확인
	// 6. 출금 계좌 잔액 객체 수정
	// 7. 입금 계좌 잔액 객체 수정
	// 8. 출금 계좌 update
	// 9. 입금 계좌 update
	// 10. 거래 내역 등록 처리
	// 11.트랜잭션 처리
	@Transactional
	public void updateAccountTransfer(TransferFormDto dto, Integer principalId) {
		Account withdrawAccountEntity = accountRepository.findByNumber(dto.getWAccountNumber());
		Account depositAccountEntity = accountRepository.findByNumber(dto.getDAccountNumber());

		if (withdrawAccountEntity == null) {
			throw new CustomRestfulException(Define.NOT_EXIST_ACCOUNT, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (depositAccountEntity == null) {
			throw new CustomRestfulException("상대방의 계좌 번호가 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		withdrawAccountEntity.checkOwner(principalId);

		withdrawAccountEntity.checkPassword(dto.getPassword());

		withdrawAccountEntity.checkBalance(dto.getAmount());

		withdrawAccountEntity.withdraw(dto.getAmount());

		depositAccountEntity.deposit(dto.getAmount());

		int resultRowCountWithdraw = accountRepository.updateById(withdrawAccountEntity);
		int resultRowCountDeposit = accountRepository.updateById(depositAccountEntity);

		if (resultRowCountWithdraw != 1 && resultRowCountDeposit != 1) {
			throw new CustomRestfulException("정상 처리 되지 않았습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		History history = History.builder().amount(dto.getAmount()) // 이체 금액
				.wAccountId(withdrawAccountEntity.getId()) // 출금 계좌
				.dAccountId(depositAccountEntity.getId()) // 입금 계좌
				.wBalance(withdrawAccountEntity.getBalance()) // 출금 계좌 남은 잔액
				.dBalance(depositAccountEntity.getBalance()) // 입금 계좌 남은 잔액
				.build();

		int resultRowCountHistory = historyRepository.insert(history);
		if (resultRowCountHistory != 1) {
			throw new CustomRestfulException("정상 처리 되지 않았습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 단일 계좌 거래 내역 검색 (전체, 입금, 출금)
	 * 
	 * @param type = [all, deposit, withdraw]
	 * @param id   (account_id)
	 * @return 동적 쿼리 - List
	 */
	public List<CustomHistoryEntity> readHistoryListByAccount(String type, Integer id) {
		return historyRepository.findByIdHistoryType(type, id);
	}

	// 단일 계좌 조회 - AccountById
	public Account readByAccountId(Integer id) {
		return accountRepository.findByAccountId(id);
	}
}
