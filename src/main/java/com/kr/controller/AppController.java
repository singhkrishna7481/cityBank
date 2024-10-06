package com.kr.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itextpdf.text.DocumentException;
import com.kr.model.TransactionsHistory;
import com.kr.model.UserAccount;
import com.kr.report.TransactionsReportPDF;
import com.kr.service.ITransactionService;
import com.kr.service.IUserService;

@Controller
@RequestMapping("bank/")
public class AppController {
	@Autowired
	private TransactionsReportPDF pdf;
	@Autowired
	private IUserService service;
	@Autowired
	private ITransactionService transService;
	
	@GetMapping("home")
	public String home() {
		return "login";
	}

	@GetMapping("/dash")
	public String dashboard(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserAccount user = service.getUser(authentication.getName());
		DecimalFormat df = new DecimalFormat("#.00");
		user.setBalance(Double.parseDouble(df.format(user.getBalance())));
		model.addAttribute("user", user);
		return "dash";
	}

	@GetMapping("register")
	public String reg(@ModelAttribute UserAccount user) {
		return "register";
	}
	
	@GetMapping("forgot")
	public String forgot() {
		return "forgot";
	}
	
	@PostMapping("send")
	public String sendOtp(@RequestParam String username, RedirectAttributes attr) {
		if(service.findByUsername(username))
		{
			boolean isSent = service.sendOTPMail(username);
			if(isSent)
			{
				attr.addFlashAttribute("success", true);
				attr.addFlashAttribute("otpSent", true);
				attr.addFlashAttribute("username",username);
				attr.addFlashAttribute("sent","OTP sent Successfully");
			}
			else {
				attr.addFlashAttribute("error", true);
				attr.addFlashAttribute("serverError","Error Occured");
			}
		}
		else {
			attr.addFlashAttribute("error", true);
			attr.addFlashAttribute("userNotFound","User Not Found");
		}
		return "redirect:forgot";
	}
	
	@PostMapping("verify")
	public String verifyOtp(@RequestParam String otp,@RequestParam String usernm, RedirectAttributes attr,Model model) {
		
		boolean isValid = service.verifyOTP(otp);
		if(isValid)
		{
			model.addAttribute("usernm",usernm);
			return "reset-pswd";
		}
		else {
			attr.addFlashAttribute("error", true);
			attr.addFlashAttribute("otpSent", true);
			attr.addFlashAttribute("username",usernm);
			attr.addFlashAttribute("invalidOTP","Invalid OTP");
		}
		return "redirect:forgot";
	}

	@PostMapping("register")
	public String register(@ModelAttribute UserAccount user,RedirectAttributes attr, Model model) {
		if(!(service.findByUsername(user.getUsername())))
		{
			attr.addFlashAttribute("name", user.getName());
			attr.addFlashAttribute("username", user.getUsername());
			attr.addFlashAttribute("email", user.getEmail());
			attr.addFlashAttribute("phone", user.getPhone().toString());
			service.addUser(user);
			service.sendMail(user.getEmail(), user);
			return "redirect:./details";
		}
		else {
			model.addAttribute("error", "User already Exists.");
			return "register";
		}
	}
	private String username=null;
	@PostMapping("reset")
	public String update(@RequestParam String pswd,@RequestParam String usernm,@RequestParam String password, Model model,RedirectAttributes attr) {
		
		if(username==null)
		{
			username=usernm;
		}
		if(pswd.equals(password))
		{
			attr.addFlashAttribute("success",true);
			attr.addFlashAttribute("updated","Password Updated");
			service.updatePassword(password, username);
			username=null;
			return "redirect:home";
		}
		else {
			model.addAttribute("error","Password Didn't Matched.");
			return "reset-pswd";
		}
	}
	
