/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.migrator.database.TemplateDefinition;
import com.migrator.database.TemplateDetails;

/**
 * The Interface TemplateDetailsRepo.
 */
public interface TemplateDetailsRepo extends JpaRepository<TemplateDetails, Long> {

	/**
	 * Find all by templatedefinition.
	 *
	 * @param templateId the template id
	 * @return the list
	 */
	List<TemplateDetails> findAllByTemplatedefinition(final TemplateDefinition templateId);
	
//	List<TemplateDetails> findAllByismultivalued(final TemplateDefinition templateId,final boolean ismultivalued);
	
	/**
	 * Find byuniqueid.
	 *
	 * @param uniqueid the uniqueid
	 * @return the template details
	 */
	TemplateDetails findByuniqueid(final Long uniqueid);
}
