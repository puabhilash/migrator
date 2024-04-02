/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.consumers;

import java.io.File;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.migrator.constants.DatabaseConstants;
import com.migrator.constants.MigratorConstants;
import com.migrator.database.ConfigurationDetails;
import com.migrator.database.MigratedRecord;
import com.migrator.repositories.CsvFilesRepo;
import com.migrator.repositories.MigratedRecordRepo;
import com.migrator.services.AlfrescoService;
import com.migrator.services.AmazonServices;
import com.migrator.services.ConfigurationService;
import com.migrator.utils.MigrationUtils;
import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;

/**
 * The Class CSVConsumer.
 */
@Service
public class CSVConsumer {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CSVConsumer.class);
	
	/** The alfresco service. */
	@Autowired
	AlfrescoService alfrescoService;
	
	/** The configuration service. */
	@Autowired
	ConfigurationService configurationService;
	
	/** The migrated record repo. */
	@Autowired
	MigratedRecordRepo migratedRecordRepo;
	
	/** The csv files repo. */
	@Autowired
	CsvFilesRepo csvFilesRepo;
	
	/** The amazon services. */
	@Autowired
	AmazonServices amazonServices;
	
	/**
	 * Csv consumer.
	 *
	 * @param jsonMessage the json message
	 */
	@JmsListener(destination = MigratorConstants.JMS_UPLOAD_MESSAGE)
	public void csvConsumer(final Message jsonMessage) {
		LOGGER.info("received message {}",jsonMessage);
		final MigratedRecord migratedRecord = new MigratedRecord();
		if(jsonMessage instanceof TextMessage) {
			TextMessage textMessage = (TextMessage)jsonMessage;
			try {
				final ConfigurationDetails configurationDetails = configurationService.getAlfConfigurations();
				final String alfrescoBaseURL = generateAlfrescoURL(configurationDetails);
				final JSONObject messageJson = new JSONObject(textMessage.getText().toString());
				LOGGER.info("decoded json {}",messageJson);
				migratedRecord.setStartdate(MigrationUtils.formatDateTime(messageJson.getString(DatabaseConstants.START_DATE_TIME)));
				final boolean isnativeFile = messageJson.getBoolean(MigratorConstants.ISNATIVE);
				final String fileName = getFilename(messageJson.getString(MigratorConstants.KEY_FILE_NAME));
				final String fileSeparator = System.getProperty("file.separator");
				final String csvFileName = messageJson.getString(DatabaseConstants.CSV_FILE_NAME);
				File file = null;
				String destination= StringUtils.EMPTY;
				final String sourceLocation = messageJson.getString(DatabaseConstants.SOURCE_LOCATION);
				if(MigratorConstants.LOCAL.equalsIgnoreCase(sourceLocation)) {
					if(isnativeFile) {
						final String nativeFileBaseLocation = messageJson.getString(DatabaseConstants.NATIVE_FILE_LOCATION);
						file = new File(nativeFileBaseLocation+fileSeparator+fileName);
						destination = messageJson.getString(DatabaseConstants.NATIVE_DESTINATION);
						migratedRecord.setLocalfilelocation(nativeFileBaseLocation);
					}else {
						final String fileBaseLocation = messageJson.getString(DatabaseConstants.PDF_LOCATION);
						file = new File(fileBaseLocation+fileSeparator+fileName);
						destination = messageJson.getString(DatabaseConstants.PDF_DESTINATION);
						migratedRecord.setLocalfilelocation(fileBaseLocation);
					}
					final String fileType = messageJson.getString(MigratorConstants.KEY_FILE_TYPE);
					final long uniqueId = messageJson.getLong(DatabaseConstants.UNIQUE_ID);
					final long csvRecordNumber = messageJson.getLong(MigratorConstants.KEY_CSV_RECORD_NUMBER);
					final boolean islatest = messageJson.getBoolean(DatabaseConstants.IS_LATEST);
					final JSONObject properties = messageJson.getJSONObject(MigratorConstants.KEY_PROPERTIES);
					properties.put(MigratorConstants.CM_DESCRIPTION, csvFileName);
					LOGGER.info("file exists {}",file.exists());
//					final CSVFiles csvFiles = new CSVFiles();
//					final CSVFiles csvFileRecord = csvFilesRepo.findByuniqueid(uniqueId);
//					csvFiles.setUniqueid(uniqueId);
//					long totalMigratedCount = 0;
//					long totalFailedCount = 0;
					if(null!=file && !file.exists()) {
//						if(null!=csvFileRecord) {
//							totalFailedCount = csvFileRecord.getTotalfailed();
//							totalFailedCount+=1;
//							csvFileRecord.setTotalfailed(totalFailedCount);
//						}else {
//							totalFailedCount+=1;
//							csvFileRecord.setTotalfailed(totalFailedCount);
//						}
						migratedRecord.setStatus("500");
						migratedRecord.setMessage("File "+file+" not found");
						migratedRecord.setPropertiesstatus(StringUtils.EMPTY);
						migratedRecord.setPropertiesmessage(StringUtils.EMPTY);
						migratedRecord.setNoderef(StringUtils.EMPTY);
					}else {
						final JSONObject response = alfrescoService.UploadFile(file, properties, alfrescoBaseURL, isnativeFile, fileType, destination, configurationDetails.getUsername(), configurationDetails.getPassword());
	//					if(HttpStatus.SC_OK == response.getInt(MigratorConstants.KEY_STATUS)) {
	//						if(null!=csvFileRecord) {
	//							totalMigratedCount = csvFileRecord.getTotalmigrated();
	//							totalMigratedCount+=1;
	//							csvFileRecord.setTotalmigrated(totalMigratedCount);
	//						}else {
	//							totalMigratedCount+=1;
	//							csvFileRecord.setTotalmigrated(totalMigratedCount);
	//						}
	//					}else {
	//						if(null!=csvFileRecord) {
	//							totalFailedCount = csvFileRecord.getTotalfailed();
	//							totalFailedCount+=1;
	//							csvFileRecord.setTotalfailed(totalFailedCount);
	//						}else {
	//							totalFailedCount+=1;
	//							csvFileRecord.setTotalfailed(totalFailedCount);
	//						}
	//					}
						LOGGER.info("upload response {}",response);
						migratedRecord.setStatus(response.getInt(MigratorConstants.KEY_STATUS)+"");
						migratedRecord.setMessage(response.getString(MigratorConstants.KEY_STATUS_MESSAGE));
						migratedRecord.setPropertiesstatus(response.getString(MigratorConstants.KEY_PROPERTIES_STATUS));
						migratedRecord.setPropertiesmessage(response.getString(MigratorConstants.KEY_PROPERTIES_MESSAGE));
						migratedRecord.setNoderef(response.getString(MigratorConstants.KEY_NODE_REF));
					}
					migratedRecord.setFilename(fileName);
					LOGGER.info("properties {}",properties);
	//				final CSVFiles csvFiles = new CSVFiles();
	//				csvFiles.setUniqueid(uniqueId);
	//				migratedRecord.setCsvFiles(csvFiles);
					migratedRecord.setCsvuniqueid(uniqueId);
					migratedRecord.setIslatest(islatest);
					migratedRecord.setIsnative(isnativeFile);
					LOGGER.info("json to map {}",properties.toMap());
//					migratedRecord.setProperties(JacksonUtil.toJsonNode(properties.toString()));
					migratedRecord.setProperties(properties.toMap());
					migratedRecord.setCsvrecordnumber(csvRecordNumber);
					migratedRecord.setEnddate(LocalDateTime.now());
					migratedRecordRepo.save(migratedRecord);
//					csvFilesRepo.save(csvFileRecord);
					jsonMessage.acknowledge();
				}else if(MigratorConstants.AMAZON_S3.equalsIgnoreCase(sourceLocation)) {
					final AmazonS3 amazonS3 = amazonServices.gets3Session();
					final String fileType = messageJson.getString(MigratorConstants.KEY_FILE_TYPE);
					final long uniqueId = messageJson.getLong(DatabaseConstants.UNIQUE_ID);
					final long csvRecordNumber = messageJson.getLong(MigratorConstants.KEY_CSV_RECORD_NUMBER);
					final boolean islatest = messageJson.getBoolean(DatabaseConstants.IS_LATEST);
					final JSONObject properties = messageJson.getJSONObject(MigratorConstants.KEY_PROPERTIES);
					properties.put(MigratorConstants.CM_DESCRIPTION, csvFileName);
					if(isnativeFile) {
						final String nativeFileBaseLocation = messageJson.getString(DatabaseConstants.NATIVE_FILE_LOCATION);
						final String fileLocation = nativeFileBaseLocation+"/"+fileName;
						migratedRecord.setFilename(fileName);
						migratedRecord.setLocalfilelocation(nativeFileBaseLocation);
						LOGGER.info("file location {}",fileLocation);
						final S3Object object = amazonServices.readS3Object(amazonS3, fileLocation);
						if(null==object) {
							LOGGER.info("could not read inpout stream of file in s3 {}",fileLocation);
							migratedRecord.setStatus(HttpStatus.SC_NOT_FOUND+"");
							migratedRecord.setMessage(fileName+" missing in s3 location "+nativeFileBaseLocation);
							migratedRecord.setPropertiesstatus(HttpStatus.SC_NOT_FOUND+"");
							migratedRecord.setPropertiesmessage(fileName+" missing in s3 location "+nativeFileBaseLocation);
							migratedRecord.setNoderef(StringUtils.EMPTY);
						}else {
							destination = messageJson.getString(DatabaseConstants.NATIVE_DESTINATION);
							migratedRecord.setLocalfilelocation(nativeFileBaseLocation);
							LOGGER.info("native input stream {}",object);
							LOGGER.info("mimetype {} size {}",object.getObjectMetadata().getContentType(),object.getObjectMetadata().getContentLength());
							final JSONObject responseObj = alfrescoService.createNode(alfrescoBaseURL, properties, fileType, object.getObjectContent(), destination, object.getKey(), configurationDetails.getUsername(), configurationDetails.getPassword());
							LOGGER.info("upload response nagtive {}",responseObj);
							migratedRecord.setStatus(responseObj.getInt(MigratorConstants.KEY_STATUS)+"");
							migratedRecord.setMessage(responseObj.getString(MigratorConstants.KEY_STATUS_MESSAGE));
							migratedRecord.setPropertiesstatus(responseObj.getInt(MigratorConstants.KEY_PROPERTIES_STATUS)+"");
							migratedRecord.setPropertiesmessage(responseObj.getString(MigratorConstants.KEY_PROPERTIES_MESSAGE));
							migratedRecord.setNoderef(responseObj.getString(MigratorConstants.KEY_NODE_REF));
							migratedRecord.setFilesize(object.getObjectMetadata().getContentLength());
//							inputStream.close();
//							inputStream.abort();
						}
					}else {
						final String fileBaseLocation = messageJson.getString(DatabaseConstants.PDF_LOCATION);
						final String fileLocation = fileBaseLocation+"/"+fileName;
						migratedRecord.setLocalfilelocation(fileBaseLocation);
						migratedRecord.setFilename(fileName);
						LOGGER.info("file location {}",fileLocation);
						final S3Object object = amazonServices.readS3Object(amazonS3, fileLocation);
						if(null==object) {
							LOGGER.info("could not read inpout stream of file in s3 {}",fileLocation);
							migratedRecord.setStatus(HttpStatus.SC_NOT_FOUND+"");
							migratedRecord.setMessage(fileName+" missing in s3 location "+fileBaseLocation);
							migratedRecord.setPropertiesstatus(HttpStatus.SC_NOT_FOUND+"");
							migratedRecord.setPropertiesmessage(fileName+" missing in s3 location "+fileBaseLocation);
							migratedRecord.setNoderef(StringUtils.EMPTY);
						}else {
							destination = messageJson.getString(DatabaseConstants.PDF_DESTINATION);
							LOGGER.info("native input stream {}",object);
							LOGGER.info("mimetype {} size {}",object.getObjectMetadata().getContentType(),object.getObjectMetadata().getContentLength());
							final JSONObject responseObj = alfrescoService.createNode(alfrescoBaseURL, properties, fileType, object.getObjectContent(), destination, object.getKey(), configurationDetails.getUsername(), configurationDetails.getPassword());
							LOGGER.info("upload response pdf {}",responseObj);
							migratedRecord.setStatus(responseObj.getInt(MigratorConstants.KEY_STATUS)+"");
							migratedRecord.setMessage(responseObj.getString(MigratorConstants.KEY_STATUS_MESSAGE));
							migratedRecord.setPropertiesstatus(responseObj.getInt(MigratorConstants.KEY_PROPERTIES_STATUS)+"");
							migratedRecord.setPropertiesmessage(responseObj.getString(MigratorConstants.KEY_PROPERTIES_MESSAGE));
							migratedRecord.setNoderef(responseObj.getString(MigratorConstants.KEY_NODE_REF));
							migratedRecord.setFilesize(object.getObjectMetadata().getContentLength());
//							inputStream.close();
//							inputStream.abort();
						}
					}
					migratedRecord.setFilename(fileName);
					migratedRecord.setCsvuniqueid(uniqueId);
					migratedRecord.setIslatest(islatest);
					migratedRecord.setIsnative(isnativeFile);
					LOGGER.info("json to map {}",JacksonUtil.toJsonNode(new String(properties.toString().getBytes(Charset.forName("UTF-8")))));
//					migratedRecord.setProperties(JacksonUtil.toJsonNode(new String(properties.toString().getBytes(Charset.forName("UTF-8")))));
					migratedRecord.setProperties(properties.toMap());
					migratedRecord.setCsvrecordnumber(csvRecordNumber);
					migratedRecord.setEnddate(LocalDateTime.now());
					LOGGER.info("to string migrated record {}",migratedRecord.toString());
					LOGGER.info("migrated record {}",migratedRecord);
					migratedRecordRepo.save(migratedRecord);
					jsonMessage.acknowledge();
					amazonS3.shutdown();
				}
				
			} catch (JMSException jMSException) {
				LOGGER.error("jMSException occured while consuming message {}",jMSException);
				migratedRecord.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR+"");
				migratedRecord.setMessage(jMSException.getMessage());
				migratedRecord.setPropertiesstatus(HttpStatus.SC_INTERNAL_SERVER_ERROR+"");
				migratedRecord.setPropertiesmessage(jMSException.getMessage());
				migratedRecord.setNoderef(StringUtils.EMPTY);
				migratedRecord.setEnddate(LocalDateTime.now());
				migratedRecordRepo.save(migratedRecord);
			} 
		}
