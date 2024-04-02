/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tcs.constants.DatabaseConstants;
import com.tcs.constants.MigratorConstants;
import com.tcs.database.FileColumn;
import com.tcs.database.TemplateDefinition;
import com.tcs.database.TemplateDetails;
import com.tcs.repositories.FileColumnRepo;
import com.tcs.repositories.TemplateDefinitionRepo;
import com.tcs.repositories.TemplateDetailsRepo;

/**
 * The Class ExcelService.
 */
@Service
public class ExcelService {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelService.class);
	
	/** The template definition repo. */
	@Autowired
	TemplateDefinitionRepo templateDefinitionRepo;
	
	/** The template details repo. */
	@Autowired
	TemplateDetailsRepo templateDetailsRepo;
	
	/** The jms template. */
	@Autowired
	JmsTemplate jmsTemplate;
	
	/** The file column repo. */
	@Autowired
	FileColumnRepo fileColumnRepo;
	
	/**
	 * Gets the excel headers.
	 *
	 * @param multipartFile the multipart file
	 * @return the excel headers
	 */
	public JSONArray getExcelHeaders(final MultipartFile multipartFile) {
		JSONArray columnsJsonArray = new JSONArray();
		final Map<Integer, String> header = new HashMap<>();
		final Iterator<Row> rowIterator=getRowIterator(multipartFile);
		final Row row = rowIterator.next();
		if(row.getRowNum()==0) {
			header.putAll(getValues(row));
			LOGGER.info("____________________{} {}_________________________","Row",row.getRowNum());
			columnsJsonArray = getcolumnsArray(header);
			LOGGER.info("_______________________________________________");
		}
		return columnsJsonArray;
	}
	
	/**
	 * Gets the columns array.
	 *
	 * @param header the header
	 * @return the columns array
	 */
	private JSONArray getcolumnsArray(final Map<Integer, String> header) {
		final JSONArray columnsJsonArray = new JSONArray();
		for(final Integer index:header.keySet()) {
			final JSONObject columnJson= new JSONObject();
			columnJson.put(MigratorConstants.KEY_COLUMN_NAME, header.get(index));
			columnsJsonArray.put(columnJson);
		}
		return columnsJsonArray;
	}
	
	/**
	 * Count records.
	 *
	 * @param multipartFile the multipart file
	 * @param hasincludecolumn 
	 * @return the long
	 */
	public long countRecords(final MultipartFile multipartFile, final boolean hasincludecolumn) {
		final Iterator<Row> iterator = getRowIterator(multipartFile);
		long count=0;
		final Map<Integer, String> header = new HashMap<>();
		while(iterator.hasNext()) {
			final Row row = iterator.next();
			final Map<Integer, String> valuesMap = new HashMap<>();
			final Map<String, String> record = new HashMap<>();
			if(row.getRowNum()==0) {
				header.putAll(getValues(row));
			}else {
				valuesMap.putAll(getValues(row));
				record.putAll(getRecordasMap(header, valuesMap));
				final String include = record.containsKey(MigratorConstants.INCLUDE)?record.get(MigratorConstants.INCLUDE)+"":record.get(MigratorConstants.INCLUDE_CAMEL)+"";
				LOGGER.info("-------------------has include {}",hasincludecolumn);
				if(hasincludecolumn) {
					if(MigratorConstants.YES.equalsIgnoreCase(include)) {
						final String pdfYesNo = record.get(MigratorConstants.COLUMN_PDF_YES_NO)+"";
						final String nativeYesNo = record.get(MigratorConstants.COLUMN_NATIVE_FILE_YES_NO)+"";
						if(MigratorConstants.YES.equalsIgnoreCase(pdfYesNo)) {
							count+=1;
						}
						if(MigratorConstants.YES.equalsIgnoreCase(nativeYesNo)) {
							count+=1;
						}
					}
				}else {
					count+=1;
				}
			}
			
		}
		return count;
	}
	
	/**
	 * Send upload message.
	 *
	 * @param savedCSVFile the saved CSV file
	 * @param multipartFile the multipart file
	 * @param islatest the islatest
	 * @param pdfDestination the pdf destination
	 * @param nativedestination the nativedestination
	 * @param sourcelocation 
	 * @param hasincludecolumn 
	 */
	public void sendUploadMessage(final JSONObject savedCSVFile, final MultipartFile multipartFile, final boolean islatest, final String pdfDestination, 
			final String nativedestination, final String sourcelocation, final boolean hasincludecolumn) {
		final Map<String, String> configuredColumns = new HashMap<>();
		final Map<String, String> nativeConfiguredColumns = new HashMap<>();
		final long templateId = savedCSVFile.getLong(DatabaseConstants.TEMPLATE_ID);
		final long nativeTemplateId = savedCSVFile.getLong(DatabaseConstants.NATIVE_TEMPLATE_ID);
		LOGGER.info("template ID {}",templateId);
		LOGGER.info("ntive template ID {}",nativeTemplateId);
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
			readExcel(multipartFile,configuredColumns,multiValuedProps,templateDefinition,savedCSVFile,islatest,nativeConfiguredColumns,nativeTemplateDefinition,pdfDestination,nativedestination,sourcelocation,hasincludecolumn);
		}else {
			LOGGER.error("no template found for definition {}",templateId);
		}
		
	}
	
	/**
	 * Read excel.
	 *
	 * @param multipartFile the multipart file
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
	public void readExcel(final MultipartFile multipartFile, final Map<String, String> configuredColumns, final String multiValuedProps, 
			final TemplateDefinition templateDefinition, final JSONObject savedCSVFile, final boolean islatest, 
			final Map<String, String> nativeConfiguredColumns, final TemplateDefinition nativeTemplateDefinition, 
			final String pdfDestination, final String nativedestination, final String sourcelocation, final boolean hasincludecolumn) {
		JSONObject response = new JSONObject();
		final Map<Integer, String> header = new HashMap<>();
		final Iterator<Row> rowIterator=getRowIterator(multipartFile);
		final LocalDateTime startdate = LocalDateTime.now();
		while(rowIterator.hasNext()) {
			final Row row = rowIterator.next();
			final Map<Integer, String> valuesMap = new HashMap<>();
			final Map<String, String> record = new HashMap<>();
			if(row.getRowNum()==0) {
				LOGGER.info("____________________%s %d \\n_________________________","Row",row.getRowNum());
				header.putAll(getValues(row));
				LOGGER.info("_______________________________________________");
			}else {
				valuesMap.putAll(getValues(row));
				record.putAll(getRecordasMap(header, valuesMap));
				final Map<String, Object> alfrescoProperties = new HashMap<>();
				final String include = record.containsKey(MigratorConstants.INCLUDE)?record.get(MigratorConstants.INCLUDE)+"":record.get(MigratorConstants.INCLUDE_CAMEL)+"";
				if(hasincludecolumn) {
					if(MigratorConstants.YES.equalsIgnoreCase(include)) {
						final String pdfYesNo = record.get(MigratorConstants.COLUMN_PDF_YES_NO)+"";
						final String nativeYesNo = record.get(MigratorConstants.COLUMN_NATIVE_FILE_YES_NO)+"";
						if(MigratorConstants.YES.equalsIgnoreCase(pdfYesNo)) {
							LOGGER.info("csv record {}",record);
							final FileColumn fileColumn = fileColumnRepo.findBytemplatedefinition(templateDefinition);
							alfrescoProperties.clear();
							alfrescoProperties.putAll(proccessAlfrescoProperties(record, configuredColumns));
							response.put(MigratorConstants.KEY_PROPERTIES, new JSONObject(alfrescoProperties));
							response.put(MigratorConstants.KEY_CSV_RECORD_NUMBER, row.getRowNum());
							response.put(MigratorConstants.KEY_FILE_TYPE, templateDefinition.getFiletypeqname());
							response.put(DatabaseConstants.UNIQUE_ID, savedCSVFile.getLong(DatabaseConstants.UNIQUE_ID));
							response.put(DatabaseConstants.IS_LATEST, islatest);
							response.put(DatabaseConstants.NATIVE_TEMPLATE_ID, savedCSVFile.getLong(DatabaseConstants.NATIVE_TEMPLATE_ID));
							response.put(DatabaseConstants.PDF_LOCATION, savedCSVFile.getString(DatabaseConstants.PDF_LOCATION));
							response.put(DatabaseConstants.NATIVE_FILE_LOCATION, savedCSVFile.getString(DatabaseConstants.NATIVE_FILE_LOCATION));
							response.put(DatabaseConstants.CSV_FILE_NAME, savedCSVFile.getString(DatabaseConstants.CSV_FILE_NAME));
							response.put(MigratorConstants.KEY_FILE_NAME, record.get(fileColumn.getColumnname()));
							response.put(MigratorConstants.ISNATIVE, false);
							response.put(DatabaseConstants.PDF_DESTINATION, pdfDestination);
							response.put(DatabaseConstants.SOURCE_LOCATION, sourcelocation);
							response.put(DatabaseConstants.START_DATE_TIME, startdate);
							LOGGER.info("PDF MESSAGE {}",response);
							jmsTemplate.convertAndSend(MigratorConstants.JMS_UPLOAD_MESSAGE, response.toString());
						}
						if(MigratorConstants.YES.equalsIgnoreCase(nativeYesNo)) {
							response = new JSONObject();
							final FileColumn fileColumn = fileColumnRepo.findBytemplatedefinition(nativeTemplateDefinition);
							alfrescoProperties.clear();
							alfrescoProperties.putAll(proccessAlfrescoProperties(record, nativeConfiguredColumns));
							response.put(MigratorConstants.KEY_PROPERTIES, new JSONObject(alfrescoProperties));
							response.put(MigratorConstants.KEY_CSV_RECORD_NUMBER, row.getRowNum());
							response.put(MigratorConstants.KEY_FILE_TYPE, nativeTemplateDefinition.getFiletypeqname());
							response.put(DatabaseConstants.UNIQUE_ID, savedCSVFile.getLong(DatabaseConstants.UNIQUE_ID));
							response.put(DatabaseConstants.IS_LATEST, islatest);
							response.put(DatabaseConstants.NATIVE_TEMPLATE_ID, savedCSVFile.getLong(DatabaseConstants.NATIVE_TEMPLATE_ID));
							response.put(DatabaseConstants.PDF_LOCATION, savedCSVFile.getString(DatabaseConstants.PDF_LOCATION));
							response.put(DatabaseConstants.NATIVE_FILE_LOCATION, savedCSVFile.getString(DatabaseConstants.NATIVE_FILE_LOCATION));
							response.put(DatabaseConstants.CSV_FILE_NAME, savedCSVFile.getString(DatabaseConstants.CSV_FILE_NAME));
							response.put(MigratorConstants.KEY_FILE_NAME, record.get(fileColumn.getColumnname()));
							response.put(MigratorConstants.ISNATIVE, true);
							response.put(DatabaseConstants.NATIVE_DESTINATION,nativedestination);
							response.put(DatabaseConstants.SOURCE_LOCATION, sourcelocation);
							response.put(DatabaseConstants.START_DATE_TIME, startdate);
							LOGGER.info("NATIVE MESSAGE {}",response);
							jmsTemplate.convertAndSend(MigratorConstants.JMS_UPLOAD_MESSAGE, response.toString());
						}
					}
				}else {
					final FileColumn fileColumn = fileColumnRepo.findBytemplatedefinition(templateDefinition);
					alfrescoProperties.clear();
					LOGGER.info("------------------------------------------------");
					LOGGER.info("{}",record);
					LOGGER.info("------------------------------------------------");
					alfrescoProperties.putAll(proccessAlfrescoProperties(record, configuredColumns));
					response.put(MigratorConstants.KEY_PROPERTIES, new JSONObject(alfrescoProperties));
					response.put(MigratorConstants.KEY_CSV_RECORD_NUMBER, row.getRowNum());
					response.put(MigratorConstants.KEY_FILE_TYPE, templateDefinition.getFiletypeqname());
					response.put(DatabaseConstants.UNIQUE_ID, savedCSVFile.getLong(DatabaseConstants.UNIQUE_ID));
					response.put(DatabaseConstants.IS_LATEST, islatest);
					response.put(DatabaseConstants.NATIVE_TEMPLATE_ID, savedCSVFile.getLong(DatabaseConstants.NATIVE_TEMPLATE_ID));
					response.put(DatabaseConstants.PDF_LOCATION, savedCSVFile.getString(DatabaseConstants.PDF_LOCATION));
					response.put(DatabaseConstants.NATIVE_FILE_LOCATION, savedCSVFile.getString(DatabaseConstants.NATIVE_FILE_LOCATION));
					response.put(DatabaseConstants.CSV_FILE_NAME, savedCSVFile.getString(DatabaseConstants.CSV_FILE_NAME));
					response.put(MigratorConstants.KEY_FILE_NAME, record.get(fileColumn.getColumnname()));
					response.put(MigratorConstants.ISNATIVE, false);
					response.put(DatabaseConstants.PDF_DESTINATION, pdfDestination);
					response.put(DatabaseConstants.SOURCE_LOCATION, sourcelocation);
					response.put(DatabaseConstants.START_DATE_TIME, startdate);
					LOGGER.info("NORMAL MESSAGE {}",response);
					jmsTemplate.convertAndSend(MigratorConstants.JMS_UPLOAD_MESSAGE, response.toString());
				}
			}
		}
		LOGGER.info("total columns {}",header.size());
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
	 * Proccess alfresco properties.
	 *
	 * @param recordMap the record map
	 * @param configuredColumns the configured columns
	 * @return the map
	 */
	public Map<String, String> proccessAlfrescoProperties(final Map<String,String> recordMap,final Map<String, String> configuredColumns) {
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
	private String buildValue(final String key,final Map<String,String> recordMap) {
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
	 * Gets the row iterator.
	 *
	 * @param multipartFile the multipart file
	 * @return the row iterator
	 */
	public Iterator<Row> getRowIterator(final MultipartFile multipartFile){
		Iterator<Row> rowIterator=null;
		XSSFWorkbook wb = null;
		HSSFWorkbook hwb =null;
		final String fileExt = FileNameUtils.getExtension(multipartFile.getOriginalFilename());
		try {
			if("xls".equalsIgnoreCase(fileExt)) {
				hwb = new HSSFWorkbook(multipartFile.getInputStream());
				final HSSFSheet sheet = hwb.getSheetAt(0);
				rowIterator = sheet.rowIterator();
			}else {
				wb = new XSSFWorkbook(multipartFile.getInputStream());
				final XSSFSheet sheet = wb.getSheetAt(0);
				rowIterator = sheet.rowIterator();
				
			}
		}catch (FileNotFoundException fileNotFoundException) {
			LOGGER.error("fileNotFoundException occured while executing method readExcel {}",fileNotFoundException);
		} catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method readExcel {}",ioException);
		}finally {
			try {
				if(null!=wb) {
					wb.close();
				}
				if(null!=hwb) {
					hwb.close();
				}
			} catch (IOException ioException2) {
				LOGGER.error("ioException2 occured while executing method readExcel {}",ioException2);
			}
		}
		return rowIterator;
	}
	
	/**
	 * Gets the values.
	 *
	 * @param row the row
	 * @return the values
	 */
	public Map<Integer, String> getValues(final Row row){
		final Map<Integer, String> rowValues = new HashMap<>();
		for (Cell cell: row){
			switch (cell.getCellType()) {
			case NUMERIC:
				rowValues.put(cell.getColumnIndex(), String.format("%.0f", cell.getNumericCellValue()));
				break;
			case STRING:
				rowValues.put(cell.getColumnIndex(),  new String(cell.getStringCellValue().trim().getBytes(), Charset.forName("UTF-8")));
				break;
			case BOOLEAN:
				rowValues.put(cell.getColumnIndex(), Boolean.toString(cell.getBooleanCellValue()));
				break;
			case BLANK:
				rowValues.put(cell.getColumnIndex(), StringUtils.EMPTY);
				break;
			case _NONE:
				rowValues.put(cell.getColumnIndex(), StringUtils.EMPTY);
				break;
			case FORMULA:
				final String formula = cell.getCellFormula();
				if(formula.contains("HYPERLINK")) {
					Hyperlink hyperlink = cell.getHyperlink();
					if(null==hyperlink) {
						rowValues.put(cell.getColumnIndex(), cell.getRichStringCellValue().toString());
					}else {
						rowValues.put(cell.getColumnIndex(), hyperlink.getAddress());
					}
				}else {
					rowValues.put(cell.getColumnIndex(), StringUtils.EMPTY);
				}
				break;
			default:
				break;
			}
		}
		return rowValues;
	}
	
	/**
	 * Gets the recordas map.
	 *
	 * @param header the header
	 * @param valueMap the value map
	 * @return the recordas map
	 */
	public Map<String, String> getRecordasMap(final Map<Integer, String> header,final Map<Integer, String> valueMap){
		final Map<String, String> record = new HashMap<>();
		for(final Integer index: header.keySet()) {
			record.put(header.get(index), valueMap.containsKey(index)?valueMap.get(index):StringUtils.EMPTY);
		}
		LOGGER.info("{}",record);
		LOGGER.info("row size {}",record.size());
		return record;
	}

}
