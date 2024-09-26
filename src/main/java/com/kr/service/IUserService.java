package com.kr.service;

import java.util.List;

import com.kr.model.TransactionsHistory;
import com.kr.model.UserAccount;

public interface IUserService {
	void addUser(UserAccount user);

	boolean findByUsername(String username);

	UserAccount getUser(String username);
	
	UserAccount getUserById(Integer id);
	
	List<TransactionsHistory> getTransactionHistory(Integer id);
}
