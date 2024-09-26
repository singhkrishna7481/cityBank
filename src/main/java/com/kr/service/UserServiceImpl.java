package com.kr.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kr.model.TransactionsHistory;
import com.kr.model.UserAccount;
import com.kr.repository.ITransactionRepository;
import com.kr.repository.IUserRepository;

@Service
public class UserServiceImpl implements UserDetailsService, IUserService, ITransactionService {

	@Autowired
	private IUserRepository repo;
	@Autowired
	private ITransactionRepository transRepo;
	@Autowired
	private PasswordEncoder encoder;

	@Override
	public boolean deposit(String username, Double amt) {
		boolean isDoneSuccessfully=false;
		if (amt < 0)
			throw new IllegalArgumentException("Invalid Amount");
		else {
			UserAccount user = getUser(username);
			user.setBalance(user.getBalance() + amt);
			repo.save(user);
			List<String> dateTime = getFormattedDateTime();
			transRepo.save(new TransactionsHistory(0, "Deposit", amt, dateTime.get(0), dateTime.get(1), user));
			isDoneSuccessfully=true;
		}
		return isDoneSuccessfully;
	}

	@Override
	public boolean withdraw(String username, Double amt) {
		boolean isDoneSuccessfully=false;
		UserAccount user = getUser(username);
		if (amt < 0) {
			throw new IllegalArgumentException("Invalid Amount.");
		} else if (user.getBalance() <= 0 || user.getBalance() < amt) {
			throw new IllegalArgumentException("Insufficient Balance.");
		} else {
			user.setBalance(user.getBalance() - amt);
			repo.save(user);
			List<String> dateTime = getFormattedDateTime();
			transRepo.save(new TransactionsHistory(0, "Withdraw", amt, dateTime.get(0), dateTime.get(1), user));
			isDoneSuccessfully=true;
		}
		return isDoneSuccessfully;
	}

	@Override
	public boolean transferFund(String toUsername, String fromUsername, Double amt) {
		boolean isDoneSuccessfully=false;

		if (toUsername.equalsIgnoreCase(fromUsername)) {
			throw new IllegalArgumentException("You Cann't Transfer Fund To Your Own Account");
		}
		else if (findByUsername(toUsername)) 
		{
			withdraw(fromUsername, amt);
			deposit(toUsername, amt);
			isDoneSuccessfully=true;
		} else {
			throw new IllegalArgumentException("Recipient Not Found Please Check Username Once ");
		}
		return isDoneSuccessfully;
	}

	@Override
	public void addUser(UserAccount user) {
		user.setPassword(encoder.encode(user.getPassword()));
		repo.save(user);
	}

	@Override
	public boolean findByUsername(String username) {
		return repo.findByUsername(username).isPresent();
	}

	@Override
	public UserAccount getUser(String username) {
		return repo.findByUsername(username).get();
	}

	@Override
	public UserAccount getUserById(Integer id) {
		return repo.findById(id).get();
	}

	@Override
	public List<TransactionsHistory> getTransactionHistory(Integer id) {
		List<TransactionsHistory> list = transRepo.findAll().stream().filter(e -> e.getAccount().getId() == (id))
				.collect(Collectors.toList());
		System.out.println(list);
		return list;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Optional<UserAccount> userAccount = repo.findByUsername(username);
		userAccount.orElseThrow(() -> new UsernameNotFoundException("User Not Found Execption"));
		UserAccount user = userAccount.get();
		Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();

		for (String role : user.getRoles()) {
			SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
			roles.add(authority);
		}
		User userObj = new User(user.getUsername(), user.getPassword(),
				user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toSet()));

		return userObj;
	}

	private List<String> getFormattedDateTime() {
		// date and time formatting
		String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM, yyyy hh:mm a"));
		String[] dateTimeParts = dateTime.split(" ");

		String date = dateTimeParts[0] + " " + dateTimeParts[1] + " " + dateTimeParts[2];
		String time = dateTimeParts[3] + " " + dateTimeParts[4];
		return List.of(date, time);
	}
}
