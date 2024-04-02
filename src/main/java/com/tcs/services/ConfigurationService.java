/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tcs.constants.DatabaseConstants;
import com.tcs.constants.MigratorConstants;
import com.tcs.database.ConfigurationDetails;
import com.tcs.database.Configurations;
import com.tcs.database.FileColumn;
import com.tcs.database.TemplateDefinition;
import com.tcs.database.TemplateDetails;
import com.tcs.repositories.ConfigurationRepo;
import com.tcs.repositories.ConfigurationsDetailsRepo;
import com.tcs.repositories.FileColumnRepo;
import com.tcs.repositories.TemplateDefinitionRepo;
import com.tcs.repositories.TemplateDetailsRepo;
import com.tcs.utils.MigrationUtils;

/**
 * The Class ConfigurationService.
 */
@Service
public class ConfigurationService {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class);
	
	/** The configuration repo. */
	@Autowired
	ConfigurationRepo configurationRepo;
	
	/** The configurations details repo. */
	@Autowired
	ConfigurationsDetailsRepo configurationsDetailsRepo;
	
	/** The alfresco service. */
	@Autowired
	AlfrescoService alfrescoService;
	
	/** The template definition repo. */
	@Autowired
	TemplateDefinitionRepo templateDefinitionRepo;
	
	/** The template details repo. */
	@Autowired
	TemplateDetailsRepo templateDetailsRepo;
	
	/** The file column repo. */
	@Autowired
	FileColumnRepo fileColumnRepo;
	
	/**
	 * Save alf configurations.
	 *
	 * @param protocol the protocol
	 * @param host the host
	 * @param port the port
	 * @param appname the appname
	 * @param username the username
	 * @param password the password
	 * @param hasalfconfig the hasalfconfig
	 * @param configid the configid
	 * @return the JSON object
	 */
	public JSONObject saveAlfConfigurations(final String protocol,final String host,final int port,final String appname, final String username, final String password, 
			final boolean hasalfconfig, final Long configid) {
		final JSONObject responseObj = new JSONObject();
		try {
			final Configurations configurations = new Configurations();
			final ConfigurationDetails configurationDetails = new ConfigurationDetails();
			configurations.setConfigurationname(MigratorConstants.ALFRESCO);
			Configurations savedConf = configurationRepo.save(configurations);
			if(null!=savedConf) {
				final Long confId = savedConf.getConfid();
				LOGGER.info("alfresco conf id generated : {}",confId);
				configurationDetails.setConfigurations(savedConf);
				configurationDetails.setAppname(appname);
				configurationDetails.setHost(host);
				configurationDetails.setPort(port);
				configurationDetails.setProtocol(protocol);
				configurationDetails.setUsername(username);
				configurationDetails.setPassword(password);
				if(hasalfconfig) {
					configurationDetails.setConfigid(configid);
				}
				final ConfigurationDetails savedDetails= configurationsDetailsRepo.save(configurationDetails);
				if(null!=savedDetails) {
					responseObj.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
					responseObj.put(MigratorConstants.KEY_MESSAGE, "Alfresco details saved cuccessfully");
					responseObj.put(MigratorConstants.KEY_DETAILS, new JSONObject(savedDetails));
				}else {
					responseObj.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
					responseObj.put(MigratorConstants.KEY_MESSAGE, "failed to saved cuccessfully");
					responseObj.put(MigratorConstants.KEY_DETAILS, new JSONObject());
				}
				LOGGER.info("alf config saved {}",new JSONObject(savedDetails));
			}else {
				responseObj.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_NOT_IMPLEMENTED);
				responseObj.put(MigratorConstants.KEY_MESSAGE, "failed to saved cuccessfully");
				responseObj.put(MigratorConstants.KEY_DETAILS, new JSONObject());
			}
		}catch (Exception exception) {
			responseObj.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseObj.put(MigratorConstants.KEY_MESSAGE, exception.getMessage());
			responseObj.put(MigratorConstants.KEY_DETAILS, new JSONObject());
			LOGGER.error("exception occured while executing method saveActiveMQConfigurations {}",exception);
		}
		return responseObj;
	}
	
	/**
	 * Gets the alf configurations.
	 *
	 * @return the alf configurations
	 */
	public ConfigurationDetails getAlfConfigurations() {
		final ConfigurationDetails configurations = configurationsDetailsRepo.findByAppname(MigratorConstants.ALFRESCO);
		return configurations;
	}
	
	/**
	 * Validate alfresco connection.
	 *
	 * @return the string
	 */
	public String validateAlfrescoConnection() {
		final ConfigurationDetails configurations = configurationsDetailsRepo.findByAppname(MigratorConstants.ALFRESCO);
		JSONObject responseJson = null;
		if(null!=configurations) {
			final String URL = String.format(MigratorConstants.URL_TEMPLATE, configurations.getProtocol(),configurations.getHost(),configurations.getPort(),MigratorConstants.ALFRESCO);
			final String userName = configurations.getUsername();
			final String password = configurations.getPassword();
			responseJson = alfrescoService.validateConnection(URL, userName, password);
		}else {
			responseJson  = new JSONObject();
			responseJson.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_NOT_ACCEPTABLE);
			responseJson.put(MigratorConstants.KEY_MESSAGE, "failed to validate connection contact administrator");
		}
		LOGGER.info("completed validation of Alfresco connection");
		return responseJson.toString();
	}
	
	/**
	 * Gets the active MQ configurations.
	 *
	 * @return the active MQ configurations
	 */
	public ConfigurationDetails getActiveMQConfigurations() {
		final ConfigurationDetails configurations = configurationsDetailsRepo.findByAppname(MigratorConstants.ACTIVEMQ);
		return configurations;
	}
	
	/**
	 * Save active MQ configurations.
	 *
	 * @param protocol the protocol
	 * @param host the host
	 * @param port the port
	 * @param appname the appname
	 * @param username the username
	 * @param password the password
	 * @param hasalfconfig the hasalfconfig
	 * @param configid the configid
	 * @return the JSON object
	 */
	public JSONObject saveActiveMQConfigurations(final String protocol,final String host,final int port,final String appname, final String username, final String password, 
			final boolean hasalfconfig, final Long configid) {
		final JSONObject responseObj = new JSONObject();
		try {
			final Configurations configurations = new Configurations();
			final ConfigurationDetails configurationDetails = new ConfigurationDetails();
			configurations.setConfigurationname(MigratorConstants.ACTIVEMQ);
			final Configurations savedConf = configurationRepo.save(configurations);
			if(null != savedConf) {
				final Long confId = savedConf.getConfid();
				LOGGER.info("activemq conf id generated : "+confId);
				configurationDetails.setConfigurations(savedConf);
				configurationDetails.setAppname(appname);
				configurationDetails.setHost(host);
				configurationDetails.setPort(port);
				configurationDetails.setProtocol(protocol);
				configurationDetails.setUsername(username);
				configurationDetails.setPassword(password);
				if(hasalfconfig) {
					configurationDetails.setConfigid(configid);
				}
				final ConfigurationDetails savedDetails= configurationsDetailsRepo.save(configurationDetails);
				LOGGER.info("activemq config saved {}",new JSONObject(savedDetails));
				if(null != savedDetails) {
					responseObj.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
					responseObj.put(MigratorConstants.KEY_MESSAGE, "details saved cuccessfully");
					responseObj.put(MigratorConstants.KEY_DETAILS, new JSONObject(savedDetails));
				}else {
					responseObj.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_NOT_IMPLEMENTED);
					responseObj.put(MigratorConstants.KEY_MESSAGE, "failed to saved cuccessfully");
					responseObj.put(MigratorConstants.KEY_DETAILS, new JSONObject());
				}
			}else {
				responseObj.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_NOT_IMPLEMENTED);
				responseObj.put(MigratorConstants.KEY_MESSAGE, "failed to saved cuccessfully");
				responseObj.put(MigratorConstants.KEY_DETAILS, new JSONObject());
			}
		}catch(Exception exception) {
			responseObj.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseObj.put(MigratorConstants.KEY_MESSAGE, exception.getMessage());
			responseObj.put(MigratorConstants.KEY_DETAILS, new JSONObject());
			LOGGER.error("exception occured while executing method saveActiveMQConfigurations {}",exception);
		}
		return responseObj;
	}
	
	/**
	 * Gets the alfresco types.
	 *
	 * @param classType the class type
	 * @return the alfresco types
	 */
	public String getAlfrescoTypes(final String classType) {
		final StringBuilder typesBuilder = new StringBuilder();
		final ConfigurationDetails configurationDetails = configurationsDetailsRepo.findByAppname(MigratorConstants.ALFRESCO);
		final String alfrescoBaseURL = generateAlfrescoURL(configurationDetails);
		final String serviceURL = alfrescoBaseURL+MigratorConstants.ALFRESCO_CLASSES_URL;
		LOGGER.info("Alfresco Classes URL {}",serviceURL);
		final Map<String, Map<String, String>> definitionsMDR =alfrescoService.getAlfrescoTypes(serviceURL, configurationDetails.getUsername(), configurationDetails.getPassword(),"mdr",StringUtils.EMPTY);
		LOGGER.info("types map {}",definitionsMDR);
		final Map<String, Map<String, String>> definitionsMDRKM =alfrescoService.getAlfrescoTypes(serviceURL, configurationDetails.getUsername(), configurationDetails.getPassword(),"mdrkm",StringUtils.EMPTY);
		final Map<String, Map<String, String>> defaultContent =alfrescoService.getAlfrescoTypes(serviceURL, configurationDetails.getUsername(), configurationDetails.getPassword(),StringUtils.EMPTY,MigratorConstants.TYPE_CM_CONTENT.replace(":", "_"));
		LOGGER.info("types map {}",definitionsMDRKM);
		typesBuilder.append(buildAlfrescoDropdownItems(definitionsMDR,classType));
		typesBuilder.append(buildAlfrescoDropdownItems(definitionsMDRKM,classType));
		typesBuilder.append(buildAlfrescoDropdownItems(defaultContent,classType));
		return typesBuilder.toString();
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
	 * Builds the alfresco dropdown items.
	 *
	 * @param configurations the configurations
	 * @param key the key
	 * @return the string
	 */
	private String buildAlfrescoDropdownItems(final Map<String, Map<String, String>> configurations, final String key) {
		final StringBuilder optionBuilder = new StringBuilder();
		if((!configurations.isEmpty()) && configurations.containsKey(key)) {
			final Map<String, String> typesMap = new HashMap<>();
			typesMap.putAll((Map<? extends String, ? extends String>) configurations.get(key));
			final Iterator<String> typesIterator = typesMap.keySet().iterator();
			while(typesIterator.hasNext()) {
				final String mapKey = typesIterator.next();
				final String value = (StringUtils.isEmpty(typesMap.get(mapKey))|| StringUtils.isBlank(typesMap.get(mapKey)))?mapKey:typesMap.get(mapKey);
				optionBuilder.append(String.format(MigratorConstants.OPTION_TEMPLATE,mapKey, value));
				
			}
		}
		return optionBuilder.toString();
	}
	
	/**
	 * Save metadata mapping.
	 *
	 * @param templateDefinition the template definition
	 * @param templateDetails the template details
	 * @param columnName the column name
	 * @return the JSON object
	 */
	public JSONObject saveMetadataMapping(final TemplateDefinition templateDefinition,final List<TemplateDetails> templateDetails,final String columnName) {
		final JSONObject response = new JSONObject();
		try {
			final TemplateDefinition savedTemplate = templateDefinitionRepo.save(templateDefinition);
			if(null!=savedTemplate) {
				final Long templateIdSaved = savedTemplate.getTemplateid();
				LOGGER.info("saved template definition ID {}",templateIdSaved);
				final FileColumn fileColumn = new FileColumn();
				fileColumn.setTemplatedefinition(savedTemplate);
				LOGGER.info("file column name {}",columnName);
				fileColumn.setColumnname(columnName);
				fileColumnRepo.save(fileColumn);
				templateDetails.forEach(tDetail->{
					tDetail.setTemplatedefinition(savedTemplate);
					templateDetailsRepo.save(tDetail);
				});
				response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
				response.put(MigratorConstants.KEY_MESSAGE, "template saved successfully!");
			}else {
				response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
				response.put(MigratorConstants.KEY_MESSAGE, "failed to save template");
			}
		}catch (Exception exception) {
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.put(MigratorConstants.KEY_MESSAGE, exception.getMessage());
			LOGGER.error("exception occured while saving template {}",exception);
		}
		return response;
	}
	
	/**
	 * Gets the template details by id.
	 *
	 * @param templateId the template id
	 * @return the template details by id
	 */
	public JSONArray getTemplateDetailsById(final TemplateDefinition templateId) {
		final JSONArray response = new JSONArray();
		final List<TemplateDetails> details = templateDetailsRepo.findAllByTemplatedefinition(templateId);
		for(final TemplateDetails detail:details) {
			final JSONObject sDetail = new JSONObject();
			sDetail.put(MigratorConstants.KEY_PROPERTY_QNAME, detail.getAlfrescopropertyqname());
			sDetail.put(MigratorConstants.KEY_PROPERTY_TITLE, detail.getAlfpropertytitle());
			sDetail.put(MigratorConstants.KEY_UNIQUE_ID, detail.getUniqueid());
			sDetail.put(MigratorConstants.KEY_COLUMN_NAME, detail.getCsvcolumnname());
			response.put(sDetail);
			LOGGER.info("{} {}",detail.getUniqueid(),detail.getAlfrescopropertyqname());
		}
		return response;
	}
	
	/**
	 * Gets the template.
	 *
	 * @param templateId the template id
	 * @return the template
	 */
	public JSONObject getTemplate(final Long templateId) {
		final JSONObject response = new JSONObject();
		final Optional<TemplateDefinition> templateDefinition = templateDefinitionRepo.findById(templateId);
		if(templateDefinition.isEmpty()) {
			response.put(MigratorConstants.KEY_TEMPLATE_NAME, StringUtils.EMPTY);
			response.put(MigratorConstants.KEY_PROPERTY_QNAME, StringUtils.EMPTY);
			response.put(MigratorConstants.KEY_PROPERTY_TITLE, StringUtils.EMPTY);
			response.put(MigratorConstants.KEY_TEMPLATE_DETAILS, new JSONArray());
		}else {
			LOGGER.info("template details {}",templateDefinition.get().getTemplatedetails());
			LOGGER.info("file column name {}",templateDefinition.get().getFilecolumn());
			response.put(MigratorConstants.KEY_TEMPLATE_NAME, templateDefinition.get().getTemplatename());
			response.put(MigratorConstants.KEY_PROPERTY_QNAME, templateDefinition.get().getFiletypeqname());
			response.put(MigratorConstants.KEY_PROPERTY_TITLE, templateDefinition.get().getFiletype());
			response.put(DatabaseConstants.COLUMN_NAME, templateDefinition.get().getFilecolumn().getColumnname());
			final Set<TemplateDetails> detailsSet = new HashSet<>();
			detailsSet.addAll(templateDefinition.get().getTemplatedetails());
			final JSONArray detailsArray = new JSONArray();
			for(final TemplateDetails detail : detailsSet) {
				detailsArray.put(new JSONObject(detail.toString()));
			}
			response.put(MigratorConstants.KEY_TEMPLATE_DETAILS, detailsArray);
		}
		return response;
	}

}
