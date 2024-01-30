package com.tenco.bank.repository.interfaces;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tenco.bank.repository.entity.Account;

//interface + xml 연결
@Mapper
public interface AccountRepository {
	public int insert(Account account);

	public int updateById(Account account);

	public int deleteById(Integer id);

	// 계좌 조회 - 1유저, N 계좌
	public List<Account> findByUserId();

	public Account findByNumber(Integer id);
}
