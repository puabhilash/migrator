/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.database;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.tcs.constants.DatabaseConstants;
import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;


/**
 * The Class MigratedRecord.
 */
@Entity
@Table(name = "migratedrecord",indexes = {@Index(columnList = "csvfileid", unique = false,name="csvfileidindex"),
		@Index(name="csvuniqueidindex",columnList = "csvuniqueid",unique = false)})
@IdClass(MigratedRecordId.class)
public class MigratedRecord implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

//	@Id
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "uniqueid")
//	private CSVFiles csvFiles;
	
	/** The csvfileid. */
	@Id
	@Column(name = "csvfileid")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long csvfileid;
	
	/** The properties. */
	@Column(name="properties", columnDefinition = "json")
	@Convert(converter = PropertiesConverter.class)
	private Map<String, Object> properties;
	
	/** The noderef. */
	@Column(name = "noderef")
	private String noderef;
	
	/** The status. */
	@Column(name = "status")
	private String status;
	
	/** The message. */
	@Column(name = "message", length = 500)
	private String message;
	
	/** The isnative. */
	@Column(name = "isnative")
	private boolean isnative;
	
	/** The islatest. */
	@Column(name = "islatest")
	private boolean islatest;
	
	/** The localfilelocation. */
	@Column(name = "localfilelocation", length = 200)
	private String localfilelocation;
	
	/** The csvrecordnumber. */
	@Column(name = "csvrecordnumber")
	private long csvrecordnumber;
	
	/** The propertiesstatus. */
	@Column(name = "propertiesstatus",length = 20)
	private String propertiesstatus;
	
	/** The propertiesmessage. */
	@Column(name = "propertiesmessage", length = 1000)
	private String propertiesmessage;
	
	/** The csvuniqueid. */
	@Column(name = "csvuniqueid")
	private long csvuniqueid;
	
	/** The startdate. */
	@Column(name = "startdate")
	private LocalDateTime startdate;
	
	/** The enddate. */
	@Column(name = "enddate")
	private LocalDateTime enddate;
	
	/** The filename. */
	@Column(name = "filename", length = 500)
	private String filename;
	
	
	/** The filesize. */
	@Column(name = "filesize", columnDefinition = "bigint default 0")
	private long filesize;
	
//	@Id
//	@OneToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "uniqueid")
//	private TemplateDetails templateDetails;

//	public CSVFiles getCsvFiles() {
//		return csvFiles;
//	}
//
//	public void setCsvFiles(final CSVFiles csvFiles) {
//		this.csvFiles = csvFiles;
//	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
    public String toString() {
		final JSONObject migratedRecordJson = new JSONObject();
		migratedRecordJson.put(DatabaseConstants.CSV_FILE_ID, this.csvfileid);
		migratedRecordJson.put(DatabaseConstants.PROPERTIES, JacksonUtil.toString(this.properties));
		migratedRecordJson.put(DatabaseConstants.NODE_REF, this.noderef);
		migratedRecordJson.put(DatabaseConstants.STATUS, this.status);
		migratedRecordJson.put(DatabaseConstants.MESSAGE, this.message);
		migratedRecordJson.put(DatabaseConstants.IS_NATIVE, this.isnative);
		migratedRecordJson.put(DatabaseConstants.IS_LATEST, this.islatest);
		migratedRecordJson.put(DatabaseConstants.LOCAL_FILE_LOCATION, (this.localfilelocation==null)?StringUtils.EMPTY:this.localfilelocation);
		migratedRecordJson.put(DatabaseConstants.CSV_RECORD_NUMBER, this.csvrecordnumber);
		migratedRecordJson.put(DatabaseConstants.PROPERTIES_STATUS, this.propertiesstatus);
		migratedRecordJson.put(DatabaseConstants.PROPERTIES_MESSAGE, this.propertiesmessage);
		migratedRecordJson.put(DatabaseConstants.CSV_UNIQUE_ID, this.csvuniqueid);
		migratedRecordJson.put(DatabaseConstants.FILENAME, (this.filename==null)?StringUtils.EMPTY:this.filename);
		migratedRecordJson.put(DatabaseConstants.FILE_SIZE,this.filesize);
        return migratedRecordJson.toString();
    }
	
	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
//	public String getProperties() {
//		return properties;
//	}

	/**
	 * Sets the properties.
	 *
	 * @param properties the new properties
	 */
