/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.restcontrollers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.migrator.constants.DatabaseConstants;
import com.migrator.constants.MigratorConstants;
import com.migrator.database.CSVFiles;
import com.migrator.database.ConfigurationDetails;
import com.migrator.database.MigratedRecord;
import com.migrator.database.TemplateDefinition;
import com.migrator.database.specifications.MigratorSpecifications;
import com.migrator.repositories.ConfigurationsDetailsRepo;
import com.migrator.repositories.CsvFilesRepo;
import com.migrator.repositories.MigratedRecordRepo;
import com.migrator.repositories.TemplateDefinitionRepo;
import com.migrator.services.AlfrescoService;
import com.migrator.services.AmazonServices;
import com.migrator.services.CSVService;
import com.migrator.services.CounterService;
import com.migrator.services.ExcelService;
import com.migrator.utils.MigrationUtils;

/**
 * The Class MigratorController.
 */
@RestController
public class MigratorController {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MigratorController.class);
	
	@Value("${spring.jms.listener.concurrency}")
	int maxThreads;
	
	/** The size. */
	@Value("${batch.size}")
	int size;
	
	/** The csv service. */
	@Autowired
	CSVService csvService;
	
	/** The excel service. */
	@Autowired
	ExcelService excelService;
	
	/** The alfresco service. */
	@Autowired
	AlfrescoService alfrescoService;
	
	/** The configurations details repo. */
	@Autowired
	ConfigurationsDetailsRepo configurationsDetailsRepo;
	
	/** The migrated record repo. */
	@Autowired
	MigratedRecordRepo migratedRecordRepo;
	
	/** The amazon services. */
	@Autowired
	AmazonServices amazonServices;
	
	/** The csv files repo. */
	@Autowired
	CsvFilesRepo csvFilesRepo;
	
	/** The template definition repo. */
	@Autowired
	TemplateDefinitionRepo templateDefinitionRepo;
	
	/** The counter service. */
	@Autowired
	CounterService counterService;
	
	/** The failed counter. */
	AtomicInteger failedCounter = new AtomicInteger(0);
	
	/** The success counter. */
	AtomicInteger successCounter = new AtomicInteger(0);
	
	/**
	 * Migrate csv records.
	 *
	 * @param file the file
	 * @param pdfdestination the pdfdestination
	 * @param nativedestination the nativedestination
	 * @param localpdflocation the localpdflocation
	 * @param localnativelocation the localnativelocation
	 * @param islatest the islatest
	 * @param metadtatemplateid the metadtatemplateid
	 * @param nativemetadtatemplateid the nativemetadtatemplateid
	 * @param sourcelocation the sourcelocation
	 * @param hasinclude the hasinclude
	 * @param httpServletResponse the http servlet response
	 * @return the string
	 */
	@SuppressWarnings("unused")
	@PostMapping(value = {"/migrate"})
	public String migrateCsvRecords(@RequestParam MultipartFile file, @RequestParam String pdfdestination, @RequestParam String nativedestination,
			@RequestParam String localpdflocation, @RequestParam String localnativelocation, @RequestParam boolean islatest,@RequestParam long metadtatemplateid,@RequestParam long nativemetadtatemplateid
			,@RequestParam(required = false) String sourcelocation,@RequestParam(required = false) Boolean hasinclude,final HttpServletResponse httpServletResponse) {
		JSONObject response = new JSONObject();
		try {
			LOGGER.info("executing URL {}","/migratecsv");
			LOGGER.info("csv file {}",file.getOriginalFilename());
			LOGGER.info("pdf file destination alfresco {}",pdfdestination);
			LOGGER.info("native file destination alfresco {}",nativedestination);
			LOGGER.info("local pdf location {}",localpdflocation);
			LOGGER.info("local native location {}",localnativelocation);
			LOGGER.info("is latest files {}",islatest);
			LOGGER.info("template ID {}",metadtatemplateid);
			LOGGER.info("native template ID {}",nativemetadtatemplateid);
			final String fileExt = FileNameUtils.getExtension(file.getOriginalFilename());
			if(null==file) {
				response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
				response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
				response.put(MigratorConstants.KEY_MESSAGE, "csvfile parameter not found");
				httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
			}else if(null==pdfdestination || StringUtils.isEmpty(pdfdestination) || StringUtils.isBlank(pdfdestination)) {
				response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
				response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
				response.put(MigratorConstants.KEY_MESSAGE, "pdfdestination parameter not found");
				httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
			}else if(null==nativedestination || StringUtils.isEmpty(nativedestination) || StringUtils.isBlank(nativedestination)) {
				response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
				response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
				response.put(MigratorConstants.KEY_MESSAGE, "nativedestination parameter not found");
				httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
			}else if(StringUtils.isEmpty(islatest+"") || StringUtils.isBlank(islatest+"")) {
				response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
				response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
				response.put(MigratorConstants.KEY_MESSAGE, "islatest parameter not found");
				httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
			}else if(StringUtils.isEmpty(sourcelocation) || StringUtils.isBlank(sourcelocation)) {
				response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
				response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
				response.put(MigratorConstants.KEY_MESSAGE, "sourcelocation parameter not found");
				httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
			}else {
				LOGGER.info("has include column {} {}",hasinclude, (hasinclude instanceof Boolean));
				if(MigratorConstants.XLS.equalsIgnoreCase(fileExt) || MigratorConstants.XLSX.equalsIgnoreCase(fileExt)) {
					final long count = excelService.countRecords(file,hasinclude.booleanValue());
					LOGGER.info("total excel records {}",count);
					response =csvService.saveCsvDetails(file.getOriginalFilename(), pdfdestination, nativedestination, localpdflocation, localnativelocation, 0, 0, count, islatest,metadtatemplateid,file.getInputStream(),nativemetadtatemplateid,sourcelocation);
					httpServletResponse.setStatus(HttpStatus.SC_OK);
				}else {
					final long csvRecordsCount = csvService.countCSVRecords(file.getInputStream(),hasinclude.booleanValue());
					LOGGER.info("total csv recodds {}",csvRecordsCount);
					response =csvService.saveCsvDetails(file.getOriginalFilename(), pdfdestination, nativedestination, localpdflocation, localnativelocation, 0, 0, csvRecordsCount, islatest,metadtatemplateid,file.getInputStream(),nativemetadtatemplateid,sourcelocation);
					httpServletResponse.setStatus(HttpStatus.SC_OK);
				}
			}
		} catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method migrateCsvRecords {}",ioException);
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.put(MigratorConstants.KEY_MESSAGE, ioException.getMessage());
			httpServletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
		return response.toString();
	}
	
	/**
	 * Send upload messages.
	 *
	 * @param file the file
	 * @param pdfdestination the pdfdestination
	 * @param nativedestination the nativedestination
	 * @param islatest the islatest
	 * @param savedcsvobject the savedcsvobject
	 * @param sourcelocation the sourcelocation
	 * @param hasinclude the hasincludecolumn
	 * @param httpServletResponse the http servlet response
	 * @return the string
	 */
	@PostMapping(value = {"/senduplaodmessage"})
	public String sendUploadMessages(@RequestParam MultipartFile file, @RequestParam String pdfdestination, @RequestParam String nativedestination,
			 @RequestParam boolean islatest,@RequestParam String savedcsvobject,@RequestParam(required = false) String sourcelocation,
			 @RequestParam(required = false) boolean hasinclude,final HttpServletResponse httpServletResponse) {
		JSONObject response = new JSONObject();
		if(file == null) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_MESSAGE, "csvfile parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else if(null==pdfdestination || StringUtils.isEmpty(pdfdestination) || StringUtils.isBlank(pdfdestination)) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_MESSAGE, "pdfdestination parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else if(null==nativedestination || StringUtils.isEmpty(nativedestination) || StringUtils.isBlank(nativedestination)) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_MESSAGE, "nativedestination parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else if(StringUtils.isEmpty(islatest+"") || StringUtils.isBlank(islatest+"")) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_MESSAGE, "islatest parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else if(StringUtils.isEmpty(savedcsvobject) || StringUtils.isBlank(savedcsvobject)) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_MESSAGE, "savedcsvobject parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else if(StringUtils.isEmpty(sourcelocation) || StringUtils.isBlank(sourcelocation)) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_MESSAGE, "sourcelocation parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else {
			final JSONObject savedCsvObject = new JSONObject(savedcsvobject);
			try {
				final String fileExt = FileNameUtils.getExtension(file.getOriginalFilename());
				if(MigratorConstants.XLS.equalsIgnoreCase(fileExt) || MigratorConstants.XLSX.equalsIgnoreCase(fileExt)) {
					LOGGER.info("excel savedobj {}",savedcsvobject);
					excelService.sendUploadMessage(savedCsvObject, file, islatest, pdfdestination, nativedestination,sourcelocation,hasinclude);
					response.put(MigratorConstants.KEY_MESSAGE, "Messages Sent");
					httpServletResponse.setStatus(HttpStatus.SC_OK);
				}else {
					csvService.sendUploadMessage(savedCsvObject, file.getInputStream(), islatest, pdfdestination, nativedestination,sourcelocation,hasinclude);
					response.put(MigratorConstants.KEY_MESSAGE, "Messages Sent");
					httpServletResponse.setStatus(HttpStatus.SC_OK);
				}
			} catch (IOException ioException) {
				httpServletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				response.put(MigratorConstants.KEY_MESSAGE, ioException.getMessage());
				LOGGER.error("ioException occured while sending message {}",ioException);
			}
		}
		return response.toString();
	}
	
	/**
	 * Creates the folder.
	 *
	 * @param parentnode the parentnode
	 * @param foldername the foldername
	 * @param httpServletResponse the http servlet response
	 * @return the string
	 */
	@PostMapping(value = {"/createfolder"},produces = MediaType.APPLICATION_JSON_VALUE)
	public String createFolder(@RequestParam String parentnode,@RequestParam String foldername,final HttpServletResponse httpServletResponse) {
		JSONObject response = new JSONObject();
		final ConfigurationDetails configurationDetails = configurationsDetailsRepo.findByAppname(MigratorConstants.ALFRESCO);
		if(null==parentnode || StringUtils.isEmpty(parentnode) || StringUtils.isBlank(parentnode)) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_MESSAGE, "parent node id parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else if(null==foldername || StringUtils.isEmpty(foldername) || StringUtils.isBlank(foldername)) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_MESSAGE, "folder name parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else {
			if(null!=configurationDetails) {
				final String alfrescoBaseUrl = MigrationUtils.generateAlfrescoURL(configurationDetails);
				response = alfrescoService.createFolder(alfrescoBaseUrl, parentnode, foldername, configurationDetails.getUsername(), configurationDetails.getPassword());
				httpServletResponse.setStatus(HttpStatus.SC_OK);
			}else {
				response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_NOT_FOUND);
				response.put(MigratorConstants.KEY_MESSAGE, "alfresco configurations not found");
				httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
			}
		}
		return response.toString();
	}
	
	/**
	 * Re upload.
	 *
	 * @param target the target
	 * @param properties the properties
	 * @param filelocation the filelocation
	 * @param filetype the filetype
	 * @param isnative the isnative
	 * @param migratedrecordid the migratedrecordid
	 * @param sourcelocation the sourcelocation
	 * @param localfilelocation the localfilelocation
	 * @param httpServletResponse the http servlet response
	 * @return the string
	 */
	@PostMapping(value = {"/reupload"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String reUpload(@RequestParam String target,@RequestParam String properties,@RequestParam String filelocation,@RequestParam String filetype,
			@RequestParam boolean isnative,@RequestParam String migratedrecordid, @RequestParam String sourcelocation,
			@RequestParam String localfilelocation,final HttpServletResponse httpServletResponse) {
		JSONObject response = new JSONObject();
		if(StringUtils.isEmpty(target) || StringUtils.isBlank(target)) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_NOT_FOUND);
			response.put(MigratorConstants.KEY_MESSAGE, "target parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else if(StringUtils.isEmpty(properties) || StringUtils.isBlank(properties)) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_NOT_FOUND);
			response.put(MigratorConstants.KEY_MESSAGE, "properties parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else if(StringUtils.isEmpty(filelocation) || StringUtils.isBlank(filelocation)) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_NOT_FOUND);
			response.put(MigratorConstants.KEY_MESSAGE, "fileLocation parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else if(StringUtils.isEmpty(filetype) || StringUtils.isBlank(filetype)) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_NOT_FOUND);
			response.put(MigratorConstants.KEY_MESSAGE, "filetype parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else if(StringUtils.isEmpty(isnative+"") || StringUtils.isBlank(isnative+"")) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_NOT_FOUND);
			response.put(MigratorConstants.KEY_MESSAGE, "isnative parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else if(StringUtils.isEmpty(sourcelocation) || StringUtils.isBlank(sourcelocation)) {
			response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_NOT_FOUND);
			response.put(MigratorConstants.KEY_MESSAGE, "sourcelocation parameter not found");
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
		}else {
			final File file = new File(filelocation);
			if((null == file || !file.exists()) && !MigratorConstants.AMAZON_S3.equalsIgnoreCase(sourcelocation)) {
				response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_NOT_FOUND);
				response.put(MigratorConstants.KEY_MESSAGE, "file "+filelocation+" not found in path");
				httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
			}else {
				if(file.isDirectory() && !MigratorConstants.AMAZON_S3.equalsIgnoreCase(sourcelocation)) {
					response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_INTERNAL_SERVER_ERROR);
					response.put(MigratorConstants.KEY_MESSAGE, "file "+filelocation+" is a directory not a file");
					httpServletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				}else {
					LOGGER.info("uplaod file into alfresco");
					try {
						final JSONObject metadata = new JSONObject(properties);
						LOGGER.info("metadata {}",metadata);
						LOGGER.info("target {}",target);
						LOGGER.info("filetype {}",filetype);
						LOGGER.info("file {}",file);
						final ConfigurationDetails configurationDetails = configurationsDetailsRepo.findByAppname(MigratorConstants.ALFRESCO);
						if(null == configurationDetails) {
							response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_NOT_FOUND);
							response.put(MigratorConstants.KEY_MESSAGE, "Alfresco COnfigurations not found");
							httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
						}else {
							final String alfrescoBaseURL = MigrationUtils.generateAlfrescoURL(configurationDetails);
							if(MigratorConstants.LOCAL.equalsIgnoreCase(sourcelocation)) {
								response =alfrescoService.UploadFile(file, metadata, alfrescoBaseURL, isnative, filetype, target, configurationDetails.getUsername(), configurationDetails.getPassword());
								LOGGER.info("alfreso upload response {}",response);
								if(response.getInt(MigratorConstants.KEY_STATUS_CODE)==HttpStatus.SC_OK) {
									final JSONObject updatedRecord = csvService.updateMetadataReuplaodDetails(migratedrecordid, response);
									response.put(MigratorConstants.KEY_DETAILS,updatedRecord.getString(MigratorConstants.KEY_DETAILS));
								}else {
									httpServletResponse.setStatus(response.getInt(MigratorConstants.KEY_STATUS_CODE));
									final MigratedRecord migratedRecord = migratedRecordRepo.findBycsvfileid(Long.parseLong(migratedrecordid));
									migratedRecord.setCsvfileid(Long.parseLong(migratedrecordid));
									migratedRecord.setStatus(String.valueOf(response.getInt(MigratorConstants.KEY_STATUS_CODE)));
									migratedRecord.setMessage(response.getString(MigratorConstants.KEY_MESSAGE));
									migratedRecordRepo.save(migratedRecord);
								}
							}else if(MigratorConstants.AMAZON_S3.equalsIgnoreCase(sourcelocation)) {
								LOGGER.info("filelocation {}",localfilelocation);
								final AmazonS3 amazonS3 = amazonServices.gets3Session();
								final S3Object object = amazonServices.readS3Object(amazonS3, localfilelocation);
								final MigratedRecord migratedRecord = migratedRecordRepo.findBycsvfileid(Long.parseLong(migratedrecordid));
								if(null == object) {
									migratedRecord.setCsvfileid(Long.parseLong(migratedrecordid));
									migratedRecord.setStatus(HttpStatus.SC_NOT_FOUND+"");
									migratedRecord.setMessage(localfilelocation+" not found in s3 bucket location");
									migratedRecordRepo.save(migratedRecord);
									response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_NOT_FOUND);
									response.put(MigratorConstants.KEY_MESSAGE, localfilelocation+" not found in s3 bucket location");
									httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
								}else {
									final JSONObject alfresponse =alfrescoService.createNode(alfrescoBaseURL, metadata, filetype, object.getObjectContent(), target, object.getKey(), configurationDetails.getUsername(), configurationDetails.getPassword());
									migratedRecord.setStatus(alfresponse.getInt(MigratorConstants.KEY_STATUS)+"");
									migratedRecord.setMessage(alfresponse.getString(MigratorConstants.KEY_STATUS_MESSAGE));
									migratedRecord.setPropertiesstatus(alfresponse.getInt(MigratorConstants.KEY_PROPERTIES_STATUS)+"");
									migratedRecord.setPropertiesmessage(alfresponse.getString(MigratorConstants.KEY_PROPERTIES_MESSAGE));
									migratedRecord.setNoderef(alfresponse.getString(MigratorConstants.KEY_NODE_REF));
									migratedRecordRepo.save(migratedRecord);
									response.put(MigratorConstants.KEY_STATUS_CODE, alfresponse.getInt(MigratorConstants.KEY_STATUS));
									response.put(MigratorConstants.KEY_STATUS, alfresponse.getInt(MigratorConstants.KEY_STATUS));
									response.put(MigratorConstants.KEY_MESSAGE, alfresponse.getString(MigratorConstants.KEY_STATUS_MESSAGE));
									httpServletResponse.setStatus(alfresponse.getInt(MigratorConstants.KEY_STATUS));
								}
							}
						}
					}catch (Exception exception) {
						httpServletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
						response.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_INTERNAL_SERVER_ERROR);
						response.put(MigratorConstants.KEY_MESSAGE, exception.getStackTrace());
						LOGGER.error("exception occured while reuploading document {}",exception);
					}
				}
			}
		}
		return response.toString();
	}
	
	/**
	 * Delete selected records.
	 *
	 * @param records the records
	 * @param httpServletResponse the http servlet response
	 * @return the string
	 */
	@DeleteMapping(value = {"/deleterecords"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String deleteSelectedRecords(@RequestParam(required = false) String records,final HttpServletResponse httpServletResponse) {
		JSONObject response = new JSONObject();
		try {
			
			int deletecount=0;
			int notdeletecount=0;
			if(StringUtils.isEmpty(records) || StringUtils.isBlank(records)) {
				httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
				response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
				response.put(MigratorConstants.KEY_MESSAGE, "records parameter is missing");
			}else {
				final JSONArray recordArray = new JSONArray(records);
				LOGGER.info("records array {}",recordArray);
				if(recordArray.length()==0) {
					httpServletResponse.setStatus(HttpStatus.SC_OK);
					response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
					response.put(MigratorConstants.KEY_MESSAGE, "No records selected to delete");
				}else {
					final ConfigurationDetails configurationDetails = configurationsDetailsRepo.findByAppname(MigratorConstants.ALFRESCO);
					if(null == configurationDetails) {
						httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
						response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_NOT_FOUND);
						response.put(MigratorConstants.KEY_MESSAGE, "Alfresco Configurations not found contact administrator");
					}else {
						final String alfrescoBaseURL = MigrationUtils.generateAlfrescoURL(configurationDetails);
						for(int index=0;index<recordArray.length();index++) {
							LOGGER.info("{}",recordArray.getJSONObject(index));
							final JSONObject detailJson = recordArray.getJSONObject(index);
							final String nodeRef = detailJson.has(MigratorConstants.KEY_NODE_REF_LOWERCASE)? detailJson.getString(MigratorConstants.KEY_NODE_REF_LOWERCASE):StringUtils.EMPTY;
							if(StringUtils.isEmpty(nodeRef) || StringUtils.isBlank(nodeRef)) {
								final Long uniqueId = detailJson.getLong(MigratorConstants.KEY_RECORD_ID);
								LOGGER.info("log record value {}",uniqueId);
								final MigratedRecord migratedRecord = migratedRecordRepo.findBycsvfileid(uniqueId);
								LOGGER.info("record details {}",migratedRecord.toString());
								if(null!=migratedRecord) {
									migratedRecordRepo.deleteBycsvfileid(migratedRecord.getCsvfileid());
								}
								deletecount+=1;
							}else {
								final String nodeId = MigrationUtils.toNodeId(nodeRef);
								response = alfrescoService.deleteNode(alfrescoBaseURL, nodeId, Boolean.FALSE.toString(), configurationDetails.getUsername(), configurationDetails.getPassword());
								final int status = response.getInt(MigratorConstants.KEY_STATUS);
								if(HttpStatus.SC_OK == status) {
									final Long uniqueId = detailJson.getLong(MigratorConstants.KEY_RECORD_ID);
									LOGGER.info("log record value {}",uniqueId);
									final MigratedRecord migratedRecord = migratedRecordRepo.findBycsvfileid(uniqueId);
									LOGGER.info("------record details {}",migratedRecord.toString());
									if(null!=migratedRecord) {
										migratedRecordRepo.deleteBycsvfileid(migratedRecord.getCsvfileid());
									}
									deletecount+=1;
								}else if(HttpStatus.SC_NOT_FOUND == status) {
									final Long uniqueId = detailJson.getLong(MigratorConstants.KEY_RECORD_ID);
									final MigratedRecord migratedRecord = migratedRecordRepo.findBycsvfileid(uniqueId);
									LOGGER.info("------record details {}",migratedRecord.toString());
									if(null!=migratedRecord) {
										migratedRecordRepo.deleteBycsvfileid(migratedRecord.getCsvfileid());
									}
									deletecount+=1;
								} else {
									LOGGER.info("-------->status {}",status);
									notdeletecount+=1;
								}
							}
						}
						httpServletResponse.setStatus(HttpStatus.SC_OK);
						response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
						response.put(MigratorConstants.KEY_MESSAGE, deletecount+" records deleted successfully "+notdeletecount+" skipped");
					}
				}
			}
		}catch (Exception exception) {
			LOGGER.error("exception occured while executing method deleteSelectedRecords {}",exception);
			httpServletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.put(MigratorConstants.KEY_MESSAGE, exception.getMessage());
		}
		return response.toString();
	}
	
	/**
	 * Delete csv file.
	 *
	 * @param csvid the csvid
	 * @param httpServletResponse the http servlet response
	 * @return the string
	 */
	@DeleteMapping(value = {"/deletecsv"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String deleteCsvFile(@RequestParam(required = false) String csvid,final HttpServletResponse httpServletResponse) {
		final JSONObject response = new JSONObject();
		if(StringUtils.isEmpty(csvid) || StringUtils.isBlank(csvid)) {
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_MESSAGE, "csvid parameter is missing");
		}else {
			final CSVFiles csvFiles = csvFilesRepo.findByuniqueid(Long.parseLong(csvid));
			if(null!=csvFiles) {
				LOGGER.info("csv files {}",csvFiles.toString());
				csvFilesRepo.delete(csvFiles);
				httpServletResponse.setStatus(HttpStatus.SC_OK);
				response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
				response.put(MigratorConstants.KEY_MESSAGE, csvFiles.getCsvname()+" deleted successfully");
			}else {
				httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
				response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_NOT_FOUND);
				response.put(MigratorConstants.KEY_MESSAGE, "CSV not found");
			}
		}
		return response.toString();
	}
	
	/**
	 * Calculate time taken.
	 *
	 * @param csvid the csvid
	 * @param httpServletResponse the http servlet response
	 * @return the string
	 */
	@GetMapping(value = {"/calculatetimetaken"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String calculateTimeTaken(@RequestParam String csvid,final HttpServletResponse httpServletResponse) {
		final JSONObject response = new JSONObject();
		if(StringUtils.isEmpty(csvid) || StringUtils.isBlank(csvid)) {
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_MESSAGE, "csvid parameter is missing");
		}else {
			final List<MigratedRecord> ascSortRecords = new ArrayList<>();
			LocalDateTime startdate,endate;
			Page<MigratedRecord> page = null;
			page = migratedRecordRepo.findBycsvuniqueid(Long.parseLong(csvid),PageRequest.of(0, 1,Sort.by(Sort.Direction.ASC, DatabaseConstants.START_DATE_TIME)));
			ascSortRecords.addAll(page.getContent());
			startdate = ascSortRecords.get(0).getStartdate();
			page = migratedRecordRepo.findBycsvuniqueid(Long.parseLong(csvid),PageRequest.of(0, 1,Sort.by(Sort.Direction.DESC, DatabaseConstants.END_DATE_TIME)));
			ascSortRecords.clear();
			ascSortRecords.addAll(page.getContent());
			endate = ascSortRecords.get(0).getEnddate();
			final String timeTaken = MigrationUtils.calculateTimeTaken(startdate, endate);
			LOGGER.info("time taken : {}",timeTaken);
			httpServletResponse.setStatus(HttpStatus.SC_OK);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
			response.put(MigratorConstants.KEY_TIME_TAKEN, timeTaken);
		}
		return response.toString();
	}
	
	/**
	 * Download.
	 *
	 * @param csvid the csvid
	 * @return the response entity
	 */
	@GetMapping(value = {"/downloadcsv"})
	public ResponseEntity<Resource> download(@RequestParam String csvid,@RequestParam String exporttype){
		final List<MigratedRecord> records = new ArrayList<>();
		final CSVFiles csvFiles = csvFilesRepo.findByuniqueid(Long.parseLong(csvid));
//		final TemplateDefinition templateDefinition = templateDefinitionRepo.findBytemplateid(csvFiles.getTemplateid());
		final PageRequest pageRequest= PageRequest.of(0, Integer.MAX_VALUE,Sort.by(Sort.Direction.ASC, DatabaseConstants.CSV_RECORD_NUMBER));
		Page<MigratedRecord> page= null;
		if(MigratorConstants.EXPORT_ALL_CSV.equalsIgnoreCase(exporttype)) {
			page = migratedRecordRepo.findBycsvuniqueid(Long.parseLong(csvid),pageRequest);
		}else if(MigratorConstants.EXPORT_SUCCESS_CSV.equalsIgnoreCase(exporttype)) {
			page = migratedRecordRepo.findAll(MigratorSpecifications.csvIdSpec(Long.parseLong(csvid)).and(MigratorSpecifications.okSpec()),pageRequest);
		}else {
			page = migratedRecordRepo.findAll(MigratorSpecifications.csvIdSpec(Long.parseLong(csvid)).and(MigratorSpecifications.notOkSpec()),pageRequest);
		}
		records.addAll(page.getContent());
		
		final String csvName = csvFiles.getCsvname();
		LOGGER.info("csv file name {} extension {}",FileNameUtils.getBaseName(csvName),FileNameUtils.getExtension(csvName));
		InputStreamResource resource=null;
		HttpHeaders headers = new HttpHeaders();
		File tempFile =null;
		try {
			tempFile=File.createTempFile(FileNameUtils.getBaseName(csvName)+"-report","."+MigratorConstants.CSV);
			Field[] columns= MigratedRecord.class.getDeclaredFields();
			final List<String> header = new ArrayList<>();
			final Builder builder = CSVFormat.Builder.create().setNullString("-");
			CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tempFile), builder.build());
			for(Field field: columns) {
				if(!"serialVersionUID".equalsIgnoreCase(field.getName())) {
					header.add(field.getName());
					csvPrinter.print(field.getName());
				}
			}
			csvPrinter.println();
			for(final MigratedRecord migratedRecord: records) {
				for(String colName: header) {
					final JSONObject recordJson = new JSONObject(migratedRecord.toString());
					csvPrinter.print(recordJson.has(colName)?recordJson.get(colName)+"":"-");
				}
				csvPrinter.println();
			}
			LOGGER.info("{}",String.join(MigratorConstants.COMMA,header));
			resource = new InputStreamResource(new FileInputStream(tempFile));
	        headers.add("Content-disposition", "attachment; filename="+ tempFile.getName());
	        csvPrinter.close();
	        tempFile.deleteOnExit();
		} catch (FileNotFoundException fileNotFoundException) {
			LOGGER.error("fileNotFoundException occured while downloading file {}",fileNotFoundException);
		} catch (IOException ioException) {
			LOGGER.error("ioException occured while downloading file {}",ioException);
		}finally {
			if(null!=tempFile) {
				tempFile.delete();
			}
		}

	    return ResponseEntity.ok()
	    		.headers(headers)
	            .contentLength(tempFile.length())
	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
	            .body(resource);
	}
	
	/**
	 * Reupload failed.
	 *
	 * @param csvid the csvid
	 * @param httpServletResponse the http servlet response
	 * @return the string
	 */
	@RequestMapping(value = {"/reuploadfailed"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String reuploadFailed(@RequestParam String csvid,final HttpServletResponse httpServletResponse) {
		final JSONObject response = new JSONObject();
		failedCounter.set(0);
		successCounter.set(0);
		if(StringUtils.isEmpty(csvid) || StringUtils.isBlank(csvid)) {
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_MESSAGE, "csvid parameter is missing");
		}else {
			final CSVFiles csvFiles = csvFilesRepo.findByuniqueid(Long.parseLong(csvid));
			final PageRequest pageRequest= PageRequest.of(0, Integer.MAX_VALUE,Sort.by(Sort.Direction.ASC, DatabaseConstants.CSV_RECORD_NUMBER));
			final Page<MigratedRecord> page = migratedRecordRepo.findAll(MigratorSpecifications.csvIdSpec(Long.parseLong(csvid)).and(MigratorSpecifications.notOkSpec()),pageRequest);
			final List<MigratedRecord> failedRecords = new ArrayList<>();
			failedRecords.addAll(page.getContent());
			final List<List<MigratedRecord>> choppedList = new ArrayList<>();
			choppedList.addAll(chopList(failedRecords, size));
			LOGGER.info("failed size {}",page.getContent().size());
			LOGGER.info("chopped list size {}",choppedList.size());
			counterService.setTotal(failedRecords.size());
			counterService.setFailed(0);
			counterService.setSuccess(0);
			final ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
			final ConfigurationDetails configurationDetails = configurationsDetailsRepo.findByAppname(MigratorConstants.ALFRESCO);
			final String username = configurationDetails.getUsername();
			final String password = configurationDetails.getPassword();
			final String alfrescoURL = MigrationUtils.generateAlfrescoURL(configurationDetails);
			for (int i = 0; i < choppedList.size(); i++) {
				MultiTaskEx mte = new MultiTaskEx(choppedList.get(i),csvFiles,amazonServices,migratedRecordRepo,counterService,alfrescoURL,username,password);
				executor.execute(mte);
			}
			executor.shutdown();
			httpServletResponse.setStatus(HttpStatus.SC_OK);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
			response.put(MigratorConstants.KEY_MESSAGE, "Finished re-uploading documents");
		}
		return response.toString();
	}
	
	/**
	 * Gets the reupload status.
	 *
	 * @return the reupload status
	 */
	@GetMapping(value = {"/reuploadstatus"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getReuploadStatus() {
		final JSONArray valuesJson = new JSONArray();
		valuesJson.put(new JSONObject(counterService.toString()));
		final JSONObject statusObj = new JSONObject();
		statusObj.put(MigratorConstants.KEY_LIST, valuesJson);
		LOGGER.info("{}",statusObj);
		return statusObj.toString();
	}
	
	/**
	 * Reset counter.
	 *
	 * @return the string
	 */
	@GetMapping(value = {"/resetcounter"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String resetCounter() {
		final JSONObject response = new JSONObject();
		counterService.setFailed(0);
		counterService.setSuccess(0);
		counterService.setTotal(0);
		response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
		response.put(MigratorConstants.KEY_MESSAGE, "Counter Reset Successfull");
		return response.toString();
	}
	
	/**
	 * Chop list.
	 *
	 * @param list the list
	 * @param L the l
	 * @return the list
	 */
	public List<List<MigratedRecord>> chopList(List<MigratedRecord> list, final int L) {
	    final List<List<MigratedRecord>> parts = new ArrayList<>();
	    final int N = list.size();
	    for (int i = 0; i < N; i += L) {
	        parts.add(new ArrayList<>(
	            list.subList(i, Math.min(N, i + L)))
	        );
	    }
	    return parts;
	}
	
	/**
	 * The Class MultiTaskEx.
	 */
	public class MultiTaskEx implements Runnable{
		
		/** The migrated record. */
		List<MigratedRecord> migratedRecord =null;
		
		/** The csv files. */
		CSVFiles csvFiles=null;
		
		/** The amazon services. */
		AmazonServices amazonServices = null;
		
		/** The migrated record repo. */
		MigratedRecordRepo migratedRecordRepo = null;
		
		/** The counter service. */
		CounterService counterService;
		
		/** The alfresco URL. */
		String alfrescoURL;
		
		/** The user name. */
		String userName;
		
		/** The password. */
		String password;

		/**
		 * Instantiates a new multi task ex.
		 *
		 * @param numbers the numbers
		 * @param csvFiles the csv files
		 * @param amazonServices the amazon services
		 * @param migratedRecordRepo the migrated record repo
		 * @param counterService the counter service
		 * @param alfrescoURL 
		 * @param password 
		 * @param username 
		 */
		public MultiTaskEx(final List<MigratedRecord> numbers,final CSVFiles csvFiles, final AmazonServices amazonServices, 
				final MigratedRecordRepo migratedRecordRepo, final CounterService counterService, final String alfrescoURL, final String username, final String password) {
			this.migratedRecord = numbers;
			this.csvFiles = csvFiles;
			this.amazonServices = amazonServices;
			this.migratedRecordRepo = migratedRecordRepo;
			this.counterService = counterService;
			this.alfrescoURL = alfrescoURL;
			this.userName = username;
			this.password = password;
		}


		/**
		 * Run.
		 */
		@Override
		public void run() {
			for(MigratedRecord record: this.migratedRecord) {
				final String fileLocation = record.getLocalfilelocation();
				final String fileName = record.getFilename();
				if(StringUtils.isBlank(fileName) || StringUtils.isEmpty(fileName)) {
					record.setStatus(HttpStatus.SC_BAD_REQUEST+"");
					record.setMessage("filename missing in csv record");
					record.setPropertiesstatus(HttpStatus.SC_NOT_FOUND+"");
					record.setPropertiesmessage("filename missing in csv record");
					this.migratedRecordRepo.save(record);
					this.counterService.setFailed(failedCounter.incrementAndGet());
				}else {
					LOGGER.info("source location {}",this.csvFiles.getSourcelocation());
					final AmazonS3 amazonS3 = this.amazonServices.gets3Session();
					if(record.isIsnative()) {
						final String destination = this.csvFiles.getNativedestination();
						final String fileNamePath = fileLocation+"/"+fileName;
						if(MigratorConstants.AMAZON_S3.equalsIgnoreCase(this.csvFiles.getSourcelocation())) {
							final S3Object s3Object= this.amazonServices.readS3Object(amazonS3, fileNamePath);
							if(null!=s3Object) {
								LOGGER.info("filename--> {} filesize--> {} metadata--> {}",s3Object,s3Object.getObjectMetadata().getContentLength(),s3Object.getObjectMetadata());
								final long nativeTemplateId = this.csvFiles.getNativetemplateid();
								final TemplateDefinition templateDefinition = templateDefinitionRepo.findBytemplateid(nativeTemplateId);
								final String fileType = templateDefinition.getFiletypeqname();
								final String nativeDestination = this.csvFiles.getNativedestination();
								final JSONObject properties = new JSONObject(record.getProperties());
								final JSONObject alfrescoResponse = alfrescoService.createNode(this.alfrescoURL, properties, fileType, s3Object.getObjectContent(), nativeDestination, s3Object.getKey(), this.userName, this.password);
								LOGGER.info("upload response pdf {}",alfrescoResponse);
								record.setStatus(alfrescoResponse.getInt(MigratorConstants.KEY_STATUS)+"");
								record.setMessage(alfrescoResponse.getString(MigratorConstants.KEY_STATUS_MESSAGE));
								record.setPropertiesstatus(alfrescoResponse.getInt(MigratorConstants.KEY_PROPERTIES_STATUS)+"");
								record.setPropertiesmessage(alfrescoResponse.getString(MigratorConstants.KEY_PROPERTIES_MESSAGE));
								record.setFilesize(s3Object.getObjectMetadata().getContentLength());
								if(HttpStatus.SC_OK == alfrescoResponse.getInt(MigratorConstants.KEY_STATUS)) {
									record.setNoderef(alfrescoResponse.getString(MigratorConstants.KEY_NODE_REF));
									this.counterService.setSuccess(successCounter.incrementAndGet());
								}else {
									this.counterService.setFailed(failedCounter.incrementAndGet());
								}
								this.migratedRecordRepo.save(record);
								LOGGER.info("not native {}",this.counterService);
							}else {
								LOGGER.info("file not found {}/{}",fileLocation,fileName);
								record.setStatus(HttpStatus.SC_NOT_FOUND+"");
								record.setMessage(fileLocation+"/"+fileName+" not found in s3 bucket");
								record.setPropertiesstatus(HttpStatus.SC_NOT_FOUND+"");
								record.setPropertiesmessage(fileLocation+"/"+fileName+" not found in s3 bucket");
								this.migratedRecordRepo.save(record);
								this.counterService.setFailed(failedCounter.incrementAndGet());
								LOGGER.info("{}",this.counterService);
							}
							LOGGER.info("native {} {} ",destination,fileNamePath);
						}
					}else {
						final String destination = this.csvFiles.getNativedestination();
						final String fileNamePath = fileLocation+"/"+fileName;
						if(MigratorConstants.AMAZON_S3.equalsIgnoreCase(this.csvFiles.getSourcelocation())) {
							final S3Object s3Object= this.amazonServices.readS3Object(amazonS3, fileNamePath);
							if(null!=s3Object) {
								LOGGER.info("filename--> {} filesize--> {} metadata--> {}",s3Object,s3Object.getObjectMetadata().getContentLength(),s3Object.getObjectMetadata());
								final long templateId = this.csvFiles.getTemplateid();
								final TemplateDefinition templateDefinition = templateDefinitionRepo.findBytemplateid(templateId);
								final String fileType = templateDefinition.getFiletypeqname();
								final String pdfDestination = this.csvFiles.getPdfDestination();
								final JSONObject properties = new JSONObject(record.getProperties());
								final JSONObject alfrescoResponse = alfrescoService.createNode(this.alfrescoURL, properties, fileType, s3Object.getObjectContent(), pdfDestination, s3Object.getKey(), this.userName, this.password);
								LOGGER.info("upload response pdf {}",alfrescoResponse);
								record.setStatus(alfrescoResponse.getInt(MigratorConstants.KEY_STATUS)+"");
								record.setMessage(alfrescoResponse.getString(MigratorConstants.KEY_STATUS_MESSAGE));
								record.setPropertiesstatus(alfrescoResponse.getInt(MigratorConstants.KEY_PROPERTIES_STATUS)+"");
								record.setPropertiesmessage(alfrescoResponse.getString(MigratorConstants.KEY_PROPERTIES_MESSAGE));
								record.setFilesize(s3Object.getObjectMetadata().getContentLength());
								if(HttpStatus.SC_OK == alfrescoResponse.getInt(MigratorConstants.KEY_STATUS)) {
									record.setNoderef(alfrescoResponse.getString(MigratorConstants.KEY_NODE_REF));
									this.counterService.setSuccess(successCounter.incrementAndGet());
								}else {
									this.counterService.setFailed(failedCounter.incrementAndGet());
								}
								this.migratedRecordRepo.save(record);
								LOGGER.info("not native {}",this.counterService);
							}else {
								LOGGER.info("file not found {}/{}",fileLocation,fileName);
								record.setStatus(HttpStatus.SC_NOT_FOUND+"");
								record.setMessage(fileLocation+"/"+fileName+" not found in s3 bucket");
								record.setPropertiesstatus(HttpStatus.SC_NOT_FOUND+"");
								record.setPropertiesmessage(fileLocation+"/"+fileName+" not found in s3 bucket");
								this.migratedRecordRepo.save(record);
								this.counterService.setFailed(failedCounter.incrementAndGet());
								LOGGER.info("not native {}",this.counterService);
							}
							LOGGER.info("non native {} {}",destination,fileNamePath);
						}
					}
				}
			}
		}
	}
}
