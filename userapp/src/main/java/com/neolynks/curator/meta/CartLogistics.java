package com.neolynks.curator.meta;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

/**
 * Created by nitesh.garg on Dec 29, 2015
 *
 */

@Data
public class CartLogistics {

	private final Set<Long> closedCartIds = new HashSet<Long>();

	private final Set<Long> syncedCartIds = new HashSet<Long>();
	private final Set<Long> updatedCartIds = new HashSet<Long>();

	private static final CartLogistics instance = new CartLogistics();

	protected CartLogistics() {
	}

	public static CartLogistics getInstance() {
		return instance;
	}

}
