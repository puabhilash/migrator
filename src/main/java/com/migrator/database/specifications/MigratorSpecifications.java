/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.database.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import com.migrator.constants.DatabaseConstants;
import com.migrator.database.MigratedRecord;

/**
 * The Class MigratorSpecifications.
 */
public class MigratorSpecifications {
	
	/**
	 * Csv id spec.
	 *
	 * @param csvid the csvid
	 * @return the specification
	 */
	public static Specification<MigratedRecord> csvIdSpec(final long csvid) {
	    return (root, query, builder) -> {
	      return builder.equal(root.get(DatabaseConstants.CSV_UNIQUE_ID), csvid);
	    };
	}
	
	/**
	 * Created spec.
	 *
	 * @return the specification
	 */
	public static Specification<MigratedRecord> createdSpec() {
	    return (root, query, builder) -> {
	      return builder.equal(root.get(DatabaseConstants.STATUS),String.valueOf(HttpStatus.CREATED.value()));
	    };
	}
	
	/**
	 * Not created spec.
	 *
	 * @return the specification
	 */
	public static Specification<MigratedRecord> notCreatedSpec() {
	    return (root, query, builder) -> {
	      return builder.notEqual(root.get(DatabaseConstants.STATUS),String.valueOf(HttpStatus.CREATED.value()));
	    };
	}
	
	/**
	 * Not ok spec.
	 *
	 * @return the specification
	 */
	public static Specification<MigratedRecord> notOkSpec() {
	    return (root, query, builder) -> {
	      return builder.notEqual(root.get(DatabaseConstants.STATUS),String.valueOf(HttpStatus.OK.value()));
	    };
	}
	
	/**
	 * Greater than created spec.
	 *
	 * @return the specification
	 */
	public static Specification<MigratedRecord> greaterThanCreatedSpec() {
	    return (root, query, builder) -> {
	      return builder.greaterThan(root.get(DatabaseConstants.STATUS),String.valueOf(HttpStatus.CREATED.value()));
	    };
	}
	
	/**
	 * Ok spec.
	 *
	 * @return the specification
	 */
	public static Specification<MigratedRecord> okSpec() {
	    return (root, query, builder) -> {
	      return builder.equal(root.get(DatabaseConstants.STATUS), String.valueOf(HttpStatus.OK.value()));
	    };
	}
	
	/**
	 * Ok or created spec.
	 *
	 * @return the specification
	 */
	public static Specification<MigratedRecord> okOrCreatedSpec() {
	    return (root, query, builder) -> {
	      return builder.or(builder.equal(root.get(DatabaseConstants.STATUS), String.valueOf(HttpStatus.OK.value())),
	    		  builder.equal(root.get(DatabaseConstants.STATUS), String.valueOf(HttpStatus.CREATED.value())));
	    };
	}
	
	/**
	 * Not ok or created spec.
	 *
	 * @return the specification
	 */
	public static Specification<MigratedRecord> notOkOrCreatedSpec() {
	    return (root, query, builder) -> {
	      return builder.or(builder.notEqual(root.get(DatabaseConstants.STATUS), String.valueOf(HttpStatus.OK.value())),
	    		  builder.notEqual(root.get(DatabaseConstants.STATUS), String.valueOf(HttpStatus.CREATED.value())));
	    };
	}

}
