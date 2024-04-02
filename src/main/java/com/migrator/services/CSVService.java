/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.migrator.constants.DatabaseConstants;
import com.migrator.constants.MigratorConstants;
import com.migrator.database.CSVFiles;
import com.migrator.database.FileColumn;
import com.migrator.database.MigratedRecord;
import com.migrator.database.TemplateDefinition;
import com.migrator.database.TemplateDetails;
import com.migrator.database.specifications.MigratorSpecifications;
import com.migrator.repositories.CsvFilesRepo;
import com.migrator.repositories.FileColumnRepo;
import com.migrator.repositories.MigratedRecordRepo;
import com.migrator.repositories.TemplateDefinitionRepo;
import com.migrator.repositories.TemplateDetailsRepo;

/**
 * The Class CSVService.
 */
@Service
public class CSVService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CSVService.class);
	
	/** The Constant JUNK_CHARACTER. */
	private static final String JUNK_CHARACTER = "ï»¿";
	
	/** The csv files repo. */
	@Autowired
	CsvFilesRepo csvFilesRepo;
	
	/** The template definition repo. */
	@Autowired
	TemplateDefinitionRepo templateDefinitionRepo;
	
	/** The template details repo. */
	@Autowired
	TemplateDetailsRepo templateDetailsRepo;
	
	/** The jms template. */
	@Autowired
	JmsTemplate jmsTemplate;
	
	/** The migrated record repo. */
	@Autowired
	MigratedRecordRepo migratedRecordRepo;
	
	/** The alfresco service. */
	@Autowired
	AlfrescoService alfrescoService;
	
	/** The file column repo. */
	@Autowired
	FileColumnRepo fileColumnRepo;
	
	/**
	 * Gets the csv headers.
	 *
	 * @param inputStream the input stream
	 * @return the csv headers
	 */
	public JSONArray getCsvHeaders(final InputStream inputStream) {
		JSONArray columnsJsonArray = new JSONArray();
		int count =0;
		String line = StringUtils.EMPTY;
		try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))){
			while((line =br.readLine())!=null) {
				if(count==0) {
					LOGGER.info("line {} {}",count,line);
					final String[] headerSplit = line.split(MigratorConstants.COMMA,-1);
					columnsJsonArray = getcolumnsArray(headerSplit);
					LOGGER.info("columns {}",columnsJsonArray);
				}
				count+=1;
			}
		} catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method getCsvHeaders {}",ioException);
		}
		return columnsJsonArray;
	}
	
	/**
	 * Gets the columns array.
	 *
	 * @param headerSplit the header split
	 * @return the columns array
	 */
	private JSONArray getcolumnsArray(final String[] headerSplit) {
		final JSONArray columnsJsonArray = new JSONArray();
		for(int index=0;index<headerSplit.length;index++) {
			final JSONObject columnJson= new JSONObject();
			columnJson.put(MigratorConstants.KEY_COLUMN_NAME, headerSplit[index]);
			columnsJsonArray.put(columnJson);
		}
		return columnsJsonArray;
	}
	
	/**
	 * Count CSV records.
	 *
	 * @param inputStream the input stream
	 * @param hasincludecolumn 
	 * @return the long
	 */
	public long countCSVRecords(final InputStream inputStream, final boolean hasincludecolumn) {
		long recordCount=0;
//		final Map<Integer, Object> headers = new HashMap<>();
//		String line = StringUtils.EMPTY;
		CSVParser csvParser = null;
		try {
			final Builder builder = CSVFormat.Builder.create().setHeader().setHeader().setDelimiter(",").setAllowMissingColumnNames(true).setTrim(true).setIgnoreEmptyLines(true).setAutoFlush(true);
			csvParser = new CSVParser(new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8), builder.build());
			final List<CSVRecord> records = csvParser.getRecords();
			LOGGER.info("total records in csv {}",records.size());
			for(final CSVRecord record : records) {
				final Map<String, Object> recordMap = new HashMap<>();
				recordMap.putAll(record.toMap());
				LOGGER.info("record map {}",recordMap);
				final String include = recordMap.containsKey(MigratorConstants.INCLUDE)?recordMap.get(MigratorConstants.INCLUDE)+"":recordMap.get(MigratorConstants.INCLUDE_CAMEL)+"";
				if(hasincludecolumn==true) {
					if(MigratorConstants.YES.equalsIgnoreCase(include)) {
						final String pdfYesNo = recordMap.get(MigratorConstants.COLUMN_PDF_YES_NO)+"";
						if(MigratorConstants.YES.equalsIgnoreCase(pdfYesNo)) {
							recordCount+=1;
						}
						final String nativeYesNo = recordMap.get(MigratorConstants.COLUMN_NATIVE_FILE_YES_NO)+"";
						if(MigratorConstants.YES.equalsIgnoreCase(nativeYesNo)) {
							recordCount+=1;
						}
					}
				}else {
					recordCount+=1;
				}
			}
//			recordCount = records.size();
		} catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method getCsvHeaders {}",ioException);
		}finally {
				try {
					if(null!=csvParser) {
						csvParser.close();
					}
				} catch (IOException ioException2) {
					LOGGER.error("ioException2 occured while executing method getCsvHeaders {}",ioException2);
				}
		}
		
