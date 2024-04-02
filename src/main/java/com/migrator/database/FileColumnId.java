/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.database;

import java.io.Serializable;

/**
 * The Class FileColumnId.
 */
public class FileColumnId implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The columnid. */
	private long columnid;
	

	/**
	 * Gets the columnid.
	 *
	 * @return the columnid
	 */
	public long getColumnid() {
		return columnid;
	}

	/**
	 * Sets the columnid.
	 *
	 * @param columnid the new columnid
	 */
	public void setColumnid(final long columnid) {
		this.columnid = columnid;
	}

}
