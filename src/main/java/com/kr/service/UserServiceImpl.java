package com.kr.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.kr.model.TransactionsHistory;
import com.kr.model.UserAccount;
import com.kr.repository.ITransactionRepository;
import com.kr.repository.IUserRepository;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class UserServiceImpl implements UserDetailsService, IUserService, ITransactionService {

	@Autowired
	private IUserRepository repo;
	@Autowired
	private ITransactionRepository transRepo;
	@Autowired
	private PasswordEncoder encoder;
	@Autowired
	JavaMailSender mailSender;
	@Autowired
	private Configuration config;
	@Value("${spring.mail.username}") // getting data from properties file
	private String fromEmail; // admin email to send emails

	@Override
	public boolean deposit(String username, Double amt) {
		boolean isDoneSuccessfully=false;
		if (amt < 0)
			throw new IllegalArgumentException("Invalid Amount");
		else {
			UserAccount user = getUser(username);
			user.setBalance(user.getBalance() + amt);
			repo.save(user);
			transRepo.save(new TransactionsHistory(0, "Deposit", amt, getFormattedDateTime(), user));
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
			transRepo.save(new TransactionsHistory(0, "Withdraw", amt, getFormattedDateTime(), user));
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
	public boolean updatePassword(String password, String username) {
		int updatePassword = repo.updateUserPassword(encoder.encode(password), username);
		return (updatePassword==0)?false:true;
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
	public List<TransactionsHistory> getTransactionHistory(Integer id,Sort sort) {
		List<TransactionsHistory> list =
	            transRepo.findAll(sort).stream().filter(e -> e.getAccount().getId() == id).collect(Collectors.toList());
		System.out.println(list);
	    return list;
	}
	
	@Override
	public Sort sortHistory(String sortBy) {
		return Sort.by(Direction.DESC, sortBy);
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

	private LocalDateTime getFormattedDateTime() {
		// date and time formatting
		return LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy hh:mm:ss a")),DateTimeFormatter.ofPattern("dd MMMM yyyy hh:mm:ss a"));
	}
	
	@Override
	public boolean sendMail(String toEmail, UserAccount user) {
		MimeMessage message = mailSender.createMimeMessage();
		try {
			// set mediaType
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			Template t = config.getTemplate("reg_template.ftl");
			Map<String, Object> map = Map.of("date", getFormattedDateTime(), "name", user.getName().split(" ")[0],"accountNumber", user.getId(),"accountType", "Savings","username", user.getUsername());
			// it will changes into String format and add dynamic values from map
			String html = FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
			helper.setTo(toEmail);
			helper.setText(html, true);
			helper.setSubject("Registration Success");
			helper.setFrom(fromEmail);
			mailSender.send(message);
			return true;
		} catch (MessagingException | IOException | TemplateException e) {
			e.printStackTrace();
			return false;
		}
	}
	private String userOtp;
	private String generateOTP()
	{
		Random random = new Random();
		StringBuilder otp = new StringBuilder();
		for(int i=1;i<=6;i++)
		{
			otp.append(random.nextInt(10));
		}
		return otp.toString();
	}
	
	@Override
	public boolean verifyOTP(String otp) {
		if(otp.equals(userOtp))
		{
			System.out.println("true");
			return true;			
		}
		else {
			System.out.println("false");
			return false;
		}
	}

	@Override
	public boolean sendOTPMail(String username) {
		userOtp = generateOTP();
		System.out.println(userOtp);
		UserAccount user = getUser(username);
		MimeMessage message = mailSender.createMimeMessage();
		try {
			// set mediaType
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			Template t = config.getTemplate("otp_template.ftl");
			Map<String, Object> map = Map.of("date", getFormattedDateTime()+" "+getFormattedDateTime(),"otp", userOtp,"email",user.getEmail(),"name",user.getName().split(" ")[0]);
			// it will changes into String format and add dynamic values from map
			String html = FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
			helper.setTo(user.getEmail());
			helper.setText(html, true);
			helper.setSubject("Verification Code");
			helper.setFrom(fromEmail);
			mailSender.send(message);
			return true;
		} catch (MessagingException | IOException | TemplateException e) {
			e.printStackTrace();
			return false;
		}
	}

}