//		try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))){
//			while((line =br.readLine())!=null) {
//				if(StringUtils.isNotBlank(line) && StringUtils.isNotEmpty(line)) {
//					if(count==0) {
//						headers.putAll(identifyCsvHeader(line));
//					}else {
//						
//						final Map<String, Object> recordMap = new HashMap<>();
//						recordMap.putAll(getRecordAsMap(line, headers));
//						final String include = recordMap.containsKey(MigratorConstants.INCLUDE)?recordMap.get(MigratorConstants.INCLUDE)+"":recordMap.get(MigratorConstants.INCLUDE_CAMEL)+"";
//						if(MigratorConstants.YES.equalsIgnoreCase(include)) {
//							final String pdfYesNo = recordMap.get(MigratorConstants.COLUMN_PDF_YES_NO)+"";
//							if(MigratorConstants.YES.equalsIgnoreCase(pdfYesNo)) {
//								recordCount+=1;
//							}
//							final String nativeYesNo = recordMap.get(MigratorConstants.COLUMN_NATIVE_FILE_YES_NO)+"";
//							if(MigratorConstants.YES.equalsIgnoreCase(nativeYesNo)) {
//								recordCount+=1;
//							}
//						}
//					}
//				}
//				count+=1;
//			}
//		} catch (IOException ioException) {
//			LOGGER.error("ioException occured while executing method getCsvHeaders {}",ioException);
//		}
		return recordCount;
	}
	
	/**
	 * Save csv details.
	 *
	 * @param csvFileName the csv file name
	 * @param pdfDestination the pdf destination
	 * @param nativedestination the nativedestination
	 * @param pdfLocalLocation the pdf local location
	 * @param nativeLocalLocation the native local location
	 * @param totalfailed the totalfailed
	 * @param totalSuccess the total success
	 * @param totalRecords the total records
	 * @param islatest the islatest
	 * @param metadtatemplateid the metadtatemplateid
	 * @param inputStream the input stream
	 * @param nativemetadtatemplateid the nativemetadtatemplateid
	 * @param sourcelocation 
	 * @return the JSON object
	 */
	public JSONObject saveCsvDetails(final String csvFileName,final String pdfDestination,final String nativedestination,final String pdfLocalLocation,
			final String nativeLocalLocation,final long totalfailed,final long totalSuccess,final long totalRecords, final boolean islatest, 
			final long metadtatemplateid,final InputStream inputStream, final long nativemetadtatemplateid, final String sourcelocation) {
		JSONObject responseJson = new JSONObject();
		final CSVFiles csvFiles = new CSVFiles();
		csvFiles.setCsvname(csvFileName);
		csvFiles.setNativedestination(nativedestination);
		csvFiles.setPdfDestination(pdfDestination);
		csvFiles.setPdflocation(pdfLocalLocation);
		csvFiles.setNativelocation(nativeLocalLocation);
		csvFiles.setTotalfailed(0);
		csvFiles.setTotalmigrated(0);
		csvFiles.setTotalrecords(totalRecords);
		csvFiles.setTemplateid(metadtatemplateid);
		csvFiles.setNativetemplateid(nativemetadtatemplateid);
		csvFiles.setSourcelocation(sourcelocation);
		final CSVFiles savedCSV = csvFilesRepo.save(csvFiles);
		if(null!=savedCSV) {
			LOGGER.info("csv files details {}",savedCSV.toString());
			responseJson = new JSONObject(savedCSV.toString());
//			sendUploadMEssage(responseJson, inputStream,islatest,pdfDestination,nativedestination);
		}else {
			responseJson.put(DatabaseConstants.UNIQUE_ID, StringUtils.EMPTY);
			responseJson.put(DatabaseConstants.CSV_FILE_NAME, StringUtils.EMPTY);
			responseJson.put(DatabaseConstants.TOTAL_RECORDS, StringUtils.EMPTY);
			responseJson.put(DatabaseConstants.TOTAL_MIGRATED, StringUtils.EMPTY);
			responseJson.put(DatabaseConstants.TOTAL_FAILED, StringUtils.EMPTY);
			responseJson.put(DatabaseConstants.PDF_DESTINATION, StringUtils.EMPTY);
			responseJson.put(DatabaseConstants.NATIVE_DESTINATION, StringUtils.EMPTY);
			responseJson.put(DatabaseConstants.PDF_LOCATION, StringUtils.EMPTY);
			responseJson.put(DatabaseConstants.NATIVE_FILE_LOCATION, StringUtils.EMPTY);
			responseJson.put(DatabaseConstants.TEMPLATE_ID, StringUtils.EMPTY);
			responseJson.put(DatabaseConstants.NATIVE_TEMPLATE_ID, StringUtils.EMPTY);
			responseJson.put(DatabaseConstants.SOURCE_LOCATION, StringUtils.EMPTY);
		}
		return responseJson;
	}
	
	/**
	 * Send upload message.
	 *
	 * @param savedCSVFile the saved CSV file
	 * @param inputStream the input stream
	 * @param islatest the islatest
	 * @param pdfDestination the pdf destination
	 * @param nativedestination the nativedestination
	 * @param sourcelocation 
	 * @param hasincludecolumn 
	 */
	public void sendUploadMessage(final JSONObject savedCSVFile, final InputStream inputStream, final boolean islatest, final String pdfDestination,
			final String nativedestination, final String sourcelocation, final boolean hasincludecolumn) {
		final Map<String, String> configuredColumns = new HashMap<>();
		final Map<String, String> nativeConfiguredColumns = new HashMap<>();
		final long templateId = savedCSVFile.getLong(DatabaseConstants.TEMPLATE_ID);
		final long nativeTemplateId = savedCSVFile.getLong(DatabaseConstants.NATIVE_TEMPLATE_ID);
		final TemplateDefinition templateDefinition = templateDefinitionRepo.findBytemplateid(templateId);
		final TemplateDefinition nativeTemplateDefinition = templateDefinitionRepo.findBytemplateid(nativeTemplateId);
		if(null!=templateDefinition) {
			final TemplateDefinition queryTemplateDetails = new TemplateDefinition();
			final TemplateDefinition queryNativeTemplateDetails = new TemplateDefinition();
			queryTemplateDetails.setTemplateid(templateDefinition.getTemplateid());
			queryNativeTemplateDetails.setTemplateid(nativeTemplateDefinition.getTemplateid());
			final List<TemplateDetails> templateDetails = templateDetailsRepo.findAllByTemplatedefinition(queryTemplateDetails);
			final List<TemplateDetails> nativeTemplateDetails = templateDetailsRepo.findAllByTemplatedefinition(queryNativeTemplateDetails);
			final String multiValuedProps = getMultivaluedProperties(templateDetails, true);
			final String nativemultiValuedProps = getMultivaluedProperties(nativeTemplateDetails, true);
			LOGGER.info("multivalued properties {}",multiValuedProps);
			LOGGER.info("native multivalued properties {}",nativemultiValuedProps);
			configuredColumns.putAll(identifyConfiguredColumns(templateDetails));
			nativeConfiguredColumns.putAll(identifyConfiguredColumns(nativeTemplateDetails));
			LOGGER.info("configured columns {}",configuredColumns);
			LOGGER.info("configured native columns {}",nativeConfiguredColumns);
			readCsv(inputStream,configuredColumns,multiValuedProps,templateDefinition,savedCSVFile,islatest,nativeConfiguredColumns,nativeTemplateDefinition,pdfDestination,nativedestination,sourcelocation,hasincludecolumn);
		}
		
	}
	
	/**
	 * Read csv.
	 *
	 * @param inputStream the input stream
	 * @param configuredColumns the configured columns
	 * @param multiValuedProps the multi valued props
	 * @param templateDefinition the template definition
	 * @param savedCSVFile the saved CSV file
	 * @param islatest the islatest
	 * @param nativeConfiguredColumns the native configured columns
	 * @param nativeTemplateDefinition the native template definition
	 * @param pdfDestination the pdf destination
	 * @param nativedestination the nativedestination
	 * @param sourcelocation 
	 * @param hasincludecolumn 
	 */
	private void readCsv(final InputStream inputStream, final Map<String, String> configuredColumns, final String multiValuedProps, final TemplateDefinition templateDefinition, final JSONObject savedCSVFile, final boolean islatest, final Map<String, String> nativeConfiguredColumns, final TemplateDefinition nativeTemplateDefinition, final String pdfDestination, final String nativedestination, final String sourcelocation, final boolean hasincludecolumn) {
		JSONObject response = new JSONObject();
//		long count =0;
//		String line = StringUtils.EMPTY;
//		final Map<Integer, Object> headers = new HashMap<>();
		
		CSVParser csvParser = null;
		try {
			final Builder builder = CSVFormat.Builder.create().setHeader().setDelimiter(",").setAllowMissingColumnNames(true).setTrim(true).setIgnoreEmptyLines(true).setAutoFlush(true);
			csvParser = new CSVParser(new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8), builder.build());
			final List<CSVRecord> records = csvParser.getRecords();
			final LocalDateTime startdate = LocalDateTime.now();
			for(final CSVRecord record : records) {
				final Map<String, Object> recordMap = new HashMap<>();
				recordMap.putAll(record.toMap());
				final Map<String, Object> alfrescoProperties = new HashMap<>();
				final String include = recordMap.containsKey(MigratorConstants.INCLUDE)?recordMap.get(MigratorConstants.INCLUDE)+"":recordMap.get(MigratorConstants.INCLUDE_CAMEL)+"";
				LOGGER.info("has include column {}",hasincludecolumn);
				if(hasincludecolumn) {
					if(MigratorConstants.YES.equalsIgnoreCase(include)) {
						final String pdfYesNo = recordMap.get(MigratorConstants.COLUMN_PDF_YES_NO)+"";
						if(MigratorConstants.YES.equalsIgnoreCase(pdfYesNo)) {
							final FileColumn fileColumn = fileColumnRepo.findBytemplatedefinition(templateDefinition);
							response = new JSONObject();
							LOGGER.info("csv record {}",recordMap);
							LOGGER.info("pdf file column name {}",fileColumn.getColumnname());
							alfrescoProperties.clear();
							alfrescoProperties.putAll(proccessAlfrescoProperties(recordMap, configuredColumns));
							response.put(MigratorConstants.KEY_PROPERTIES, new JSONObject(alfrescoProperties));
							response.put(MigratorConstants.KEY_CSV_RECORD_NUMBER, record.getRecordNumber());
							response.put(MigratorConstants.KEY_FILE_TYPE, templateDefinition.getFiletypeqname());
							response.put(DatabaseConstants.UNIQUE_ID, savedCSVFile.getLong(DatabaseConstants.UNIQUE_ID));
							response.put(DatabaseConstants.IS_LATEST, islatest);
							response.put(DatabaseConstants.NATIVE_TEMPLATE_ID, savedCSVFile.getLong(DatabaseConstants.NATIVE_TEMPLATE_ID));
							response.put(DatabaseConstants.PDF_LOCATION, savedCSVFile.getString(DatabaseConstants.PDF_LOCATION));
							response.put(DatabaseConstants.NATIVE_FILE_LOCATION, savedCSVFile.getString(DatabaseConstants.NATIVE_FILE_LOCATION));
							response.put(DatabaseConstants.CSV_FILE_NAME, savedCSVFile.getString(DatabaseConstants.CSV_FILE_NAME));
							response.put(MigratorConstants.KEY_FILE_NAME, recordMap.get(fileColumn.getColumnname()));
							response.put(MigratorConstants.ISNATIVE, false);
							response.put(DatabaseConstants.PDF_DESTINATION, pdfDestination);
							response.put(DatabaseConstants.NATIVE_DESTINATION,nativedestination);
							response.put(DatabaseConstants.SOURCE_LOCATION, sourcelocation);
							response.put(DatabaseConstants.START_DATE_TIME, startdate);
							LOGGER.info("PDF json {}",response);
							jmsTemplate.convertAndSend(MigratorConstants.JMS_UPLOAD_MESSAGE, response.toString());
						}
						final String nativeYesNo = recordMap.get(MigratorConstants.COLUMN_NATIVE_FILE_YES_NO)+"";
						if(MigratorConstants.YES.equalsIgnoreCase(nativeYesNo)) {
							final FileColumn fileColumn = fileColumnRepo.findBytemplatedefinition(nativeTemplateDefinition);
							LOGGER.info("native file column name {}",fileColumn.getColumnname());
							response = new JSONObject();
							alfrescoProperties.clear();
							alfrescoProperties.putAll(proccessAlfrescoProperties(recordMap, nativeConfiguredColumns));
							response.put(MigratorConstants.KEY_PROPERTIES, new JSONObject(alfrescoProperties));
							response.put(MigratorConstants.KEY_CSV_RECORD_NUMBER, record.getRecordNumber());
							response.put(MigratorConstants.KEY_FILE_TYPE, nativeTemplateDefinition.getFiletypeqname());
							response.put(DatabaseConstants.UNIQUE_ID, savedCSVFile.getLong(DatabaseConstants.UNIQUE_ID));
							response.put(DatabaseConstants.IS_LATEST, islatest);
							response.put(DatabaseConstants.NATIVE_TEMPLATE_ID, savedCSVFile.getLong(DatabaseConstants.NATIVE_TEMPLATE_ID));
							response.put(DatabaseConstants.PDF_LOCATION, savedCSVFile.getString(DatabaseConstants.PDF_LOCATION));
							response.put(DatabaseConstants.NATIVE_FILE_LOCATION, savedCSVFile.getString(DatabaseConstants.NATIVE_FILE_LOCATION));
							response.put(DatabaseConstants.CSV_FILE_NAME, savedCSVFile.getString(DatabaseConstants.CSV_FILE_NAME));
							response.put(MigratorConstants.KEY_FILE_NAME, recordMap.get(fileColumn.getColumnname()));
							response.put(MigratorConstants.ISNATIVE, true);
							response.put(DatabaseConstants.NATIVE_DESTINATION,nativedestination);
							response.put(DatabaseConstants.SOURCE_LOCATION, sourcelocation);
							response.put(DatabaseConstants.START_DATE_TIME, startdate);
							LOGGER.info("Native json {}",response);
							jmsTemplate.convertAndSend(MigratorConstants.JMS_UPLOAD_MESSAGE, response.toString());
						}
					}
				}else {
					alfrescoProperties.clear();
					final FileColumn fileColumn = fileColumnRepo.findBytemplatedefinition(templateDefinition);
					LOGGER.info("file column name {}",fileColumn.getColumnname());
					alfrescoProperties.putAll(proccessAlfrescoProperties(recordMap, configuredColumns));
					response.put(MigratorConstants.KEY_PROPERTIES, new JSONObject(alfrescoProperties));
					response.put(MigratorConstants.KEY_CSV_RECORD_NUMBER, record.getRecordNumber());
					response.put(MigratorConstants.KEY_FILE_TYPE, templateDefinition.getFiletypeqname());
					response.put(DatabaseConstants.UNIQUE_ID, savedCSVFile.getLong(DatabaseConstants.UNIQUE_ID));
					response.put(DatabaseConstants.IS_LATEST, islatest);
					response.put(DatabaseConstants.NATIVE_TEMPLATE_ID, savedCSVFile.getLong(DatabaseConstants.NATIVE_TEMPLATE_ID));
					response.put(DatabaseConstants.PDF_LOCATION, savedCSVFile.getString(DatabaseConstants.PDF_LOCATION));
					response.put(DatabaseConstants.NATIVE_FILE_LOCATION, savedCSVFile.getString(DatabaseConstants.NATIVE_FILE_LOCATION));
					response.put(DatabaseConstants.CSV_FILE_NAME, savedCSVFile.getString(DatabaseConstants.CSV_FILE_NAME));
					response.put(MigratorConstants.KEY_FILE_NAME, recordMap.get(fileColumn.getColumnname()));
					response.put(MigratorConstants.ISNATIVE, false);
					response.put(DatabaseConstants.PDF_DESTINATION, pdfDestination);
					response.put(DatabaseConstants.NATIVE_DESTINATION,nativedestination);
					response.put(DatabaseConstants.SOURCE_LOCATION, sourcelocation);
					response.put(DatabaseConstants.START_DATE_TIME, startdate);
					LOGGER.info("FILE json {}",response);
					jmsTemplate.convertAndSend(MigratorConstants.JMS_UPLOAD_MESSAGE, response.toString());
				}
			}
			LOGGER.info("total records in csv {}",records.size());
		} catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method getCsvHeaders {}",ioException);
		}finally {
				try {
					if(null!=csvParser) {
						csvParser.close();
					}
				} catch (IOException ioException2) {
					LOGGER.error("ioException2 occured while executing method getCsvHeaders {}",ioException2);
				}
		}
		