//	public void setProperties(final String properties) {
//		this.properties = properties;
//	}

	/**
	 * Gets the noderef.
	 *
	 * @return the noderef
	 */
	public String getNoderef() {
		return noderef;
	}

	/**
	 * Sets the noderef.
	 *
	 * @param noderef the new noderef
	 */
	public void setNoderef(final String noderef) {
		this.noderef = noderef;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(final String status) {
		this.status = status;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message the new message
	 */
	public void setMessage(final String message) {
		this.message = message;
	}

	/**
	 * Gets the localfilelocation.
	 *
	 * @return the localfilelocation
	 */
	public String getLocalfilelocation() {
		return localfilelocation;
	}

	/**
	 * Sets the localfilelocation.
	 *
	 * @param localfilelocation the new localfilelocation
	 */
	public void setLocalfilelocation(final String localfilelocation) {
		this.localfilelocation = localfilelocation;
	}

	/**
	 * Gets the csvfileid.
	 *
	 * @return the csvfileid
	 */
	public long getCsvfileid() {
		return csvfileid;
	}

	/**
	 * Sets the csvfileid.
	 *
	 * @param csvfileid the new csvfileid
	 */
	public void setCsvfileid(final long csvfileid) {
		this.csvfileid = csvfileid;
	}

	/**
	 * Checks if is isnative.
	 *
	 * @return true, if is isnative
	 */
	public boolean isIsnative() {
		return isnative;
	}

	/**
	 * Sets the isnative.
	 *
	 * @param isnative the new isnative
	 */
	public void setIsnative(final boolean isnative) {
		this.isnative = isnative;
	}

	/**
	 * Checks if is islatest.
	 *
	 * @return true, if is islatest
	 */
	public boolean isIslatest() {
		return islatest;
	}

	/**
	 * Sets the islatest.
	 *
	 * @param islatest the new islatest
	 */
	public void setIslatest(final boolean islatest) {
		this.islatest = islatest;
	}

	/**
	 * Gets the csvrecordnumber.
	 *
	 * @return the csvrecordnumber
	 */
	public long getCsvrecordnumber() {
		return csvrecordnumber;
	}

	/**
	 * Sets the csvrecordnumber.
	 *
	 * @param csvrecordnumber the new csvrecordnumber
	 */
	public void setCsvrecordnumber(final long csvrecordnumber) {
		this.csvrecordnumber = csvrecordnumber;
	}

	/**
	 * Gets the propertiesstatus.
	 *
	 * @return the propertiesstatus
	 */
	public String getPropertiesstatus() {
		return propertiesstatus;
	}

	/**
	 * Sets the propertiesstatus.
	 *
	 * @param propertiesstatus the new propertiesstatus
	 */
	public void setPropertiesstatus(final String propertiesstatus) {
		this.propertiesstatus = propertiesstatus;
	}

	/**
	 * Gets the propertiesmessage.
	 *
	 * @return the propertiesmessage
	 */
	public String getPropertiesmessage() {
		return propertiesmessage;
	}

	/**
	 * Sets the propertiesmessage.
	 *
	 * @param propertiesmessage the new propertiesmessage
	 */
	public void setPropertiesmessage(final String propertiesmessage) {
		this.propertiesmessage = propertiesmessage;
	}

	/**
	 * Gets the csvuniqueid.
	 *
	 * @return the csvuniqueid
	 */
	public long getCsvuniqueid() {
		return csvuniqueid;
	}

	/**
	 * Sets the csvuniqueid.
	 *
	 * @param csvuniqueid the new csvuniqueid
	 */
	public void setCsvuniqueid(final long csvuniqueid) {
		this.csvuniqueid = csvuniqueid;
	}

	/**
	 * Gets the filename.
	 *
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Sets the filename.
	 *
	 * @param filename the new filename
	 */
	public void setFilename(final String filename) {
		this.filename = filename;
	}

	/**
	 * Gets the startdate.
	 *
	 * @return the startdate
	 */
	public LocalDateTime getStartdate() {
		return startdate;
	}

	/**
	 * Sets the startdate.
	 *
	 * @param startdate the new startdate
	 */
	public void setStartdate(final LocalDateTime startdate) {
		this.startdate = startdate;
	}

	/**
	 * Gets the enddate.
	 *
	 * @return the enddate
	 */
	public LocalDateTime getEnddate() {
		return enddate;
	}

	/**
	 * Sets the enddate.
	 *
	 * @param enddate the new enddate
	 */
	public void setEnddate(final LocalDateTime enddate) {
		this.enddate = enddate;
	}

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * Sets the properties.
	 *
	 * @param properties the properties
	 */
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	/**
	 * Gets the filesize.
	 *
	 * @return the filesize
	 */
	public long getFilesize() {
		return filesize;
	}

	/**
	 * Sets the filesize.
	 *
	 * @param filesize the new filesize
	 */
	public void setFilesize(final long filesize) {
		this.filesize = filesize;
	}

//	public JsonNode getProperties() {
//		return properties;
//	}
//
//	public void setProperties(final JsonNode properties) {
//		this.properties = properties;
//	}

	



	


}
