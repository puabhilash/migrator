/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.database.FileColumn;
import com.tcs.database.TemplateDefinition;

/**
 * The Interface FileColumnRepo.
 */
public interface FileColumnRepo extends JpaRepository<FileColumn, Long> {

	/**
	 * Find bytemplatedefinition.
	 *
	 * @param templateDefinition the template definition
	 * @return the file column
	 */
	FileColumn findBytemplatedefinition(final TemplateDefinition templateDefinition);
}