//		try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))){
//			while((line =br.readLine())!=null) {
//				if(StringUtils.isNotBlank(line) && StringUtils.isNotEmpty(line)) {
//					if(count==0) {
//						headers.putAll(identifyCsvHeader(line));
//					}else {
//						final Map<String, Object> recordMap = new HashMap<>();
//						final Map<String, Object> alfrescoProperties = new HashMap<>();
//						recordMap.putAll(getRecordAsMap(line, headers));
//						final String include = recordMap.containsKey(MigratorConstants.INCLUDE)?recordMap.get(MigratorConstants.INCLUDE)+"":recordMap.get(MigratorConstants.INCLUDE_CAMEL)+"";
//						if(MigratorConstants.YES.equalsIgnoreCase(include)) {
//							final String pdfYesNo = recordMap.get(MigratorConstants.COLUMN_PDF_YES_NO)+"";
//							if(MigratorConstants.YES.equalsIgnoreCase(pdfYesNo)) {
//								final FileColumn fileColumn = fileColumnRepo.findBytemplatedefinition(templateDefinition);
//								response = new JSONObject();
//								LOGGER.info("csv record {}",recordMap);
//								alfrescoProperties.clear();
//								alfrescoProperties.putAll(proccessAlfrescoProperties(recordMap, configuredColumns));
//								response.put(MigratorConstants.KEY_PROPERTIES, new JSONObject(alfrescoProperties));
//								response.put(MigratorConstants.KEY_CSV_RECORD_NUMBER, count);
//								response.put(MigratorConstants.KEY_FILE_TYPE, templateDefinition.getFiletypeqname());
//								response.put(DatabaseConstants.UNIQUE_ID, savedCSVFile.getLong(DatabaseConstants.UNIQUE_ID));
//								response.put(DatabaseConstants.IS_LATEST, islatest);
//								response.put(DatabaseConstants.NATIVE_TEMPLATE_ID, savedCSVFile.getLong(DatabaseConstants.NATIVE_TEMPLATE_ID));
//								response.put(DatabaseConstants.PDF_LOCATION, savedCSVFile.getString(DatabaseConstants.PDF_LOCATION));
//								response.put(DatabaseConstants.NATIVE_FILE_LOCATION, savedCSVFile.getString(DatabaseConstants.NATIVE_FILE_LOCATION));
//								response.put(DatabaseConstants.CSV_FILE_NAME, savedCSVFile.getString(DatabaseConstants.CSV_FILE_NAME));
//								response.put(MigratorConstants.KEY_FILE_NAME, recordMap.get(fileColumn.getColumnname()));
//								response.put(MigratorConstants.ISNATIVE, false);
//								response.put(DatabaseConstants.PDF_DESTINATION, pdfDestination);
//								response.put(DatabaseConstants.NATIVE_DESTINATION,nativedestination);
//								response.put(DatabaseConstants.SOURCE_LOCATION, sourcelocation);
//								jmsTemplate.convertAndSend(MigratorConstants.JMS_UPLOAD_MESSAGE, response.toString());
//							}
//							final String nativeYesNo = recordMap.get(MigratorConstants.COLUMN_NATIVE_FILE_YES_NO)+"";
//							if(MigratorConstants.YES.equalsIgnoreCase(nativeYesNo)) {
//								final FileColumn fileColumn = fileColumnRepo.findBytemplatedefinition(nativeTemplateDefinition);
//								response = new JSONObject();
//								alfrescoProperties.clear();
//								alfrescoProperties.putAll(proccessAlfrescoProperties(recordMap, nativeConfiguredColumns));
//								response.put(MigratorConstants.KEY_PROPERTIES, new JSONObject(alfrescoProperties));
//								response.put(MigratorConstants.KEY_CSV_RECORD_NUMBER, count);
//								response.put(MigratorConstants.KEY_FILE_TYPE, nativeTemplateDefinition.getFiletypeqname());
//								response.put(DatabaseConstants.UNIQUE_ID, savedCSVFile.getLong(DatabaseConstants.UNIQUE_ID));
//								response.put(DatabaseConstants.IS_LATEST, islatest);
//								response.put(DatabaseConstants.NATIVE_TEMPLATE_ID, savedCSVFile.getLong(DatabaseConstants.NATIVE_TEMPLATE_ID));
//								response.put(DatabaseConstants.PDF_LOCATION, savedCSVFile.getString(DatabaseConstants.PDF_LOCATION));
//								response.put(DatabaseConstants.NATIVE_FILE_LOCATION, savedCSVFile.getString(DatabaseConstants.NATIVE_FILE_LOCATION));
//								response.put(DatabaseConstants.CSV_FILE_NAME, savedCSVFile.getString(DatabaseConstants.CSV_FILE_NAME));
//								response.put(MigratorConstants.KEY_FILE_NAME, recordMap.get(fileColumn.getColumnname()));
//								response.put(MigratorConstants.ISNATIVE, true);
//								response.put(DatabaseConstants.NATIVE_DESTINATION,nativedestination);
//								response.put(DatabaseConstants.SOURCE_LOCATION, sourcelocation);
//								jmsTemplate.convertAndSend(MigratorConstants.JMS_UPLOAD_MESSAGE, response.toString());
//							}
//						}else {
//							alfrescoProperties.clear();
//							final FileColumn fileColumn = fileColumnRepo.findBytemplatedefinition(templateDefinition);
//							alfrescoProperties.putAll(proccessAlfrescoProperties(recordMap, configuredColumns));
//							response.put(MigratorConstants.KEY_PROPERTIES, new JSONObject(alfrescoProperties));
//							response.put(MigratorConstants.KEY_CSV_RECORD_NUMBER, count);
//							response.put(MigratorConstants.KEY_FILE_TYPE, templateDefinition.getFiletypeqname());
//							response.put(DatabaseConstants.UNIQUE_ID, savedCSVFile.getLong(DatabaseConstants.UNIQUE_ID));
//							response.put(DatabaseConstants.IS_LATEST, islatest);
//							response.put(DatabaseConstants.NATIVE_TEMPLATE_ID, savedCSVFile.getLong(DatabaseConstants.NATIVE_TEMPLATE_ID));
//							response.put(DatabaseConstants.PDF_LOCATION, savedCSVFile.getString(DatabaseConstants.PDF_LOCATION));
//							response.put(DatabaseConstants.NATIVE_FILE_LOCATION, savedCSVFile.getString(DatabaseConstants.NATIVE_FILE_LOCATION));
//							response.put(DatabaseConstants.CSV_FILE_NAME, savedCSVFile.getString(DatabaseConstants.CSV_FILE_NAME));
//							response.put(MigratorConstants.KEY_FILE_NAME, recordMap.get(fileColumn.getColumnname()));
//							response.put(MigratorConstants.ISNATIVE, false);
//							response.put(DatabaseConstants.PDF_DESTINATION, pdfDestination);
//							response.put(DatabaseConstants.NATIVE_DESTINATION,nativedestination);
//							response.put(DatabaseConstants.SOURCE_LOCATION, sourcelocation);
//							jmsTemplate.convertAndSend(MigratorConstants.JMS_UPLOAD_MESSAGE, response.toString());
//						}
//					}
//				}
//				count+=1;
//			}
//		} catch (IOException ioException) {
//			LOGGER.error("ioException occured while executing method getCsvHeaders {}",ioException);
//		}
	}
	
	/**
	 * Identify configured columns.
	 *
	 * @param templateDetails the template details
	 * @return the map
	 */
	private Map<String, String> identifyConfiguredColumns(final List<TemplateDetails> templateDetails){
		final Map<String, String> configuredColumns = new HashMap<>();
		for(final TemplateDetails details : templateDetails) {
			configuredColumns.put(details.getCsvcolumnname(), details.getAlfrescopropertyqname());
		}
		return configuredColumns;
	}
	
	/**
	 * Identify csv header.
	 *
	 * @param headerLine the header line
	 * @return the map
	 */
	@SuppressWarnings("unused")
	private Map<Integer, String> identifyCsvHeader(final String headerLine){
		AtomicInteger counter = new AtomicInteger(0);
		final Map<Integer, String> csvHeader = new HashMap<>();
		final List<String> splitHeader = Arrays.asList(headerLine.split(MigratorConstants.COMMA));
		if(splitHeader.isEmpty()) {
			LOGGER.debug("No Header found");
		}else {
			splitHeader.stream().forEach((header)->{
				//csvHeader.put(counter.intValue(),header);
				if(header.contains("FILE_NAME")) {
					csvHeader.put(counter.intValue(), "FILE_NAME");
				}else {					
					csvHeader.put(counter.intValue(), new String(header.getBytes(Charset.forName("UTF-8"))).replaceAll(JUNK_CHARACTER, StringUtils.EMPTY).replaceAll(StringUtils.SPACE, "_"));
				}
				counter.getAndIncrement();
			});
		}
		if(LOGGER.isDebugEnabled()) {			
			LOGGER.debug("header : {}",csvHeader);
		}
		return csvHeader;
	}
	
	/**
	 * Gets the record as map.
	 *
	 * @param record the record
	 * @param header the header
	 * @return the record as map
	 */
	@SuppressWarnings("unused")
	private Map<String, Object> getRecordAsMap(final String record,final Map<Integer, Object> header){
		final Map<String, Object> recordMap = new HashMap<>();
		final String splitExpression = ",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";
		final String[] splitRecord = record.split(splitExpression,-1);
		final String multiValuedFields = StringUtils.EMPTY;
		for(int index = 0; index<splitRecord.length;index++) {
			final String key = header.get(index)+"";
			String value = splitRecord[index];
			if(multiValuedFields.contains(key)) {
				value = proccessMultiValueProp(value);
			}
			boolean isFirstDoublQuote = value.startsWith("\"");
			boolean isLastDoubleQuote = value.endsWith("\"");
			if(isFirstDoublQuote && isLastDoubleQuote) {	
				recordMap.put(key, value.replaceAll("\\|", StringUtils.EMPTY).substring(value.indexOf('"')+1,value.length()-1).replace("null", StringUtils.EMPTY));
			}else {
				recordMap.put(key, value.replaceAll("\\|", StringUtils.EMPTY).replace("null", StringUtils.EMPTY));
			}
			if(LOGGER.isDebugEnabled()) {				
				LOGGER.debug("index {}) name {} : value {}",index,key,value);
			}
			
		}
		return recordMap;
	}
	
	/**
	 * Gets the multivalued properties.
	 *
	 * @param templateDetails the template details
	 * @param ismultivalued the ismultivalued
	 * @return the multivalued properties
	 */
	private String getMultivaluedProperties(final List<TemplateDetails> templateDetails,final boolean ismultivalued) {
		final StringBuilder multivaluedBuilder = new StringBuilder();
		final List<String> propertiesList = new ArrayList<>();
		for(final TemplateDetails detail : templateDetails) {
			if(detail.isIsmultivalued()) {
				propertiesList.add(detail.getAlfrescopropertyqname());
			}
		}
		multivaluedBuilder.append(String.join(MigratorConstants.COMMA, propertiesList));
		return multivaluedBuilder.toString();
	}
	
	/**
	 * Proccess multi value prop.
	 *
	 * @param multiValue the multi value
	 * @return the string
	 */
	private String proccessMultiValueProp(final String multiValue) {
		final StringBuilder multiValueBuilder = new StringBuilder();
		final String splitExpression = "%s(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";
		final String[] multiValueSplit = multiValue.replaceAll("\"", StringUtils.EMPTY).split(String.format(splitExpression, ","));
		final Set<String> multiValueSet = new HashSet<>();
		multiValueSet.addAll(Arrays.asList(multiValueSplit));
		multiValueBuilder.append(String.join(",", multiValueSet));
		LOGGER.info("multi value prop : {}",multiValueBuilder);
		return (StringUtils.isNotEmpty(multiValueBuilder.toString()) && StringUtils.isNotBlank(multiValueBuilder.toString()))?multiValueBuilder.toString():StringUtils.EMPTY;
	}
	
	/**
	 * Proccess alfresco properties.
	 *
	 * @param recordMap the record map
	 * @param configuredColumns the configured columns
	 * @return the map
	 */
	public Map<String, String> proccessAlfrescoProperties(final Map<String,Object> recordMap,final Map<String, String> configuredColumns) {
		final Map<String, String> mappedProperties = new HashMap<>();
		for(final String key: configuredColumns.keySet()) {
			final String qname = configuredColumns.get(key);
			if(key.indexOf("+")>=0) {
				mappedProperties.put(qname, buildValue(key, recordMap));
			}else {
				final String value = recordMap.containsKey(key)?recordMap.get(key)+StringUtils.EMPTY:StringUtils.EMPTY;
				mappedProperties.put(qname, value);
			}
		}
		LOGGER.info("alfresco properties {}",mappedProperties);
		return mappedProperties;
	}
	
	/**
	 * Builds the value.
	 *
	 * @param key the key
	 * @param recordMap the record map
	 * @return the string
	 */
	private String buildValue(final String key,final Map<String,Object> recordMap) {
		final StringBuilder valueBuilder = new StringBuilder();
		if(null==key || StringUtils.isEmpty(key) || StringUtils.isBlank(key)) {
			valueBuilder.append(StringUtils.EMPTY);
		}else {
			final String[] splitKey = key.split("\\+",-1);
			for(int index=0;index<splitKey.length;index++) {
				if(recordMap.containsKey(splitKey[index])) {
					valueBuilder.append(recordMap.get(splitKey[index])).append(StringUtils.SPACE);
				}
			}
		}
		return valueBuilder.toString().trim();
	}
	
	/**
	 * Count migrated csv records.
	 *
	 * @param csvid the csvid
	 * @return the JSON object
	 */
	public JSONObject countMigratedCsvRecords(final long csvid) {
		final JSONObject responseJson = new JSONObject();
		final JSONArray responseObj = new JSONArray();
		final List<String> countsList = new ArrayList<>();
		countsList.addAll(migratedRecordRepo.countcsvuniqueId(csvid));
		final CSVFiles csvFiles = csvFilesRepo.findByuniqueid(csvid);
		if(null!=csvFiles) {
			responseJson.put(MigratorConstants.KEY_TOTAL, csvFiles.getTotalrecords());
		}
		LOGGER.info("{}",countsList);
		LOGGER.info("{}",countsList.size());
		for(final String resp: countsList) {
			final String[] splitStatus = resp.split(MigratorConstants.COMMA);
			LOGGER.info("{}",resp);
			final JSONObject sJson = new JSONObject();
			sJson.put(MigratorConstants.KEY_STATUS, splitStatus[0]);
			sJson.put(MigratorConstants.KEY_COUNT, splitStatus[1]);
			responseObj.put(sJson);
		}
//		responseJson.put(MigratorConstants.KEY_TOTAL_MIGRATED, migratedRecordRepo.findBycsvuniqueid(csvid,Sort.by(Sort.Direction.ASC, DatabaseConstants.CSV_RECORD_NUMBER)).size());
		responseJson.put(MigratorConstants.KEY_TOTAL_MIGRATED, migratedRecordRepo.findBycsvuniqueid(csvid,PageRequest.of(0, 1,Sort.by(Sort.Direction.ASC, DatabaseConstants.CSV_RECORD_NUMBER))).getContent().size());
		responseJson.put(MigratorConstants.KEY_LIST, responseObj);
		LOGGER.info("{}",responseJson);
		return responseJson;
	}
	
	/**
	 * Gets the pie chart data.
	 *
	 * @param csvid the csvid
	 * @return the pie chart data
	 */
	public JSONObject getPieChartData(final long csvid) {
		final JSONObject responseJson = new JSONObject();
		long success=0,failed=0;
		final JSONArray gArray = new JSONArray();
		final List<String> countsList = new ArrayList<>();
		countsList.addAll(migratedRecordRepo.countcsvuniqueId(csvid));
		final CSVFiles csvFiles = csvFilesRepo.findByuniqueid(csvid);
		if(null!=csvFiles) {
			responseJson.put(MigratorConstants.KEY_TOTAL, csvFiles.getTotalrecords());
		}
		LOGGER.info("{}",countsList);
		LOGGER.info("{}",countsList.size());
		gArray.put(new ArrayList<>(Arrays.asList("status","count")));
		final List<Object> successArray = new ArrayList<>();
		final List<Object> failedArray = new ArrayList<>();
		for(final String resp: countsList) {
			final String[] splitStatus = resp.split(MigratorConstants.COMMA);
			LOGGER.info("http status {} split status {}",HttpStatus.OK.value(),Integer.parseInt(splitStatus[0]));
			if(HttpStatus.OK.value() == Integer.parseInt(splitStatus[0]) || HttpStatus.CREATED.value() == Integer.parseInt(splitStatus[0])) {
				success+=Integer.parseInt(splitStatus[1]);
			}else {
				failed+=Integer.parseInt(splitStatus[1]);
			}
		}
		LOGGER.info("success count {} failed count {}",success,failed);
		successArray.add(MigratorConstants.KEY_SUCCESS);
		successArray.add(success);
		gArray.put(successArray);
		failedArray.add(MigratorConstants.KEY_FAILED);
		failedArray.add(failed);
		gArray.put(failedArray);
//		responseJson.put(MigratorConstants.KEY_TOTAL_MIGRATED, migratedRecordRepo.findBycsvuniqueid(csvid,Sort.by(Sort.Direction.ASC, DatabaseConstants.CSV_RECORD_NUMBER)).size());
		responseJson.put(MigratorConstants.KEY_TOTAL_MIGRATED, migratedRecordRepo.findBycsvuniqueid(csvid,PageRequest.of(0, 1,Sort.by(Sort.Direction.ASC, DatabaseConstants.STATUS))).getContent().size());
		responseJson.put("gvalues", gArray);
		LOGGER.info("{}",responseJson);
		return responseJson;
	}
	
	/**
	 * Gets the csv migrated data.
	 *
	 * @param csvid the csvid
	 * @param status the status
	 * @param issuccess the issuccess
	 * @param start the start
	 * @param max the max
	 * @return the csv migrated data
	 */
	public JSONObject getCsvMigratedData(final long csvid,final String status,final boolean issuccess,final int start,final int max) {
		final JSONArray responseJsonArray = new JSONArray();
		final JSONObject resJson = new JSONObject();
		LOGGER.info("skip {}",start);
		LOGGER.info("max {}",max);
		int slno = (start*max)+1;
		final List<MigratedRecord> migratedRecords = new ArrayList<>();
		final CSVFiles csvFiles = csvFilesRepo.findByuniqueid(csvid);
		Page<MigratedRecord> page = null;
		if(StringUtils.isEmpty(status) || StringUtils.isBlank(status)) {
			page = migratedRecordRepo.findBycsvuniqueid(csvid,PageRequest.of(start, max,Sort.by(Sort.Direction.ASC, DatabaseConstants.CSV_RECORD_NUMBER)));
		}else {
			final PageRequest pageRequest= PageRequest.of(start, max,Sort.by(Sort.Direction.ASC, DatabaseConstants.CSV_RECORD_NUMBER));
			LOGGER.info("statuc 200 {} {} {}",(HttpStatus.OK.value()==Integer.parseInt(status)),status,issuccess);
			if(issuccess) {
				page = migratedRecordRepo.findAll(MigratorSpecifications.csvIdSpec(csvid).and(MigratorSpecifications.okOrCreatedSpec()),pageRequest);
			}else {
				page =migratedRecordRepo.findAll(MigratorSpecifications.csvIdSpec(csvid).and(MigratorSpecifications.notOkSpec()),pageRequest);
			}
		}
//		migratedRecords.addAll(migratedRecordRepo.findBycsvuniqueid(csvid,Sort.by(Sort.Direction.ASC, DatabaseConstants.CSV_RECORD_NUMBER)));
		migratedRecords.addAll(page.getContent());
		for(final MigratedRecord record: migratedRecords) {
			LOGGER.info("csv record {}",record.toString());
			final JSONObject recordJson = new JSONObject(record.toString());
			recordJson.put(DatabaseConstants.START_DATE_TIME, record.getStartdate());
			recordJson.put(DatabaseConstants.END_DATE_TIME, record.getEnddate());
			recordJson.put(MigratorConstants.KEY_SLNO, slno);
			if(null!=csvFiles) {
				recordJson.put(MigratorConstants.KEY_FILE_PARENT_ID, csvFiles.getPdfDestination());
				recordJson.put(MigratorConstants.KEY_NATIVE_PARENT_ID, csvFiles.getNativedestination());
				recordJson.put(DatabaseConstants.NATIVE_TEMPLATE_ID, csvFiles.getNativetemplateid());
				recordJson.put(DatabaseConstants.TEMPLATE_ID, csvFiles.getTemplateid());
				recordJson.put(DatabaseConstants.SOURCE_LOCATION, csvFiles.getSourcelocation());
				TemplateDefinition templateDefinition = null;
				if(record.isIsnative()) {
					templateDefinition = templateDefinitionRepo.findBytemplateid(csvFiles.getNativetemplateid());
				}else {
					templateDefinition = templateDefinitionRepo.findBytemplateid(csvFiles.getTemplateid());
				}
				recordJson.put(MigratorConstants.KEY_FILE_TYPE,templateDefinition.getFiletypeqname());
				recordJson.put(DatabaseConstants.COLUMN_NAME, templateDefinition.getFilecolumn().getColumnname());
			}
			responseJsonArray.put(recordJson);
			slno+=1;
		}
		resJson.put(MigratorConstants.KEY_LIST, responseJsonArray);
		resJson.put(MigratorConstants.KEY_TOTAL, page.getTotalElements());
		resJson.put(MigratorConstants.KEY_COUNT, page.getNumberOfElements());
		resJson.put(MigratorConstants.KEY_PAGE, page.getPageable().getPageNumber());
		return resJson;
	}
	
	/**
	 * Update metadata reuplaod details.
	 *
	 * @param recordid the recordid
	 * @param recordJson the record json
	 * @return the JSON object
	 */
	public JSONObject updateMetadataReuplaodDetails(final String recordid,final JSONObject recordJson) {
		final JSONObject response = new JSONObject();
		final MigratedRecord migratedRecord = migratedRecordRepo.findBycsvfileid(Long.parseLong(recordid));
		final String status = recordJson.has(MigratorConstants.KEY_STATUS)?recordJson.getInt(MigratorConstants.KEY_STATUS)+"":migratedRecord.getStatus();
		final String statusMessage = recordJson.has(MigratorConstants.KEY_STATUS_MESSAGE)?recordJson.getString(MigratorConstants.KEY_STATUS_MESSAGE):migratedRecord.getMessage();
		final String propertiesStatus = recordJson.has(MigratorConstants.KEY_PROPERTIES_STATUS)?recordJson.getInt(MigratorConstants.KEY_PROPERTIES_STATUS)+"":migratedRecord.getPropertiesstatus();
		final String propertiesStatusMessage = recordJson.has(MigratorConstants.KEY_PROPERTIES_MESSAGE)?recordJson.getString(MigratorConstants.KEY_PROPERTIES_MESSAGE):migratedRecord.getPropertiesmessage();
		final String nodeRef = recordJson.has(MigratorConstants.KEY_NODE_REF)?recordJson.getString(MigratorConstants.KEY_NODE_REF):StringUtils.EMPTY;
//		final MigratedRecord migrateRec = new MigratedRecord();
//		migrateRec.setCsvfileid(migratedRecord.getCsvfileid());
//		migrateRec.setStatus(recordJson.getString(MigratorConstants.KEY_STATUS));
		migratedRecord.setStatus(status);
		migratedRecord.setMessage(statusMessage);
		migratedRecord.setPropertiesstatus(propertiesStatus);
		migratedRecord.setPropertiesmessage(propertiesStatusMessage);
		migratedRecord.setNoderef(nodeRef);
		final MigratedRecord saved = migratedRecordRepo.save(migratedRecord);
		response.put(MigratorConstants.KEY_DETAILS, saved.toString());
		return response;
	}
	
	
}
