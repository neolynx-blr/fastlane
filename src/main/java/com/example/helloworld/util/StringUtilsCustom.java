package com.example.helloworld.util;

import java.util.List;

import org.apache.commons.lang3.text.StrTokenizer;

/**
 * Created by nitesh.garg on 10-Sep-2015
 */
public class StringUtilsCustom {

	public static List<String> convertStringToTokens(String data) {
		return new StrTokenizer(data, ",").getTokenList();
	}
	
}

