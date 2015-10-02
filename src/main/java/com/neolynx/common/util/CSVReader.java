package com.neolynx.common.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynx.curator.util.StringUtilsCustom;

/**
 * Created by nitesh.garg on 14-Sep-2015
 */
public class CSVReader {

	static Logger LOGGER = LoggerFactory.getLogger(CSVReader.class);

	public String getLastSyncIdentifier(String fileName) {
		List<CSVRecord> csvRecords = readCSVRecords(fileName, Constant.STATUS_FILE_HEADER);
		if (CollectionUtils.isNotEmpty(csvRecords)) {
			return csvRecords.get(0).toString();
		}
		return null;
	}

	public List<CSVRecord> getLastSyncSuccessIds(String fileName) {
		return readCSVRecords(fileName, Constant.SYNC_FILE_HEADER);
	}

	public List<CSVRecord> getAllPendingInventoryRecords(String fileName) {
		return readCSVRecords(fileName, Constant.INVENTORY_FILE_HEADER);
	}

	private List<CSVRecord> readCSVRecords(String fileName, String[] fileHeader) {

		FileReader fileReader = null;
		CSVParser csvFileParser = null;
		List<CSVRecord> csvRecords = null;

		// Create the CSVFormat object with the header mapping
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(fileHeader);

		try {

			// LOGGER.debug("About to openup the file [{}]", fileName);

			fileReader = new FileReader(fileName);
			csvFileParser = new CSVParser(fileReader, csvFileFormat);
			csvRecords = csvFileParser.getRecords();

			if (CollectionUtils.isNotEmpty(csvRecords)) {
				// Remove the header row
				csvRecords.remove(0);
			}

			if (CollectionUtils.isNotEmpty(csvRecords)) {
				LOGGER.debug("While reading file [{}], found [{}] records, and first record found is:[{}]",
						StringUtilsCustom.extractFileName(fileName), csvRecords.size(), csvRecords.get(0).toString());
			} else {
				LOGGER.debug("No record found while reading file [{}]", StringUtilsCustom.extractFileName(fileName));
			}

		} catch (FileNotFoundException e) {
			LOGGER.error("Unable to find any file with name [{}]", StringUtilsCustom.extractFileName(fileName));
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("Exception with message [{}] occurred while parsing the file [{}]", e.getMessage(),
					StringUtilsCustom.extractFileName(fileName));
			e.printStackTrace();
		} finally {
			try {
				fileReader.close();
				csvFileParser.close();
			} catch (IOException e) {
				LOGGER.error("Error while closing fileReader/csvFileParser for file [{}]",
						StringUtilsCustom.extractFileName(fileName));
				e.printStackTrace();
			}
		}

		return csvRecords;

	}

}
