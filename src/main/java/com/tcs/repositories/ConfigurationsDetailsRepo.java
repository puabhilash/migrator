/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.database.ConfigurationDetails;

/**
 * The Interface ConfigurationsDetailsRepo.
 */
public interface ConfigurationsDetailsRepo extends JpaRepository<ConfigurationDetails, Long> {
	
	/**
	 * Find by appname.
	 *
	 * @param appname the appname
	 * @return the configuration details
	 */
	ConfigurationDetails findByAppname(String appname);

}
