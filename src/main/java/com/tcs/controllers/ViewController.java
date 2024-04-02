/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.tcs.constants.MigratorConstants;
import com.tcs.database.CSVFiles;
import com.tcs.database.ConfigurationDetails;
import com.tcs.database.TemplateDefinition;
import com.tcs.repositories.CsvFilesRepo;
import com.tcs.repositories.TemplateDefinitionRepo;
import com.tcs.services.ConfigurationService;
import com.tcs.utils.MigrationUtils;

/**
 * The Class ViewController.
 */
@Controller
public class ViewController {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ViewController.class);
	
	/** The configuration service. */
	@Autowired
	ConfigurationService configurationService;
	
	/** The template definition repo. */
	@Autowired
	TemplateDefinitionRepo templateDefinitionRepo;
	
	/** The csv files repo. */
	@Autowired
	CsvFilesRepo csvFilesRepo;
	
	/** The activemqhost. */
	@Value("${activemq.host}")
	String activemqhost;
	
	/** The activemqport. */
	@Value("${activemq.port}")
	String activemqport;
	
	/** The activemqprotocol. */
	@Value("${activemq.protocol}")
	String activemqprotocol;
	
	/** The activemqusername. */
	@Value("${spring.activemq.user}")
	String activemqusername;
	
	/** The activemqpassword. */
	@Value("${spring.activemq.password}")
	String activemqpassword;
	
	/*
	 * @RequestMapping(value="/home", method = RequestMethod.GET) public String
	 * displayHomePage() { return "home"; }
	 */
	
	/**
	 * Welcome.
	 *
	 * @param httpServletRequest the http servlet request
	 * @return the model and view
	 */
	@RequestMapping(value = {"/home"}, method = RequestMethod.GET)
	public ModelAndView welcome(final HttpServletRequest httpServletRequest) {
		LOGGER.info("Rendering Home JSP");
		final ModelAndView home = new ModelAndView("home");
		final String protocol = httpServletRequest.getProtocol();
		final String host =  httpServletRequest.getLocalName();
		final int port = httpServletRequest.getLocalPort();
		final String contextUrl = MigrationUtils.generateContextURL(protocol.split("/")[0].toLowerCase(), host, port);
		LOGGER.info("context URL : {}",contextUrl);
		home.addObject(MigratorConstants.URL_CONTEXT, contextUrl);
	    return home;
	}
	
	/**
	 * Configurations.
	 *
	 * @param httpServletRequest the http servlet request
	 * @return the model and view
	 */
	@RequestMapping("/configurations")
	public ModelAndView configurations(final HttpServletRequest httpServletRequest) {
		final StringBuilder fileTypeOptionsBuilder = new StringBuilder();
		final StringBuilder aspectOptionsBuilder = new StringBuilder();
		LOGGER.info("rendering configurations JSP");
		final ModelAndView configurationsModel = new ModelAndView();
		final String protocol = httpServletRequest.getProtocol();
		final String host =  httpServletRequest.getLocalName();
		final int port = httpServletRequest.getLocalPort();
		final String contextUrl = MigrationUtils.generateContextURL(protocol.split("/")[0].toLowerCase(), host, port);
		LOGGER.info("context URL : {}",contextUrl);
		configurationsModel.setViewName("configurations");
		final ConfigurationDetails configurationDetails = configurationService.getAlfConfigurations();
		final String alfHost = configurationDetails==null?StringUtils.EMPTY:configurationDetails.getHost();
		final String alfProtocol = configurationDetails==null?StringUtils.EMPTY:configurationDetails.getProtocol();
		final String alfPort = configurationDetails==null?StringUtils.EMPTY:configurationDetails.getPort()+"";
		final String alfUsername = configurationDetails==null?StringUtils.EMPTY:configurationDetails.getUsername();
		final String alfPassword = configurationDetails==null?StringUtils.EMPTY:configurationDetails.getPassword();
		final String alfConfigid = configurationDetails==null?StringUtils.EMPTY:configurationDetails.getConfigid()+"";
		if(null!=configurationDetails) {
			fileTypeOptionsBuilder.append(configurationService.getAlfrescoTypes(MigratorConstants.KEY_FILETYPE_DEF));
			aspectOptionsBuilder.append(configurationService.getAlfrescoTypes(MigratorConstants.KEY_ASPECT_DEF));
		}
//		final String fileTypeOptions = configurationService.getAlfrescoTypes(MigratorConstants.KEY_FILETYPE_DEF);
//		final String aspectTypeOptions = configurationService.getAlfrescoTypes(MigratorConstants.KEY_ASPECT_DEF);
		LOGGER.info("types options {}",fileTypeOptionsBuilder);
		LOGGER.info("aspect options {}",aspectOptionsBuilder);
		configurationsModel.addObject(MigratorConstants.KEY_PROTOCOL, alfProtocol);
		configurationsModel.addObject(MigratorConstants.KEY_HOST, alfHost);
		configurationsModel.addObject(MigratorConstants.KEY_PORT, alfPort);
		configurationsModel.addObject(MigratorConstants.KEY_USERNAME, alfUsername);
		configurationsModel.addObject(MigratorConstants.KEY_PASSWORD, alfPassword);
		configurationsModel.addObject(MigratorConstants.KEY_CONFIG_ID, alfConfigid);
		configurationsModel.addObject(MigratorConstants.URL_CONTEXT, contextUrl);
		configurationsModel.addObject(MigratorConstants.KEY_FILETYPE_DEF, fileTypeOptionsBuilder.toString());
		configurationsModel.addObject(MigratorConstants.KEY_ASPECT_DEF, aspectOptionsBuilder.toString());
		configurationsModel.addObject(MigratorConstants.KEY_ACTIMEMQ_HOST, activemqhost);
		configurationsModel.addObject(MigratorConstants.KEY_ACTIVEMQ_PORT, activemqport);
		configurationsModel.addObject(MigratorConstants.KEY_ACTIVEMQ_PROTOCOL, activemqprotocol);
		configurationsModel.addObject(MigratorConstants.KEY_ACTIVEMQ_USERNAME, activemqusername);
		configurationsModel.addObject(MigratorConstants.KEY_ACTIVEMQ_PASSWORD, activemqpassword);
		configurationsModel.addObject(MigratorConstants.KEY_HAS_ALF_DETAILS,(configurationDetails==null)?Boolean.FALSE.booleanValue():Boolean.TRUE.booleanValue());
	    return configurationsModel;
	}
	
	/**
	 * Reports.
	 *
	 * @param httpServletRequest the http servlet request
	 * @return the model and view
	 */
	@RequestMapping("/reports")
	public ModelAndView reports(final HttpServletRequest httpServletRequest) {
		
		LOGGER.info("rendering reports JSP");
		final ModelAndView reports = new ModelAndView();
		reports.setViewName("reports");
		final String protocol = httpServletRequest.getProtocol();
		final String host =  httpServletRequest.getLocalName();
		final int port = httpServletRequest.getLocalPort();
		final String contextUrl = MigrationUtils.generateContextURL(protocol.split("/")[0].toLowerCase(), host, port);
		final List<CSVFiles> csvList = new ArrayList<>();
		csvList.addAll(csvFilesRepo.findAll());
		LOGGER.info("csv files list {}",csvList);
		LOGGER.info("context URL : {}",contextUrl);
		reports.addObject(MigratorConstants.KEY_CSV_OPTIONS,buildcsvFileOptions(csvList));
		LOGGER.info("options {}",buildcsvFileOptions(csvList));
		reports.addObject(MigratorConstants.URL_CONTEXT, contextUrl);
	    return reports;
	}
	
	/**
	 * Migration.
	 *
	 * @param httpServletRequest the http servlet request
	 * @return the model and view
	 */
	@RequestMapping("/migration")
	public ModelAndView migration(final HttpServletRequest httpServletRequest) {
		LOGGER.info("Rendering Migration JSP");
		final ModelAndView migration = new ModelAndView();
		final String protocol = httpServletRequest.getProtocol();
		final String host =  httpServletRequest.getLocalName();
		final int port = httpServletRequest.getLocalPort();
		final String contextUrl = MigrationUtils.generateContextURL(protocol.split("/")[0].toLowerCase(), host, port);
		LOGGER.info("context URL : {}",contextUrl);
		final List<TemplateDefinition> definitions = templateDefinitionRepo.findAll();
		if(null!=definitions) {
			migration.addObject(MigratorConstants.KEY_TEMPLATE, buildTemplateDefinitionOptions(definitions));
		}else {
			migration.addObject(MigratorConstants.KEY_TEMPLATE, StringUtils.EMPTY);
		}
		migration.setViewName("migration");
		migration.addObject(MigratorConstants.URL_CONTEXT, contextUrl);
	    return migration;
	}
	
	/**
	 * Templates.
	 *
	 * @param httpServletRequest the http servlet request
	 * @return the model and view
	 */
	@RequestMapping("/templates")
	public ModelAndView templates(final HttpServletRequest httpServletRequest) {
		LOGGER.info("rendering templates JSP");
		final ModelAndView templates = new ModelAndView();
		final String protocol = httpServletRequest.getProtocol();
		final String host =  httpServletRequest.getLocalName();
		final int port = httpServletRequest.getLocalPort();
		final String contextUrl = MigrationUtils.generateContextURL(protocol.split("/")[0].toLowerCase(), host, port);
		LOGGER.info("context URL : {}",contextUrl);
		final List<TemplateDefinition> definitions = templateDefinitionRepo.findAll();
		if(null!=definitions) {
			templates.addObject(MigratorConstants.KEY_TEMPLATE, buildTemplateDefinitionOptions(definitions));
		}else {
			templates.addObject(MigratorConstants.KEY_TEMPLATE, StringUtils.EMPTY);
		}
		templates.addObject(MigratorConstants.URL_CONTEXT, contextUrl);
	    return templates;
	}
	
	/**
	 * Builds the template definition options.
	 *
	 * @param definitions the definitions
	 * @return the string
	 */
	private String buildTemplateDefinitionOptions(final List<TemplateDefinition> definitions) {
		final StringBuilder optionBuilder = new StringBuilder();
		for(final TemplateDefinition definition: definitions) {
			optionBuilder.append(String.format(MigratorConstants.OPTION_TEMPLATE, definition.getTemplateid(),definition.getTemplatename()));
		}
		return optionBuilder.toString();
	}
	
	/**
	 * Buildcsv file options.
	 *
	 * @param csvFiles the csv files
	 * @return the string
	 */
	private String buildcsvFileOptions(final List<CSVFiles> csvFiles) {
		final StringBuilder optionBuilder = new StringBuilder();
		for(final CSVFiles csvFile: csvFiles) {
			optionBuilder.append(String.format(MigratorConstants.OPTION_TEMPLATE, csvFile.getUniqueid(),csvFile.getCsvname()));
		}
		return optionBuilder.toString();
	}
	

}
