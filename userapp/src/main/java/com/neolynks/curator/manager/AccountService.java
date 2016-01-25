package com.neolynks.curator.manager;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.common.base.Optional;
import com.neolynks.curator.core.Account;

/**
 * Created by nitesh.garg on Oct 5, 2015
 */
@Slf4j
public class AccountService {

	private final SessionFactory sessionFactory;

	public AccountService(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	public Optional<Account> getAccountDetails(String userName) {

		Session session = this.sessionFactory.openSession();

		Query query = session.createSQLQuery("select acc.* from account acc where acc.user_name = :userName")
				.addEntity("account", Account.class).setParameter("userName", userName);

		List<Account> accountDetails = query.list();
		
		session.close();
		
		if (CollectionUtils.isNotEmpty(accountDetails)) {
			return Optional.of(accountDetails.get(0));
		}
		
		return Optional.absent();

	}

}
