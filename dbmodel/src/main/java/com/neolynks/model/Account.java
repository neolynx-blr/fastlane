package com.neolynks.model;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by nitesh.garg on Oct 5, 2015
 */

@Data
@Entity
@Table(name = "account")
@NamedQueries({
		@NamedQuery(name = "com.neolynks.model.Account.findByUserName", query = "SELECT p FROM Account p where userName = :userName")})
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "user_name", nullable = false)
	private String userName;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "role", nullable = false)
	private String role;

}
