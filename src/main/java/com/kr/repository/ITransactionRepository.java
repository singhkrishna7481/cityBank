package com.kr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kr.model.TransactionsHistory;

@Repository
public interface ITransactionRepository extends JpaRepository<TransactionsHistory, Integer> {

}
