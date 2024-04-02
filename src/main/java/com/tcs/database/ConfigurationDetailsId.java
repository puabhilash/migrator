/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.database;

import java.io.Serializable;

/**
 * The Class ConfigurationDetailsId.
 */
public class ConfigurationDetailsId implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

//	private long confid;
	
	/** The configid. */
	private long configid;

	/**
	 * Gets the configid.
	 *
	 * @return the configid
	 */
	public long getConfigid() {
		return configid;
	}

	/**
	 * Sets the configid.
	 *
	 * @param configid the new configid
	 */
	public void setConfigid(final long configid) {
		this.configid = configid;
	}
}
