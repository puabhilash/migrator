/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tcs.database.Configurations;

/**
 * The Interface ConfigurationRepo.
 */
@Repository
public interface ConfigurationRepo extends JpaRepository<Configurations, Long> {
	
	Configurations findByconfigurationname(final String configurations);
		
}
