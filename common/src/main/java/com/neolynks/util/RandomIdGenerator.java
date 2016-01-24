package com.neolynks.util;

import lombok.Getter;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RandomIdGenerator {

	private Random random; 

    @Getter
	private static final RandomIdGenerator instance = new RandomIdGenerator();

	private RandomIdGenerator() {
		random = new Random();
	}

	public long generateLongId() {
		long id = System.currentTimeMillis();
		id = Long.parseLong(String.valueOf(id) + String.valueOf(random.nextInt(1000)));
		return id;
	}

    public String generateStringId(){
        return UUID.randomUUID().toString();
    }
}