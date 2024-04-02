/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.restcontrollers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.migrator.constants.MigratorConstants;
import com.migrator.database.ConfigurationDetails;
import com.migrator.database.Configurations;
import com.migrator.database.TemplateDefinition;
import com.migrator.database.TemplateDetails;
import com.migrator.repositories.ConfigurationRepo;
import com.migrator.services.ActiveMQService;
import com.migrator.services.ConfigurationService;


/**
 * The Class ConfigurationController.
 */
@RestController
public class ConfigurationController {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationController.class); 
	
	
	@Value("${activemq.protocol}")
	private String ACTIVEMQ_PROTOCOL;
	
	@Value("${activemq.host}")
	private String ACTIVEMQ_HOST;
	
	@Value("${activemq.port}")
	private String ACTIVEMQ_PORT;
	
	@Value("${spring.activemq.user}")
	private String ACTIVEMQ_USERNAME;
	
	@Value("${spring.activemq.password}")
	private String ACTIVEMQ_PASSWORD;
	
	/** The configuration service. */
	@Autowired
	ConfigurationService configurationService;
	
	@Autowired
	ActiveMQService activeMQService;
	
	@Autowired
	ConfigurationRepo configurationRepo;
	
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
 * @param httpServletResponse the http servlet response
 * @return the string
 */
