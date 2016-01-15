package com.neolynks.curator.util;

import java.util.Random;
import java.util.UUID;

/**
 * Created by nitesh.garg on Dec 27, 2015
 *
 * Mostly copied from:
 * http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
 * 
 */
public class RandomString {

	private static final char[] symbols;

	static {
		StringBuilder tmp = new StringBuilder();
		for (char ch = '0'; ch <= '9'; ++ch)
			tmp.append(ch);
/*		for (char ch = 'A'; ch <= 'Z'; ++ch)
			tmp.append(ch);*/
		symbols = tmp.toString().toCharArray();
	}

	private static final Random random = new Random();

	private final char[] buf;

	public RandomString(int length) {
		if (length < 1)
			throw new IllegalArgumentException("length < 1: " + length);
		buf = new char[length];
	}
	
	public static String nextCartId() {
        return UUID.randomUUID().toString();
	}

//    public static Long nextCartId() {
//        Random random = new Random(1000);
//        return Long.parseLong(String.valueOf(System.currentTimeMillis()) + String.valueOf(random.nextInt()));
//    }

    public String nextString() {
		for (int idx = 0; idx < buf.length; ++idx)
			buf[idx] = symbols[random.nextInt(symbols.length)];
		return new String(buf);
	}
}
