/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.database.CSVFiles;

/**
 * The Interface CsvFilesRepo.
 */
public interface CsvFilesRepo extends JpaRepository<CSVFiles, Long> {
	
	/**
	 * Find byuniqueid.
	 *
	 * @param uniqueid the uniqueid
	 * @return the CSV files
	 */
	CSVFiles findByuniqueid(final Long uniqueid);

}
