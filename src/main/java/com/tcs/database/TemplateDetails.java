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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.json.JSONObject;

import com.tcs.constants.DatabaseConstants;

/**
 * The Class TemplateDetails.
 */
@Entity
@Table(name = "templatedetails",indexes = {@Index(columnList = "templateid", unique = false,name="templateidtdindex"),
		@Index(name="uniqueidtdindex",columnList = "uniqueid",unique = false),
		@Index(name="csvcolumnnameindex",columnList = "csvcolumnname",unique = false),
		@Index(name="alfpropertytitleindex",columnList = "alfpropertytitle",unique = false),
		@Index(name="alfrescopropertyqnameindex",columnList = "alfrescopropertyqname",unique = false)})
@IdClass(TemplateDetailsId.class)
public class TemplateDetails implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The templatedefinition. */
	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "templateid")
	private TemplateDefinition templatedefinition;
	
	/** The uniqueid. */
	@Id
	@Column(name = "uniqueid")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long uniqueid;
	
	/** The csvcolumnname. */
	@Column(name = "csvcolumnname")
	private String csvcolumnname;
	
	/** The alfpropertytitle. */
	@Column(name = "alfpropertytitle")
	private String alfpropertytitle;
	
	/** The alfrescopropertyqname. */
	@Column(name = "alfrescopropertyqname")
	private String alfrescopropertyqname;
	
	/** The ismultivalued. */
	@Column(name = "ismultivalued")
	private boolean ismultivalued;
	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
    public String toString() {
		final JSONObject csvFileJson = new JSONObject();
		csvFileJson.put(DatabaseConstants.CSV_COLUMN_NAME, this.csvcolumnname);
		csvFileJson.put(DatabaseConstants.ALFRESCO_PROPERTY_QNAMR, this.alfrescopropertyqname);
		csvFileJson.put(DatabaseConstants.ALFRESCO_PROPERTY_TITLE, this.alfpropertytitle);
		csvFileJson.put(DatabaseConstants.UNIQUE_ID, this.uniqueid);
        return csvFileJson.toString();
    }

	/**
	 * Gets the alfpropertytitle.
	 *
	 * @return the alfpropertytitle
	 */
	public String getAlfpropertytitle() {
		return alfpropertytitle;
	}

	/**
	 * Sets the alfpropertytitle.
	 *
	 * @param alfpropertytitle the new alfpropertytitle
	 */
	public void setAlfpropertytitle(final String alfpropertytitle) {
		this.alfpropertytitle = alfpropertytitle;
	}

	/**
	 * Gets the alfrescopropertyqname.
	 *
	 * @return the alfrescopropertyqname
	 */
	public String getAlfrescopropertyqname() {
		return alfrescopropertyqname;
	}

	/**
	 * Sets the alfrescopropertyqname.
	 *
	 * @param alfrescopropertyqname the new alfrescopropertyqname
	 */
	public void setAlfrescopropertyqname(final String alfrescopropertyqname) {
		this.alfrescopropertyqname = alfrescopropertyqname;
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

	/**
	 * Gets the csvcolumnname.
	 *
	 * @return the csvcolumnname
	 */
	public String getCsvcolumnname() {
		return csvcolumnname;
	}

	/**
	 * Sets the csvcolumnname.
	 *
	 * @param csvcolumnname the new csvcolumnname
	 */
	public void setCsvcolumnname(final String csvcolumnname) {
		this.csvcolumnname = csvcolumnname;
	}

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

	/**
	 * Checks if is ismultivalued.
	 *
	 * @return true, if is ismultivalued
	 */
	public boolean isIsmultivalued() {
		return ismultivalued;
	}

	/**
	 * Sets the ismultivalued.
	 *
	 * @param ismultivalued the new ismultivalued
	 */
	public void setIsmultivalued(final boolean ismultivalued) {
		this.ismultivalued = ismultivalued;
	}
}
