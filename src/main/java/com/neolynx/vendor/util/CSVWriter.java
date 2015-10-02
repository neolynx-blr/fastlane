package com.neolynx.vendor.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynx.common.model.Error;
import com.neolynx.curator.util.StringUtilsCustom;

/**
 * Created by nitesh.garg on 17-Sep-2015
 */
public class CSVWriter {

	static Logger LOGGER = LoggerFactory.getLogger(CSVWriter.class);
	private final CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(Constant.NEW_LINE_SEPARATOR);

	public List<Error> clearFileContents(String fileName, String[] fileHeader) {
		// TODO Find a better way
		return writeCSVRecords(fileName, new ArrayList<String[]>(), fileHeader);
	}

	public List<Error> writeLastSyncIdentifierRecord(String fileName, String lastSyncId) {

		List<String[]> records = new ArrayList<String[]>();
		records.add(new String[] { String.valueOf(lastSyncId), String.valueOf(System.currentTimeMillis()) });
		return writeCSVRecords(fileName, records, Constant.SYNC_FILE_HEADER);

	}

	public List<Error> writeLoadStatusRecords(String fileName, List<Long> successIds) {

		List<String[]> records = new ArrayList<String[]>();
		for (Long successId : successIds) {
			records.add(new String[] { String.valueOf(successId), String.valueOf(System.currentTimeMillis()) });
		}
		return writeCSVRecords(fileName, records, Constant.STATUS_FILE_HEADER);
	}

	public List<Error> writeInventoryRecords(String fileName, List<String[]> inventoryRecords) {
		return writeCSVRecords(fileName, inventoryRecords, Constant.INVENTORY_FILE_HEADER);
	}

	private List<Error> writeCSVRecords(String fileName, List<String[]> records, String[] fileHeader) {

		FileWriter fileWriter = null;
		CSVPrinter csvFilePrinter = null;

		//TODO Handle error codes/messages
		List<Error> errorResponse = new ArrayList<Error>();

		LOGGER.debug("Adding [{}] records to file [{}] using header [{}]", records.size(),
				StringUtilsCustom.extractFileName(fileName), fileHeader.toString());

		try {

			fileWriter = new FileWriter(fileName);
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
			csvFilePrinter.printRecord((Object[]) fileHeader);

			// Write a new student object list to the CSV file
			for (String[] record : records) {
				LOGGER.debug("Adding record  [{}] to the file [{}]", record.toString(),
						StringUtilsCustom.extractFileName(fileName));
				csvFilePrinter.printRecord((Object[]) record);
			}

			LOGGER.debug("Completed adding all records to the file.");

		} catch (IOException e) {
			LOGGER.debug("Error occured with message [{}} while adding records to file [{}]", e.getMessage(),
					StringUtilsCustom.extractFileName(fileName));
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
				csvFilePrinter.close();
			} catch (IOException e) {
				LOGGER.debug("Error occured with message [{}} while flushing/closing fileWriter/csvPrinter.",
						e.getMessage());
				e.printStackTrace();
			}
		}

		return errorResponse;

	}

}
