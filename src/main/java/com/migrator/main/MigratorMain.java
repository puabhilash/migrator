/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.migrator.constants.MigratorConstants;
import com.migrator.database.Configurations;
import com.migrator.repositories.ConfigurationRepo;
import com.migrator.repositories.ConfigurationsDetailsRepo;
import com.migrator.services.ActiveMQService;

/**
 * The Class MigratorMain.
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.tcs.controllers","com.tcs.services","com.tcs.restcontrollers","com.tcs.consumers","com.tcs.configurations"})
@EntityScan(basePackages = {"com.tcs.database"})
@Configuration
@EnableJpaRepositories(basePackages = {"com.tcs.repositories"})
@PropertySource("classpath:application.properties")
public class MigratorMain extends SpringBootServletInitializer implements CommandLineRunner {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MigratorMain.class);
	
	/** The activemqhost. */
	@Value("${activemq.host}")
	private String activemqhost;
	
	/** The activemqport. */
	@Value("${activemq.port}")
	private String activemqport;
	
	/** The activemqprotocol. */
	@Value("${activemq.protocol}")
	private String activemqprotocol;
	
	/** The activemqusername. */
	@Value("${spring.activemq.user}")
	private String activemqusername;
	
	/** The activemqpassword. */
	@Value("${spring.activemq.password}")
	private String activemqpassword;
	
	/** The configurations details repo. */
	@Autowired
	ConfigurationsDetailsRepo configurationsDetailsRepo;
	
	@Autowired
	ConfigurationRepo configurationRepo;
	
	@Autowired
	ActiveMQService activeMQService;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(MigratorMain.class, args);

	}

	@Override
	public void run(String... args) throws Exception {
		final Configurations activemqConfigurations = configurationRepo.findByconfigurationname(MigratorConstants.ACTIVEMQ);
		LOGGER.info("app name {}",MigratorConstants.ACTIVEMQ);
		if(null==activemqConfigurations) {
//			activeMQService.saveAcitiveMQDetails(activemqprotocol, activemqhost, activemqport, activemqusername, activemqpassword);
		}else {
			LOGGER.info("active mq config {}",activemqConfigurations.toString());
			LOGGER.info("active mq details {}",activemqConfigurations.getConfigurationDetails().toString());
		}
		LOGGER.info("Migrator application started....!Happy Migration..");
	}

}
