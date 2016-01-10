package com.neolynks.curator.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Data;

/**
 * Created by nitesh.garg on Oct 5, 2015
 */

@Data
@Entity
@Table(name = "account")
@NamedQueries({
		@NamedQuery(name = "com.neolynks.curator.core.Account.findByUserName", query = "SELECT p FROM Account p where userName = :userName")})
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
