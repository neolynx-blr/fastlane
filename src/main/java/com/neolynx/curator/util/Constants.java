package com.neolynx.curator.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by nitesh.garg on 10-Sep-2015
 */
public class Constants {

	public static final String ITEM_CODE_SEPARATOR = ",";
	public static final String CACHE_KEY_SEPARATOR_STRING = "-";

	public static final String COMMA_SEPARATOR = ",";
	public static final String HYPHEN_SEPARATOR = "-";

	public static final Long AMAZON_VENDOR_ID = 281L;
	public static final String ITEM_ID = "8901030215483";
	public static final String ENDPOINT = "webservices.amazon.in";

	public static final String AMAZON_API_SEARCH_INDEX = "All";
	public static final String AMAZON_API_VERSION = "2013-08-01";
	public static final String AMAZON_API_ASSOCIATE_TAG = "neol-21";
	public static final String AMAZON_API_RESPONSE_GROUP = "Medium";
	public static final String AMAZON_API_LOOKUP_OPERATION = "ItemLookup";
	public static final String AMAZON_API_SERVICE_NAME = "AWSECommerceService";
	
	/*
	 * Your AWS Access Key ID, as taken from the AWS Your Account page.
	 */
	public static final String AWS_ACCESS_KEY_ID = "AKIAIQNAUOEG2RGNCEZA";

	/*
	 * Your AWS Secret Key corresponding to the above ID, as taken from the AWS
	 * Your Account page.
	 */
	public static final String AWS_SECRET_KEY = "7WJBK+curHjbEQs9pjUeOP/+aTBY2Hvl4q5DZjw8";

	public static String getBarcodeType(String barcodeValue) {
		return StringUtils.length(barcodeValue) != 13 ? "UPC" : "EAN";
	}

}
