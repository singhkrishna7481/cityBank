package com.kr.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.kr.model.TransactionsHistory;
import com.kr.model.UserAccount;
import com.kr.service.ITransactionService;
import com.kr.service.IUserService;

@Controller
@RequestMapping("bank/")
public class AppController {

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
		model.addAttribute("user", user);
		return "dash";
	}

	@GetMapping("register")
	public String reg(@ModelAttribute UserAccount user) {
		return "register";
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
			return "redirect:./details";
		}
		else {
			model.addAttribute("error", "User already Exists.");
			return "register";
		}
	}
	@GetMapping("details")
	public String details() {
		return "regDetails";
	}
	@GetMapping("transactions")
	public String transactions(Model model) {
		Integer id = service.getUser(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
		List<TransactionsHistory> transactions = service.getTransactionHistory(id);
		model.addAttribute("transactions", transactions);
		model.addAttribute("user", service.getUserById(id));

		return "trans";
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
