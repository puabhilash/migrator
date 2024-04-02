/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.database;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.json.JSONObject;

import com.tcs.constants.DatabaseConstants;

/**
 * The Class CSVFiles.
 */
@Entity
@Table(name = "csvfiles",indexes = {@Index(columnList = "templateid", unique = false,name="templateidcsvindex"),
		@Index(name="uniqueidindex",columnList = "uniqueid",unique = false),
		@Index(name="csvtemplateidindex",columnList = "templateid",unique = false),
		@Index(name="nativetemplateidindex",columnList = "nativetemplateid",unique = false)})
public class CSVFiles implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The uniqueid. */
	@Id
	@Column(name = "uniqueid")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long uniqueid;
	
	/** The csvname. */
	@Column(name = "csvname",length = 500)
	private String csvname;
	
	/** The totalrecords. */
	@Column(name = "totalrecords")
	private long totalrecords;
	
	/** The totalmigrated. */
	@Column(name = "totalmigrated")
	private long totalmigrated;
	
	/** The totalfailed. */
	@Column(name = "totalfailed")
	private long totalfailed;
	
	/** The pdf destination. */
	@Column(name = "pdfdestination")
	private String pdfDestination;
	
	/** The nativedestination. */
	@Column(name = "nativedestination")
	private String nativedestination;
	
	/** The pdflocation. */
	@Column(name = "pdflocation", length = 500)
	private String pdflocation;
	
	/** The nativelocation. */
	@Column(name = "nativelocation", length = 500)
	private String nativelocation;
	
	/** The templateid. */
	@Column(name = "templateid")
	private long templateid;
	
	/** The nativetemplateid. */
	@Column(name = "nativetemplateid")
	private long nativetemplateid;
	
	/** The sourcelocation. */
	@Column(name = "sourcelocation")
	private String sourcelocation;
	
//	@OneToMany(fetch = FetchType.LAZY,mappedBy = "csvFiles", cascade = CascadeType.ALL)
//	private Set<MigratedRecord> migratedRecords;

	/**
 * To string.
 *
 * @return the string
 */
