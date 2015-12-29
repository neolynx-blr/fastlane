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

	private final Set<String> closedCartIds = new HashSet<String>();

	private final Set<String> syncedCartIds = new HashSet<String>();
	private final Set<String> updatedCartIds = new HashSet<String>();

	private static final CartLogistics instance = new CartLogistics();

	protected CartLogistics() {
	}

	public static CartLogistics getInstance() {
		return instance;
	}

}
