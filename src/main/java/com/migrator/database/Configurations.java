/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.database;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.json.JSONObject;

import com.migrator.constants.DatabaseConstants;

/**
 * The Class Configurations.
 */
@Entity
@Table(name = "configuration",indexes = {@Index(columnList = "confid", unique = true,name="confidindex"),
		@Index(name="configurationnameindex",columnList = "configurationname",unique = true)})
public class Configurations {
	

	/** The confid. */
	@Id
	@GeneratedValue
	@Column(name = "confid")
	private long confid;
	
	/** The configurationname. */
	@Column(name = "configurationname",length = 100)
	private String configurationname;
	
	/** The configuration details. */
	@OneToOne(fetch = FetchType.EAGER,mappedBy="configurations",cascade = CascadeType.ALL)
	ConfigurationDetails configurationDetails;
	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
    public String toString() {
		final JSONObject configurationJson = new JSONObject();
		configurationJson.put(DatabaseConstants.CONFID, this.confid);
		configurationJson.put(DatabaseConstants.CONFIGURATION_NAME, this.configurationname);
        return configurationJson.toString();
    }
	
	/**
	 * Gets the confid.
	 *
	 * @return the confid
	 */
	public long getConfid() {
		return confid;
	}

	/**
	 * Sets the confid.
	 *
	 * @param confid the new confid
	 */
	public void setConfid(final long confid) {
		this.confid = confid;
	}

	/**
	 * Gets the configurationname.
	 *
	 * @return the configurationname
	 */
	public String getConfigurationname() {
		return configurationname;
	}

	/**
	 * Sets the configurationname.
	 *
	 * @param configurationname the new configurationname
	 */
	public void setConfigurationname(final String configurationname) {
		this.configurationname = configurationname;
	}

	/**
	 * Gets the configuration details.
	 *
	 * @return the configuration details
	 */
	public ConfigurationDetails getConfigurationDetails() {
		return configurationDetails;
	}

	/**
	 * Sets the configuration details.
	 *
	 * @param configurationDetails the new configuration details
	 */
	public void setConfigurationDetails(ConfigurationDetails configurationDetails) {
		this.configurationDetails = configurationDetails;
	}

}
