package com.neolynx.curator.db;

import io.dropwizard.hibernate.AbstractDAO;

import org.hibernate.SessionFactory;

import com.neolynx.curator.core.Account;

/**
 * Created by nitesh.garg on Oct 5, 2015
 */
public class AccountDAO extends AbstractDAO<Account> {

	public AccountDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public Account findByUserName(String userName) {
		return list(namedQuery("com.neolynx.curator.core.Account.findByUserName").setParameter("userName", userName))
				.get(0);
	}
}
