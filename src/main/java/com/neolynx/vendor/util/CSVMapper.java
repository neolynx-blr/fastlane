package com.neolynx.vendor.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

/**
 * Created by nitesh.garg on 19-Sep-2015
 */
public class CSVMapper {

	public static List<String[]> mapCSVRecordsToArrayList(List<CSVRecord> records, String[] headerList) {

		List<String[]> response = new ArrayList<String[]>();

		for (CSVRecord record : records) {
			response.add(mapCSVRecordsToArray(record, headerList));
		}

		return response;

	}

	// TODO
	public static String[] mapCSVRecordsToArray(CSVRecord record, String[] headerList) {

		String[] response = new String[] {};
		List<String> responseList = new ArrayList<String>();
		int count = 0;

		System.out.println("Loading record ["+record.toString()+"] against headerList ["+headerList.toString()+"]");
		
		for (String headerName : headerList) {
			System.out.println("Loading for [" + headerName + "]" + " value [" + record.get(headerName) + "]");
			responseList.add(new String(record.get(headerName)));
		}
		
		response = new String [responseList.size()];
		for(String str : responseList) {
			if(str == null || str.trim().equalsIgnoreCase("null")) {
				response[count] = null;
			} else {
				response[count] = str;
			}
			count++;
		}
		return response;

	}

}