@Override
    public String toString() {
		final JSONObject csvFileJson = new JSONObject();
		csvFileJson.put(DatabaseConstants.UNIQUE_ID, this.uniqueid);
		csvFileJson.put(DatabaseConstants.CSV_FILE_NAME, this.csvname);
		csvFileJson.put(DatabaseConstants.TOTAL_RECORDS, this.totalrecords);
		csvFileJson.put(DatabaseConstants.TOTAL_MIGRATED, this.totalmigrated);
		csvFileJson.put(DatabaseConstants.TOTAL_FAILED, this.totalfailed);
		csvFileJson.put(DatabaseConstants.PDF_DESTINATION, this.pdfDestination);
		csvFileJson.put(DatabaseConstants.NATIVE_DESTINATION, this.nativedestination);
		csvFileJson.put(DatabaseConstants.PDF_LOCATION, this.pdflocation);
		csvFileJson.put(DatabaseConstants.NATIVE_FILE_LOCATION, this.nativelocation);
		csvFileJson.put(DatabaseConstants.TEMPLATE_ID, this.templateid);
		csvFileJson.put(DatabaseConstants.NATIVE_TEMPLATE_ID, this.nativetemplateid);
		csvFileJson.put(DatabaseConstants.SOURCE_LOCATION, this.sourcelocation);
        return csvFileJson.toString();
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
	 * Gets the csvname.
	 *
	 * @return the csvname
	 */
	public String getCsvname() {
		return csvname;
	}

	/**
	 * Sets the csvname.
	 *
	 * @param csvname the new csvname
	 */
	public void setCsvname(final String csvname) {
		this.csvname = csvname;
	}

	/**
	 * Gets the totalrecords.
	 *
	 * @return the totalrecords
	 */
	public long getTotalrecords() {
		return totalrecords;
	}

	/**
	 * Sets the totalrecords.
	 *
	 * @param totalrecords the new totalrecords
	 */
	public void setTotalrecords(final long totalrecords) {
		this.totalrecords = totalrecords;
	}

	/**
	 * Gets the totalmigrated.
	 *
	 * @return the totalmigrated
	 */
	public long getTotalmigrated() {
		return totalmigrated;
	}

	/**
	 * Sets the totalmigrated.
	 *
	 * @param totalmigrated the new totalmigrated
	 */
	public void setTotalmigrated(final long totalmigrated) {
		this.totalmigrated = totalmigrated;
	}

	/**
	 * Gets the totalfailed.
	 *
	 * @return the totalfailed
	 */
	public long getTotalfailed() {
		return totalfailed;
	}

	/**
	 * Sets the totalfailed.
	 *
	 * @param totalfailed the new totalfailed
	 */
	public void setTotalfailed(final long totalfailed) {
		this.totalfailed = totalfailed;
	}

//	public Set<MigratedRecord> getMigratedRecords() {
//		return migratedRecords;
//	}
//
//	public void setMigratedRecords(final Set<MigratedRecord> migratedRecords) {
//		this.migratedRecords = migratedRecords;
//	}

	/**
 * Gets the pdf destination.
 *
 * @return the pdf destination
 */
public String getPdfDestination() {
		return pdfDestination;
	}

	/**
	 * Sets the pdf destination.
	 *
	 * @param pdfDestination the new pdf destination
	 */
	public void setPdfDestination(final String pdfDestination) {
		this.pdfDestination = pdfDestination;
	}

	/**
	 * Gets the nativedestination.
	 *
	 * @return the nativedestination
	 */
	public String getNativedestination() {
		return nativedestination;
	}

	/**
	 * Sets the nativedestination.
	 *
	 * @param nativedestination the new nativedestination
	 */
	public void setNativedestination(final String nativedestination) {
		this.nativedestination = nativedestination;
	}

	/**
	 * Gets the pdflocation.
	 *
	 * @return the pdflocation
	 */
	public String getPdflocation() {
		return pdflocation;
	}

	/**
	 * Sets the pdflocation.
	 *
	 * @param pdflocation the new pdflocation
	 */
	public void setPdflocation(final String pdflocation) {
		this.pdflocation = pdflocation;
	}

	/**
	 * Gets the nativelocation.
	 *
	 * @return the nativelocation
	 */
	public String getNativelocation() {
		return nativelocation;
	}

	/**
	 * Sets the nativelocation.
	 *
	 * @param nativelocation the new nativelocation
	 */
	public void setNativelocation(final String nativelocation) {
		this.nativelocation = nativelocation;
	}

	/**
	 * Gets the serialversionuid.
	 *
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * Gets the templateid.
	 *
	 * @return the templateid
	 */
	public long getTemplateid() {
		return templateid;
	}

	/**
	 * Sets the templateid.
	 *
	 * @param templateid the new templateid
	 */
	public void setTemplateid(final long templateid) {
		this.templateid = templateid;
	}

	/**
	 * Gets the nativetemplateid.
	 *
	 * @return the nativetemplateid
	 */
	public long getNativetemplateid() {
		return nativetemplateid;
	}

	/**
	 * Sets the nativetemplateid.
	 *
	 * @param nativetemplateid the new nativetemplateid
	 */
	public void setNativetemplateid(final long nativetemplateid) {
		this.nativetemplateid = nativetemplateid;
	}

	/**
	 * Gets the sourcelocation.
	 *
	 * @return the sourcelocation
	 */
	public String getSourcelocation() {
		return sourcelocation;
	}

	/**
	 * Sets the sourcelocation.
	 *
	 * @param sourcelocation the new sourcelocation
	 */
	public void setSourcelocation(final String sourcelocation) {
		this.sourcelocation = sourcelocation;
	}

}
