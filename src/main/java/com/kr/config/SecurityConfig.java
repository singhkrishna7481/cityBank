package com.kr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.kr.service.UserServiceImpl;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(req -> {
			req.requestMatchers("/bank/register",
													"/bank/forgot",
													"/bank/send",
													"/bank/verify",
													"/bank/reset",
													"/bank/details").permitAll().anyRequest().authenticated();
		});

		http.formLogin(form -> {
			form.loginPage("/bank/home").loginProcessingUrl("/bank/login").failureUrl("/bank/home?error")
					.defaultSuccessUrl("/bank/dash", true).permitAll();
		});

		http.logout(logout -> logout.invalidateHttpSession(true).clearAuthentication(true)
				.logoutRequestMatcher(new AntPathRequestMatcher("/bank/logout")).logoutSuccessUrl("/bank/home")
				.permitAll());
		http.headers(header -> header.frameOptions(frameOptions -> frameOptions.sameOrigin()));
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setPasswordEncoder(passwordEncoder());
		provider.setUserDetailsService(userDetailsService());
		return provider;
	}

	@Bean
	UserDetailsService userDetailsService() {
		return new UserServiceImpl();
	}

}
