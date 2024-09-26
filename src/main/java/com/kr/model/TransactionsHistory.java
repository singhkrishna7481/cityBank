package com.kr.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@Table(name = "User_Transactions_Tab")
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class TransactionsHistory {
	@Id
	@SequenceGenerator(name = "g1", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "g1", strategy = GenerationType.IDENTITY)
	private Integer transId;

	@NonNull
	@Column(name = "Type", length = 10)
	private String transType;
	@NonNull
	@Column(name = "Amount")
	private Double transAmt;
	@NonNull
	@Column(name = "Time", length = 20)
	private String time;
	@NonNull
	@Column(name = "Date", length = 10)
	private String date;

	@ManyToOne(targetEntity = UserAccount.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "User_Account", referencedColumnName = "id")
	private UserAccount account;
}
