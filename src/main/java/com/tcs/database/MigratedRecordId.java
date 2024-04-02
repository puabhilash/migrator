/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.database;

import java.io.Serializable;

/**
 * The Class MigratedRecordId.
 */
public class MigratedRecordId implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The csvfileid. */
	private Long csvfileid;

	/**
	 * Gets the csvfileid.
	 *
	 * @return the csvfileid
	 */
	public Long getCsvfileid() {
		return csvfileid;
	}

	/**
	 * Sets the csvfileid.
	 *
	 * @param csvfileid the new csvfileid
	 */
	public void setCsvfileid(final Long csvfileid) {
		this.csvfileid = csvfileid;
	}

	


}