	@GetMapping("details")
	public String details() {
		return "regDetails";
	}
	@GetMapping("transactions")
	public String transactions(Model model,@RequestParam(required = false,defaultValue = "timeStamp") String by) {
		Integer id = service.getUser(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
		List<TransactionsHistory> transactions = service.getTransactionHistory(id,service.sortHistory(by));
		model.addAttribute("transactions", transactions);
		model.addAttribute("user", service.getUserById(id));
		return "trans";
	}
	
	@GetMapping("view-deposits")
	public String viewDeposits(Model model) {
		Integer id = service.getUser(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
		List<TransactionsHistory> transactions = service.getTransactionHistory(id,service.sortHistory("timeStamp"));
		transactions=transactions.stream().filter(user->user.getTransType().equals("Deposit")).collect(Collectors.toList());
		model.addAttribute("transactions", transactions);
		model.addAttribute("user", service.getUserById(id));
		return "trans";
	}
	@GetMapping("view-withdrawals")
	public String viewWithdrawals(Model model) {
		Integer id = service.getUser(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
		List<TransactionsHistory> transactions = service.getTransactionHistory(id,service.sortHistory("timeStamp"));
		transactions=transactions.stream().filter(user->user.getTransType().equals("Withdraw")).collect(Collectors.toList());
		model.addAttribute("transactions", transactions);
		model.addAttribute("user", service.getUserById(id));
		return "trans";
	}
	
	@GetMapping("report")
	public String report(RedirectAttributes attr) {
		UserAccount user = service.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
		List<TransactionsHistory> transactions = service.getTransactionHistory(user.getId(),service.sortHistory("timeStamp"));
		try {
			pdf.generateReport(transactions,user);
			attr.addFlashAttribute("isGenerated",true);
		} catch (FileNotFoundException | DocumentException  e) {
			e.printStackTrace();
		}
		return "redirect:./transactions";
	}
	
//	@GetMapping("get-report")
//	public ResponseEntity<Resource> getReport() throws IOException {
//		Resource resource = new ClassPathResource("reports/report.pdf");
//	    return ResponseEntity(resource,MediaType.APPLICATION_PDF,HttpStatus.ok);
//		Resource resource = new ClassPathResource("reports/report.pdf");
//	    byte[] fileBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
//	    HttpHeaders headers = new HttpHeaders();
//	    headers.setContentType(MediaType.APPLICATION_PDF);
////	    headers.setContentDispositionFormData("inline", "report.pdf");
//	    headers.setContentDispositionFormData("attachment", "report.pdf");
//	    return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
//	}
	@GetMapping("get-report")
	public ResponseEntity<Resource>  getReport() throws IOException {
		File file = new File("src/main/resources/reports/report.pdf");
	    Resource resource = new FileSystemResource(file);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
				.body(resource);
	}
	@GetMapping("view-report")
	public ResponseEntity<Resource> viewReport() throws IOException {
		File file = new File("src/main/resources/reports/report.pdf");
	    Resource resource = new FileSystemResource(file);
	    return ResponseEntity.ok()
	            .contentType(MediaType.APPLICATION_PDF)
	            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=report.pdf")
	            .body(resource);
	}
	
	@PostMapping("/deposit")
	public String deposit(@RequestParam Double amount, RedirectAttributes attr) {
		try {
			boolean isCredited = transService.deposit(SecurityContextHolder.getContext().getAuthentication().getName(), amount);
			if(isCredited)
			{
				attr.addFlashAttribute("success","Money Credited...");
			}
			else {
				attr.addFlashAttribute("failed","Server Error...");				
			}
		}
		catch (Exception e) {
			attr.addFlashAttribute("failed",e.getMessage());
		}
		return "redirect:./dash";
	}

	@PostMapping("/withdraw")
	public String withdraw(@RequestParam Double amount, RedirectAttributes attr) {
		try {
			boolean isCredited = transService.withdraw(SecurityContextHolder.getContext().getAuthentication().getName(), amount);
			if(isCredited)
			{
				attr.addFlashAttribute("success","Money Debited...");
			}
			else {
				attr.addFlashAttribute("failed","Server Error...");				
			}
		}
		catch (Exception e) {
			attr.addFlashAttribute("failed",e.getMessage());
		}
		return "redirect:./dash";
	}

	@PostMapping("/transfer")
	public String transfer(@RequestParam Double amount, @RequestParam String toUsername, RedirectAttributes attr) {
		try {
			String fromUsername = SecurityContextHolder.getContext().getAuthentication().getName();
			boolean isCredited = transService.transferFund(toUsername, fromUsername, amount);
			if(isCredited)
			{
				attr.addFlashAttribute("success","Money Transfered Successfully...");
			}
			else {
				attr.addFlashAttribute("failed","Server Error...");				
			}
		}
		catch (Exception e) {
			attr.addFlashAttribute("failed",e.getMessage());
		}
		return "redirect:./dash";
	}

}