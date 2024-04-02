/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.controllers;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.constants.MigratorConstants;
import com.tcs.database.ConfigurationDetails;
import com.tcs.database.TemplateDefinition;
import com.tcs.database.TemplateDetails;
import com.tcs.repositories.ConfigurationsDetailsRepo;
import com.tcs.repositories.TemplateDefinitionRepo;
import com.tcs.services.AlfrescoService;
import com.tcs.services.CSVService;
import com.tcs.services.SolrService;
import com.tcs.utils.MigrationUtils;

/**
 * The Class ServiceController.
 */
@RestController
public class ServiceController {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceController.class);
	
	/** The alfresco service. */
	@Autowired
	AlfrescoService alfrescoService;
	
	/** The configurations details repo. */
	@Autowired
	ConfigurationsDetailsRepo configurationsDetailsRepo;
	
	/** The csv service. */
	@Autowired
	CSVService csvService;
	
	/** The template definition repo. */
	@Autowired
	TemplateDefinitionRepo templateDefinitionRepo;
	
	/** The solr service. */
	@Autowired
	SolrService solrService;
	
	/**
	 * Gets the alfresco type properties.
	 *
	 * @param type the type
	 * @return the alfresco type properties
	 */
	@GetMapping(value = {"/alfrescoclassproperties"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAlfrescoTypeProperties(@RequestParam("type") String type) {
		final JSONObject respJson = new JSONObject();
		LOGGER.info("executing service URL {}","/alfrescoclassproperties");
		if(StringUtils.isEmpty(type) || StringUtils.isBlank(type)) {
			respJson.put(MigratorConstants.KEY_PROPERTIES, new JSONArray());
		}else {
			final ConfigurationDetails configurationDetails = configurationsDetailsRepo.findByAppname(MigratorConstants.ALFRESCO);
			if(null!=configurationDetails) {
				final String alfrescoUrl = MigrationUtils.generateAlfrescoURL(configurationDetails)+MigratorConstants.ALFRESCO_CLASSES_URL+MigratorConstants.FORWARD_SLASH+type;
				respJson.put(MigratorConstants.KEY_PROPERTIES, alfrescoService.getAlfrescoTypesProperties(alfrescoUrl, configurationDetails.getUsername(), configurationDetails.getPassword()));
			}else {
				respJson.put(MigratorConstants.KEY_PROPERTIES, new JSONArray());
			}
		}
		return respJson.toString();
	}
	
	/**
	 * Gets the children.
	 *
	 * @param nodeid the nodeid
	 * @return the children
	 */
	@GetMapping(value = {"/alfrescofolders"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getChildren(@RequestParam String nodeid,final HttpServletResponse httpServletResponse) {
		JSONObject responseJson = new JSONObject();
		final ConfigurationDetails configurationDetails = configurationsDetailsRepo.findByAppname(MigratorConstants.ALFRESCO);
		if(null!=configurationDetails) {
			final String alfrescoUrl = MigrationUtils.generateAlfrescoURL(configurationDetails);
			responseJson = alfrescoService.getChildrenFolders(alfrescoUrl, configurationDetails.getUsername(), configurationDetails.getPassword(), nodeid);
			LOGGER.info("response {}",responseJson);
			httpServletResponse.setStatus(responseJson.getInt(MigratorConstants.KEY_STATUS));
		}else {
			responseJson.put(MigratorConstants.KEY_PROPERTIES, new JSONArray());
			httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
			responseJson.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_NOT_FOUND);
			responseJson.put(MigratorConstants.KEY_MESSAGE, "Alfresco Configurations not found");
		}
		return responseJson.toString();
	}
	
	/**
	 * Check migration status.
	 *
	 * @param csvid the csvid
	 * @return the string
	 */
	@GetMapping(value = {"/checkstatus"})
	public String checkMigrationStatus(@RequestParam(required = false) Long csvid) {
		LOGGER.info("long value {}",csvid);
		if(StringUtils.isEmpty(csvid+"") || StringUtils.isBlank(csvid+"") || null==csvid) {
			final JSONObject responseJson = new JSONObject();
			responseJson.put(MigratorConstants.KEY_LIST, new JSONArray());
			return responseJson.toString();
		}else {
			return csvService.countMigratedCsvRecords(csvid.longValue()).toString();
		}
	}
	
	/**
	 * Gets the pie C hart data.
	 *
	 * @param csvid the csvid
	 * @return the pie C hart data
	 */
	@GetMapping(value = {"/getpiechartdata"})
	public String getPieCHartData(@RequestParam(required = false) Long csvid) {
		LOGGER.info("long value {}",csvid);
		if(StringUtils.isEmpty(csvid+"") || StringUtils.isBlank(csvid+"") || null==csvid) {
			final JSONObject responseJson = new JSONObject();
			responseJson.put(MigratorConstants.KEY_LIST, new JSONArray());
			return responseJson.toString();
		}else {
			return csvService.getPieChartData(csvid.longValue()).toString();
		}
	}
	
	/**
	 * Gets the node from alfresco.
	 *
	 * @param nodeid the nodeid
	 * @param templateid the templateid
	 * @param httpServletResponse the http servlet response
	 * @return the node from alfresco
	 */
	@GetMapping(value = {"/getnodedetails"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getNodeFromAlfresco(@RequestParam String nodeid,@RequestParam Long templateid,final HttpServletResponse httpServletResponse) {
		JSONObject response = new JSONObject();
		final ConfigurationDetails configurationDetails = configurationsDetailsRepo.findByAppname(MigratorConstants.ALFRESCO);
		if(null!=configurationDetails) {
			if(null== nodeid||StringUtils.isEmpty(nodeid) || StringUtils.isBlank(nodeid)) {
				httpServletResponse.setStatus(HttpStatus.SC_BAD_GATEWAY);
				response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_GATEWAY);
				response.put(MigratorConstants.KEY_DETAILS,new JSONObject(MigratorConstants.KEY_MESSAGE,MigratorConstants.KEY_MESSAGE, "Node ID parameter not found"));
			}else if(null== templateid) {
				httpServletResponse.setStatus(HttpStatus.SC_BAD_GATEWAY);
				response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_GATEWAY);
				response.put(MigratorConstants.KEY_DETAILS,new JSONObject(MigratorConstants.KEY_MESSAGE,MigratorConstants.KEY_MESSAGE, "Template ID parameter not found"));
			}else {
				final String alfrescoUrl = MigrationUtils.generateAlfrescoURL(configurationDetails);
				final TemplateDefinition templateDefinition = templateDefinitionRepo.findBytemplateid(templateid);
				response= alfrescoService.getAlfrescoNodeDetails(nodeid, configurationDetails.getUsername(), configurationDetails.getPassword(), alfrescoUrl);
				final JSONArray properties = new JSONArray();
				if(null!=templateDefinition) {
					LOGGER.info("template details length {}",templateDefinition.getTemplatedetails().size());
					LOGGER.info("template details {}",templateDefinition.getTemplatedetails());
					final Set<TemplateDetails> templateDetails = new HashSet<>();
					templateDetails.addAll(templateDefinition.getTemplatedetails());
					final JSONObject propertiesJson = response.getJSONObject(MigratorConstants.KEY_DETAILS).has(MigratorConstants.KEY_PROPERTIES)?  response.getJSONObject(MigratorConstants.KEY_DETAILS).getJSONObject(MigratorConstants.KEY_PROPERTIES): new JSONObject();
					for(final TemplateDetails tDetail: templateDetails) {
						if(propertiesJson.has(tDetail.getAlfrescopropertyqname())) {
							final JSONObject finalProp = new JSONObject(tDetail.toString());
							finalProp.put(MigratorConstants.KEY_VALUE, propertiesJson.getString(tDetail.getAlfrescopropertyqname()));
							properties.put(finalProp);
							LOGGER.info("prop json {}",finalProp);
						}
					}
					LOGGER.info("alfresco properties {}",propertiesJson);
					response.getJSONObject(MigratorConstants.KEY_DETAILS).put(MigratorConstants.KEY_PROPERTIES, properties);
					final JSONObject typeJson = new JSONObject();
					typeJson.put(MigratorConstants.KEY_QNAME, templateDefinition.getFiletypeqname());
					typeJson.put(MigratorConstants.KEY_TITLE, templateDefinition.getFiletype());
					response.getJSONObject(MigratorConstants.KEY_DETAILS).put(MigratorConstants.KEY_FILE_TYPE, typeJson);
				}
				httpServletResponse.setStatus(response.getInt(MigratorConstants.KEY_STATUS));
			}
		}else {
			httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_NOT_FOUND);
			response.put(MigratorConstants.KEY_DETAILS,new JSONObject(MigratorConstants.KEY_MESSAGE,MigratorConstants.KEY_MESSAGE, "Alfresco Details not found or configured. Please contact administartor"));
		}
		return response.toString();
	}
	@GetMapping(value = {"/fetchcsvcount"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getcsvCountAlfresco(@RequestParam(required = false) String nodeid,final HttpServletResponse httpServletResponse) {
		JSONObject response = new JSONObject();
		if(StringUtils.isEmpty(nodeid) || StringUtils.isBlank(nodeid)) {
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_BAD_REQUEST);
			response.put(MigratorConstants.KEY_MESSAGE, "nodeid parameter not found");
			response.put(MigratorConstants.KEY_DETAILS, new JSONArray());
		}else {
			response = solrService.getRecordsCountSolr(nodeid);
			httpServletResponse.setStatus(response.getInt(MigratorConstants.KEY_STATUS));
			LOGGER.info("response {}",response);
		}
		return response.toString();
	}
}
