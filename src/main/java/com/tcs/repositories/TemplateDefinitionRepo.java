/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.database.TemplateDefinition;

/**
 * The Interface TemplateDefinitionRepo.
 */
public interface TemplateDefinitionRepo extends JpaRepository<TemplateDefinition, Long> {
	
	/**
	 * Find bytemplateid.
	 *
	 * @param templateid the templateid
	 * @return the template definition
	 */
	TemplateDefinition findBytemplateid(final Long templateid);

}
