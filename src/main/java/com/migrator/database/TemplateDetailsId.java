/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.database;

import java.io.Serializable;

/**
 * The Class TemplateDetailsId.
 */
public class TemplateDetailsId implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The uniqueid. */
	private long uniqueid;

	/**
	 * Gets the uniqueid.
	 *
	 * @return the uniqueid
	 */
	public long getUniqueid() {
		return uniqueid;
	}

	/**
	 * Sets the uniqueid.
	 *
	 * @param uniqueid the new uniqueid
	 */
	public void setUniqueid(final long uniqueid) {
		this.uniqueid = uniqueid;
	}
}
