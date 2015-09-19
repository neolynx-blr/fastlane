package com.neolynx.curator.util;

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

	public static String[] mapCSVRecordsToArray(CSVRecord record, String[] headerList) {

		String[] response = new String[] {};
		int count = 0;

		for (String headerName : headerList) {
			response[count++] = record.get(headerName);
		}

		return response;

	}

}
