/**
 * 
 */
package com.neolynx.common.util;

/**
 * Created by nitesh.garg on Nov 18, 2015
 *
 */
public class StringUtilsCustom {

	public static Boolean isChanged(String oldValue, String newValue) {
		
		if(oldValue == null && newValue == null)
			return false;
		
		return !(newValue.equals(oldValue));
	}

}
