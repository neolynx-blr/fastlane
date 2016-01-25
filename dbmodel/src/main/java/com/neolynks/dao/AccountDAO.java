package com.neolynks.dao;

import com.neolynks.model.Account;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

/**
 * Created by nitesh.garg on Oct 5, 2015
 */
public class AccountDAO extends AbstractDAO<Account> {

	public AccountDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public Account findByUserName(String userName) {
		return list(namedQuery("com.neolynks.model.Account.findByUserName").setParameter("userName", userName))
				.get(0);
	}
}