//	@CrossOrigin(origins = {"http://50z1p73.mcdcorp.net:8181","http://localhost:8181"})
	@PostMapping(value = {"/savealfconf"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String saveAlfConfigurations(@RequestParam String protocol, @RequestParam String host,@RequestParam int port,@RequestParam String appname,
			@RequestParam String username, @RequestParam String password,@RequestParam boolean hasalfconfig,@RequestParam(required = false) Long configid,final HttpServletResponse httpServletResponse) {
		LOGGER.info("protocol to save");
		final JSONObject resJson = configurationService.saveAlfConfigurations(protocol, host, port, appname,username,password,hasalfconfig,configid);
		if(resJson.getInt(MigratorConstants.KEY_STATUS)==200) {
			httpServletResponse.setStatus(HttpStatus.SC_OK);
		}else {
			httpServletResponse.setStatus(resJson.getInt(MigratorConstants.KEY_STATUS));
		}
		return resJson.toString();
	}
	
	/**
	 * Gets the alf C onfigurations.
	 *
	 * @return the alf C onfigurations
	 */
	@GetMapping(value = {"/retrievealfconf"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAlfCOnfigurations() {
		final ConfigurationDetails configurationDetails = configurationService.getAlfConfigurations();
		final JSONObject responseJson = new JSONObject();
		if(null != configurationDetails) {
			responseJson.put(MigratorConstants.KEY_HOST, configurationDetails.getHost());
			responseJson.put(MigratorConstants.KEY_PORT, configurationDetails.getPort());
			responseJson.put(MigratorConstants.KEY_PROTOCOL, configurationDetails.getProtocol());
			responseJson.put(MigratorConstants.KEY_USERNAME, configurationDetails.getUsername());
			responseJson.put(MigratorConstants.KEY_PASSWORD, configurationDetails.getPassword());
			responseJson.put(MigratorConstants.KEY_HAS_ALF_DETAILS, Boolean.TRUE.booleanValue());
			responseJson.put(MigratorConstants.KEY_CONFIG_ID, configurationDetails.getConfigid());
		}else {
			responseJson.put(MigratorConstants.KEY_HOST, StringUtils.EMPTY);
			responseJson.put(MigratorConstants.KEY_PORT, StringUtils.EMPTY);
			responseJson.put(MigratorConstants.KEY_PROTOCOL, StringUtils.EMPTY);
			responseJson.put(MigratorConstants.KEY_USERNAME, StringUtils.EMPTY);
			responseJson.put(MigratorConstants.KEY_PASSWORD, StringUtils.EMPTY);
			responseJson.put(MigratorConstants.KEY_HAS_ALF_DETAILS, Boolean.FALSE.booleanValue());
			responseJson.put(MigratorConstants.KEY_CONFIG_ID, StringUtils.EMPTY);
		}
		return responseJson.toString();
	}
	
	/**
	 * Validate alfresco connection.
	 *
	 * @param appname the appname
	 * @param httpServletResponse the http servlet response
	 * @return the string
	 */
	@GetMapping(value = {"/validatealf"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String validateAlfrescoConnection(@RequestParam String appname,final HttpServletResponse httpServletResponse) {
		JSONObject responseJsonObject = new JSONObject();
		if(StringUtils.isEmpty(appname) || StringUtils.isBlank(appname)) {
			responseJsonObject.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
			responseJsonObject.put(MigratorConstants.KEY_USERNAME, "appname parameter is missing");
		}else {
			responseJsonObject = new JSONObject(configurationService.validateAlfrescoConnection());
			final int status = responseJsonObject.getInt(MigratorConstants.KEY_STATUS);
			httpServletResponse.setStatus(status);
		}
		LOGGER.info("response {}",responseJsonObject);
		return responseJsonObject.toString();
	}
	
	/**
	 * Gets the active MQ configurations.
	 *
	 * @return the active MQ configurations
	 */
	@GetMapping(value = {"/retrieveamqconf"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getActiveMQConfigurations() {
		final ConfigurationDetails configurationDetails = configurationService.getActiveMQConfigurations();
		final JSONObject responseJson = new JSONObject();
		if(null != configurationDetails) {
			responseJson.put(MigratorConstants.KEY_HOST, configurationDetails.getHost());
			responseJson.put(MigratorConstants.KEY_PORT, configurationDetails.getPort());
			responseJson.put(MigratorConstants.KEY_PROTOCOL, configurationDetails.getProtocol());
			responseJson.put(MigratorConstants.KEY_USERNAME, configurationDetails.getUsername());
			responseJson.put(MigratorConstants.KEY_PASSWORD, configurationDetails.getPassword());
			responseJson.put(MigratorConstants.KEY_HAS_ALF_DETAILS, Boolean.TRUE.booleanValue());
			responseJson.put(MigratorConstants.KEY_CONFIG_ID, configurationDetails.getConfigid());
		}else {
			responseJson.put(MigratorConstants.KEY_HOST, StringUtils.EMPTY);
			responseJson.put(MigratorConstants.KEY_PORT, StringUtils.EMPTY);
			responseJson.put(MigratorConstants.KEY_PROTOCOL, StringUtils.EMPTY);
			responseJson.put(MigratorConstants.KEY_USERNAME, StringUtils.EMPTY);
			responseJson.put(MigratorConstants.KEY_PASSWORD, StringUtils.EMPTY);
			responseJson.put(MigratorConstants.KEY_HAS_ALF_DETAILS, Boolean.FALSE.booleanValue());
			responseJson.put(MigratorConstants.KEY_CONFIG_ID, StringUtils.EMPTY);
		}
		return responseJson.toString();
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
	 * @return the string
	 */
	@PostMapping(value = {"/saveactivemqconf"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String saveActiveMQConfigurations(@RequestParam String protocol, @RequestParam String host,@RequestParam int port,@RequestParam String appname,
			@RequestParam String username, @RequestParam String password,@RequestParam boolean hasalfconfig,@RequestParam(required = false) Long configid) {
		LOGGER.info("protocol to save");
		final JSONObject respJson = configurationService.saveActiveMQConfigurations(protocol, host, port, appname,username,password,hasalfconfig,configid);
		return respJson.toString();
	}
	
	/**
	 * Save template definition.
	 *
	 * @param templateDefinition the template definition
	 * @param columnname the columnname
	 * @param httpServletResponse the http servlet response
	 * @return the string
	 */
	@PostMapping(value = {"/savetemplate"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String saveTemplateDefinition(@RequestParam String templateDefinition,@RequestParam String columnname,final HttpServletResponse httpServletResponse) {
		JSONObject responseJson = new JSONObject();
		try {
			if(StringUtils.isEmpty(templateDefinition)|| StringUtils.isBlank(templateDefinition)) {
				httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
				responseJson.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
				responseJson.put(MigratorConstants.KEY_MESSAGE, "template definition parameter missing");
			}else if(StringUtils.isEmpty(columnname)|| StringUtils.isBlank(columnname)) {
				httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
				responseJson.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
				responseJson.put(MigratorConstants.KEY_MESSAGE, "column name parameter missing");
			}else {
				LOGGER.info("req object {}",templateDefinition);
				final TemplateDefinition definition = getTemplateDefinition(templateDefinition);
				final List<TemplateDetails> templateDetails = getTemplateDetailsList(templateDefinition);
				responseJson=configurationService.saveMetadataMapping(definition, templateDetails,columnname);
				httpServletResponse.setStatus(HttpStatus.SC_OK);
			}
		}catch (Exception exception) {
			LOGGER.error("exception occured while executing method saveTemplateDefinition {}",exception);
			httpServletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseJson.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseJson.put(MigratorConstants.KEY_MESSAGE, exception.getMessage());
		}
		return responseJson.toString();
	}
	
	/**
	 * Gets the template definition.
	 *
	 * @param templateDefinition the template definition
	 * @return the template definition
	 */
	private TemplateDefinition getTemplateDefinition(final String templateDefinition) {
		final TemplateDefinition definition = new TemplateDefinition();
		final JSONObject templateJson = new JSONObject(templateDefinition);
		definition.setTemplatename(templateJson.getJSONObject(MigratorConstants.KEY_TEMPLATE).getString(MigratorConstants.KEY_TEMPLATE_NAME));
		definition.setFiletypeqname(templateJson.getJSONObject(MigratorConstants.KEY_TEMPLATE).getString(MigratorConstants.KEY_PROPERTY_QNAME));
		definition.setFiletype(templateJson.getJSONObject(MigratorConstants.KEY_TEMPLATE).getString(MigratorConstants.KEY_PROPERTY_TITLE));
		return definition;
	}
	
	/**
	 * Gets the template details list.
	 *
	 * @param templateDefinition the template definition
	 * @return the template details list
	 */
	private List<TemplateDetails> getTemplateDetailsList(final String templateDefinition) {
		final List<TemplateDetails> templateDetails = new ArrayList<>();
		final JSONObject templateJson = new JSONObject(templateDefinition);
		final JSONArray templates = templateJson.getJSONArray(MigratorConstants.KEY_TEMPLATE_DETAILS);
		for(int index=0;index<templates.length();index++) {
			final TemplateDetails details = new TemplateDetails();
			details.setAlfpropertytitle(templates.getJSONObject(index).getString(MigratorConstants.KEY_PROPERTY_TITLE));
			details.setCsvcolumnname(templates.getJSONObject(index).getString(MigratorConstants.KEY_COLUMN_NAME));
			details.setAlfrescopropertyqname(templates.getJSONObject(index).getString(MigratorConstants.KEY_PROPERTY_QNAME));
			templateDetails.add(details);
		}
		return templateDetails;
	}
	
	/**
	 * Gets the template details.
	 *
	 * @param templateId the template id
	 * @return the template details
	 */
	@GetMapping(value = {"/gettemplatedetails"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getTemplateDetails(@RequestParam Long templateId) {
		final JSONObject response = new JSONObject();
		final TemplateDefinition templateDefinition = new TemplateDefinition();
		templateDefinition.setTemplateid(templateId);
		final JSONArray resArray = configurationService.getTemplateDetailsById(templateDefinition);
		if(null==resArray) {
			response.put(MigratorConstants.KEY_TEMPLATE_DETAILS, new JSONArray());
		}else {
			response.put(MigratorConstants.KEY_TEMPLATE_DETAILS, resArray);
		}
		return response.toString();
	}
	
	/**
	 * Gets the template.
	 *
	 * @param templateId the template id
	 * @param httpServletResponse the http servlet response
	 * @return the template
	 */
	@GetMapping(value = {"/gettemplate"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getTemplate(@RequestParam Long templateId,final HttpServletResponse httpServletResponse) {
		JSONObject response = new JSONObject();
		if(StringUtils.isEmpty(Long.toString(templateId)) || StringUtils.isBlank(Long.toString(templateId))) {
			response.put(MigratorConstants.KEY_TEMPLATE_NAME, StringUtils.EMPTY);
			response.put(MigratorConstants.KEY_PROPERTY_QNAME, StringUtils.EMPTY);
			response.put(MigratorConstants.KEY_PROPERTY_TITLE, StringUtils.EMPTY);
			httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_GATEWAY);
			response.put(MigratorConstants.KEY_MESSAGE, "template id parameter not found");
		}else {
			response = configurationService.getTemplate(templateId);
			httpServletResponse.setStatus(HttpStatus.SC_OK);
		}
		return response.toString();
	}
	
	@GetMapping(value = {"/validateactivemq"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String validateActiveMQ(final HttpServletResponse httpServletResponse) {
		JSONObject response = new JSONObject();
//		final Configurations configurations = configurationRepo.findByconfigurationname(MigratorConstants.ACTIVEMQ);
//		if(null==configurations) {
//			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_NOT_FOUND);
//		}else {
//			final ConfigurationDetails configurationDetails = configurations.getConfigurationDetails();
//		}
		response=activeMQService.checkConnection(ACTIVEMQ_PROTOCOL, ACTIVEMQ_HOST, Integer.parseInt(ACTIVEMQ_PORT), ACTIVEMQ_USERNAME, ACTIVEMQ_PASSWORD);
		httpServletResponse.setStatus(response.getInt(MigratorConstants.KEY_STATUS));
		return response.toString();
	}
}
