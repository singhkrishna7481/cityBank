package com.kr.service;

import java.util.List;

import org.springframework.data.domain.Sort;

import com.kr.model.TransactionsHistory;
import com.kr.model.UserAccount;

public interface IUserService {
	void addUser(UserAccount user);

	boolean findByUsername(String username);

	UserAccount getUser(String username);
	
	UserAccount getUserById(Integer id);
	
	public List<TransactionsHistory> getTransactionHistory(Integer id,Sort sort);
	
	boolean sendMail(String toEmail,UserAccount user);
	
	boolean verifyOTP(String otp);
	
	boolean sendOTPMail(String username);
	
	boolean updatePassword(String password, String username);
	
	public Sort sortHistory(String sortBy);
}
