/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.migrator.database.FileColumn;
import com.migrator.database.TemplateDefinition;

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
