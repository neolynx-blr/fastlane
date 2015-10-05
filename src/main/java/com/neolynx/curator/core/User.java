package com.neolynx.curator.core;

import java.security.Principal;

import lombok.Data;

@Data
public class User implements Principal {
	
    private final Account accountDetail;
    
	/* (non-Javadoc)
	 * @see java.security.Principal#getName()
	 */
	@Override
	public String getName() {
		return this.getAccountDetail().getUserName();
	}

	public User(Account accountDetail) {
		super();
		this.accountDetail = accountDetail;
	}



}
