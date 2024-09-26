package com.kr.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kr.model.UserAccount;

@Repository
public interface IUserRepository extends JpaRepository<UserAccount, Integer> {
	Optional <UserAccount> findByUsername(String username);
}
