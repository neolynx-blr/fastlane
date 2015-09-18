package com.neolynx.curator.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nitesh.garg on 14-Sep-2015
 */
public class CSVReader {

	static Logger LOGGER = LoggerFactory.getLogger(CSVReader.class);

	private static final String[] FILE_HEADER_MAPPING = { "id", "item_code", "version_id", "name", "description",
			"tag_line", "barcode", "mrp", "price", "image_json", "discount_type", "discount_value", "last_modified_on" };

	public List<CSVRecord> getAllPendingRecords(String fileName) {

		FileReader fileReader = null;
		CSVParser csvFileParser = null;
		List<CSVRecord> csvRecords = null;

		// Create the CSVFormat object with the header mapping
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER_MAPPING);

		try {

			LOGGER.debug("About to openup the file [{}]", fileName);
			
			fileReader = new FileReader(fileName);
			csvFileParser = new CSVParser(fileReader, csvFileFormat);
			csvRecords = csvFileParser.getRecords();
			LOGGER.debug("Read the file [{}] for [{}] records", fileName, csvRecords.size());
			
			if(csvRecords.size()>1) {
				LOGGER.debug("First record:[{}]", csvRecords.get(1).toString());
			}

		} catch (FileNotFoundException e) {
			LOGGER.error("Unable to find any file with name [{}]", fileName);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("Exception with message [{}] occurred while parsing the file [{}]", e.getMessage(), fileName);
			e.printStackTrace();
		} finally {
			try {
				fileReader.close();
				csvFileParser.close();
			} catch (IOException e) {
				LOGGER.error("Error while closing fileReader/csvFileParser for file [{}]", fileName);
				e.printStackTrace();
			}
		}

		return csvRecords;

	}

}
