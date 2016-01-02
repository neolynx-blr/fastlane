package com.neolynks.util;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RandomIdGenerator {

	private Random random; 
	private ConcurrentMap<Long, Boolean> idMap = new ConcurrentHashMap<Long, Boolean>();

	private static final class SingletonHolder {
		static final RandomIdGenerator idGenerator = new RandomIdGenerator();
	}

	private RandomIdGenerator() {
		random = new Random();
	}

	public long generateId() {
		long id = System.currentTimeMillis();
		id = Long.parseLong(String.valueOf(id) + String.valueOf(random.nextInt(1000)));

		do {
			id = Long.parseLong(String.valueOf(id) + String.valueOf(random.nextInt(1000)));
		}while(!idMap.putIfAbsent(id, true));

		// To do
		// map need to be cleared on regular basis
		return id;
	}

	public static RandomIdGenerator getInstance() {
		return SingletonHolder.idGenerator;
	}
}