//			catch (IOException iOException) {
//				LOGGER.error("iOException occured while consuming message and uploading document {}",iOException);
//				migratedRecord.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR+"");
//				migratedRecord.setMessage(iOException.getMessage());
//				migratedRecord.setPropertiesstatus(HttpStatus.SC_INTERNAL_SERVER_ERROR+"");
//				migratedRecord.setPropertiesmessage(iOException.getMessage());
//				migratedRecord.setNoderef(StringUtils.EMPTY);
//				migratedRecordRepo.save(migratedRecord);
//			} 
 
	}
	
	/**
	 * Generate alfresco URL.
	 *
	 * @param configurationDetails the configuration details
	 * @return the string
	 */
	private String generateAlfrescoURL(final ConfigurationDetails configurationDetails) {
		final StringBuilder alfrescoURLBuilder = new StringBuilder();
		if(null!=configurationDetails) {
			alfrescoURLBuilder.append(MigrationUtils.generateAlfrescoURL(configurationDetails));
		}else {
			alfrescoURLBuilder.append(MigrationUtils.generateAlfrescoURL(new ConfigurationDetails()));
		}
		return alfrescoURLBuilder.toString();
	}
	
	/**
	 * Gets the filename.
	 *
	 * @param fileNamePath the file name path
	 * @return the filename
	 */
	private String getFilename(final String fileNamePath) {
		final StringBuilder filenameBuilder = new StringBuilder();
		final int index = fileNamePath.lastIndexOf("\\");
		if(index>0) {
			filenameBuilder.append(fileNamePath.substring(index+1));
		}else {
			filenameBuilder.append(fileNamePath);
		}
		return filenameBuilder.toString();
	}

}
