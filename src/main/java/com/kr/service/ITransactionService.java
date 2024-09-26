package com.kr.service;

public interface ITransactionService {
	boolean deposit(String username, Double amt);

	boolean withdraw(String username, Double amt);

	boolean transferFund(String toUsername, String fromUsername, Double amt);

}
