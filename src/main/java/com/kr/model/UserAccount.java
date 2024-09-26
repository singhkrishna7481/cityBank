package com.kr.model;

import java.util.List;
import java.util.Set;

import org.hibernate.annotations.SQLDelete;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "UserAccount_Tab")
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@SQLDelete(sql = "UPDATE Bank_User_Tab SET isactvie = false WHERE id = ?") // for soft deletion
public class UserAccount {

	@Id
	@SequenceGenerator(name = "g", initialValue = 100, allocationSize = 1)
	@GeneratedValue(generator = "g", strategy = GenerationType.IDENTITY)
	private Integer id;

	@NonNull
	@Column(name = "Name", length = 30)
	private String name;

	@NonNull
	@Column(name = "Username", length = 20, unique = true)
	private String username;
	@NonNull
	@Column(name = "Email", length = 40)
	private String email;
	@NonNull
	@Column(name = "Phone", length = 13)
	private Long phone;
	@NonNull
	@Column(name = "Password", length = 65)
	private String password;

	@NonNull
	@Column(name = "Balance")
	private Double balance = 0.0;

	@Column(name = "Active")
	private Boolean isactvie = true; // property for soft deletion i.e record won't be deleted permanently

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "SECURITY_ROLES", joinColumns = @JoinColumn(name = "USER_ID"))
	@Column(name = "role",updatable = true, length = 30)
	private Set<String> roles;

	@OneToMany(targetEntity = TransactionsHistory.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "account")
	private List<TransactionsHistory> transactions;

	@Override
	public String toString() {
		return "UserAccount [id=" + id + ", name=" + name + ", username=" + username + ", email=" + email + ", phone="
				+ phone + ", password=" + password + ", balance=" + balance + ", isactvie=" + isactvie + "]";
	}

}