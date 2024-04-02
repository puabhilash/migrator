/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.database;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.json.JSONObject;

import com.tcs.constants.DatabaseConstants;


/**
 * The Class FileColumn.
 */
@Entity
@Table(name = "filecolumn",indexes = {@Index(columnList = "columnid", unique = false,name="columnidindex"),
		@Index(name="columnnameindex",columnList = "columnname",unique = false)})
@IdClass(FileColumnId.class)
public class FileColumn implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The columnid. */
	@Id
	@Column(name = "columnid")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long columnid;
	
	/** The templatedefinition. */
	@Id
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = DatabaseConstants.TEMPLATE_ID)
	private TemplateDefinition templatedefinition;
	
	/** The columnname. */
	@Column(name = "columnname")
	private String columnname;
	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
    public String toString() {
		final JSONObject fileColumnJson = new JSONObject();
		fileColumnJson.put(DatabaseConstants.COLUMN_ID, this.columnid);
		fileColumnJson.put(DatabaseConstants.COLUMN_NAME, this.columnname);
        return fileColumnJson.toString();
    }

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

	/**
	 * Gets the columnname.
	 *
	 * @return the columnname
	 */
	public String getColumnname() {
		return columnname;
	}

	/**
	 * Sets the columnname.
	 *
	 * @param columnname the new columnname
	 */
	public void setColumnname(final String columnname) {
		this.columnname = columnname;
	}

	/**
	 * Gets the templatedefinition.
	 *
	 * @return the templatedefinition
	 */
	public TemplateDefinition getTemplatedefinition() {
		return templatedefinition;
	}

	/**
	 * Sets the templatedefinition.
	 *
	 * @param templatedefinition the new templatedefinition
	 */
	public void setTemplatedefinition(final TemplateDefinition templatedefinition) {
		this.templatedefinition = templatedefinition;
	}


}
