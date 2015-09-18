package com.neolynx.curator.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nitesh.garg on 17-Sep-2015
 */
public class CSVWriter {

	static Logger LOGGER = LoggerFactory.getLogger(CSVWriter.class);

	private static final String NEW_LINE_SEPARATOR = "\n";

	private static final String[] FILE_HEADER_MAPPING = { "id", "timestamp" };

	FileWriter fileWriter = null;
	CSVPrinter csvFilePrinter = null;

	CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
	
	public void createNewEmptyFile(String fileName) {
		
		try {
			fileWriter = new FileWriter(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
				e.printStackTrace();
			}
		}

	}

	public void setPostStatus(String fileName, List<Long> successIds) {

		try {

			// initialize FileWriter object
			fileWriter = new FileWriter(fileName);

			// initialize CSVPrinter object
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

			// Create CSV file header
			csvFilePrinter.printRecord((Object[]) FILE_HEADER_MAPPING);

			// Write a new student object list to the CSV file
			for (Long successId : successIds) {
				String[] studentDataRecord = new String[] { String.valueOf(successId),
						String.valueOf(System.currentTimeMillis()) };
				LOGGER.debug("About to add data:: [{}]", studentDataRecord.toString());
				csvFilePrinter.printRecord((Object[]) studentDataRecord);
			}

			System.out.println("CSV file was created successfully !!!");

		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
				csvFilePrinter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
				e.printStackTrace();
			}
		}
	}

}
