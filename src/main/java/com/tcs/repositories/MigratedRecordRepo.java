/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.tcs.database.MigratedRecord;

/**
 * The Interface MigratedRecordRepo.
 */
@Transactional
public interface MigratedRecordRepo extends JpaRepository<MigratedRecord, Long>,JpaSpecificationExecutor<MigratedRecord> {

//	List<MigratedRecord> findBycsvuniqueid(final Long csvuniqueid,Sort sort);
	
	/**
	 * Find bycsvuniqueid.
	 *
	 * @param csvuniqueid the csvuniqueid
	 * @param pageRequest the page request
	 * @return the page
	 */
	Page<MigratedRecord> findBycsvuniqueid(final Long csvuniqueid,Pageable pageRequest);
		
	/**
	 * Countcsvunique id.
	 *
	 * @param csvuniqueid the csvuniqueid
	 * @return the list
	 */
	@Query("SELECT status,count(1) FROM MigratedRecord where csvuniqueid=:csvuniqueid group by status order by status asc")
	List<String> countcsvuniqueId(final Long csvuniqueid);
	
	/**
	 * Find bycsvuniqueid and status not.
	 *
	 * @param csvuniqueid the csvuniqueid
	 * @param status the status
	 * @param pageRequest the page request
	 * @return the page
	 */
	Page<MigratedRecord> findBycsvuniqueidAndStatusNot(final Long csvuniqueid,final String status,Pageable pageRequest);
	
	/**
	 * Find bycsvuniqueid and status.
	 *
	 * @param csvuniqueid the csvuniqueid
	 * @param status the status
	 * @param pageRequest the page request
	 * @return the page
	 */
	Page<MigratedRecord> findBycsvuniqueidAndStatus(final Long csvuniqueid,final String status,Pageable pageRequest);
	
	/**
	 * Find bycsvfileid.
	 *
	 * @param csvfileid the csvfileid
	 * @return the migrated record
	 */
	MigratedRecord findBycsvfileid(final Long csvfileid);
	
	/**
	 * Delete by noderef.
	 *
	 * @param nodeRef the node ref
	 * @return the migrated record
	 */
	MigratedRecord deleteByNoderef(final String nodeRef);
	
	/**
	 * Delete bycsvfileid.
	 *
	 * @param csvfileid the csvfileid
	 */
	void deleteBycsvfileid(final Long csvfileid);

	
}
