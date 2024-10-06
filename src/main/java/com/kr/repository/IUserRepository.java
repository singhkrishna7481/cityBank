package com.kr.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kr.model.UserAccount;

import jakarta.transaction.Transactional;

@Repository
public interface IUserRepository extends JpaRepository<UserAccount, Integer> {
	Optional <UserAccount> findByUsername(String username);
	
	// Custom query method for password updation
		@Query(value = "UPDATE  UserAccount SET password=?1 WHERE username=?2")
		@Modifying
		@Transactional
		public int updateUserPassword(String password, String username);
}
