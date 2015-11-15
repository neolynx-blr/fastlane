/**
 * 
 */
package com.neolynx.curator.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by nitesh.garg on Nov 15, 2015
 *
 */
public class EanDataUtil {

	public static String getAttributeValue(String dataJSON, String attribute) {

		try {
			return StringUtils.substringBefore(
					StringUtils.substringAfter(StringUtils.substringAfterLast(dataJSON, attribute + "\""), "\""), "\"");
		} catch (Exception e) {
			return null;
		}

	}

}
