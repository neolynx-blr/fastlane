package com.neolynks.util;

import java.util.Random;

public class RandomIdGenerator {

	private Random random; 

	private static final class SingletonHolder {
		static final RandomIdGenerator idGenerator = new RandomIdGenerator();
	}

	private RandomIdGenerator() {
		random = new Random();
	}

	public synchronized long generateId() {
		long id = System.currentTimeMillis();
		id = Long.parseLong(String.valueOf(id) + String.valueOf(random.nextInt(1000)));
		return id;
	}

	public static RandomIdGenerator getInstance() {
		return SingletonHolder.idGenerator;
	}
}