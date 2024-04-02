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
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.json.JSONObject;

import com.tcs.constants.DatabaseConstants;

/**
 * The Class ConfigurationDetails.
 */
@Entity
@Table(name = "configurationdetails",indexes = {@Index(columnList = "configid", unique = true,name="configidindex")})
@IdClass(ConfigurationDetailsId.class)
public class ConfigurationDetails implements Serializable {
	

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The configurations. */
	@Id
	@OneToOne
	@JoinColumn(name = "confid")
	private Configurations configurations;
	
	/** The configid. */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long configid;
	
	/** The host. */
	@Column(name = "host")
	private String host;
	
	/** The port. */
	@Column(name = "port")
	private int port;
	
	/** The protocol. */
	@Column(name = "protocol")
	private String protocol;
	
	/** The appname. */
	@Column(name = "appname")
	private String appname;
	
	/** The username. */
	@Column(name = "username")
	private String username;
	
	/** The password. */
	@Column(name = "password")
	private String password;
	
	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
    public String toString() {
		final JSONObject migratedRecordJson = new JSONObject();
		migratedRecordJson.put(DatabaseConstants.CONFIG_ID, this.configid);
		migratedRecordJson.put(DatabaseConstants.HOST, this.host);
		migratedRecordJson.put(DatabaseConstants.PORT, this.port);
		migratedRecordJson.put(DatabaseConstants.PROTOCOL, this.protocol);
		migratedRecordJson.put(DatabaseConstants.APPNAME, this.appname);
		migratedRecordJson.put(DatabaseConstants.USERNAME, this.username);
		migratedRecordJson.put(DatabaseConstants.PASSWORD, this.password);
        return migratedRecordJson.toString();
    }

	/**
	 * Gets the host.
	 *
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the host.
	 *
	 * @param host the new host
	 */
	public void setHost(final String host) {
		this.host = host;
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port.
	 *
	 * @param port the new port
	 */
	public void setPort(final int port) {
		this.port = port;
	}

	/**
	 * Gets the protocol.
	 *
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Sets the protocol.
	 *
	 * @param protocol the new protocol
	 */
	public void setProtocol(final String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Gets the appname.
	 *
	 * @return the appname
	 */
	public String getAppname() {
		return appname;
	}

	/**
	 * Sets the appname.
	 *
	 * @param appname the new appname
	 */
	public void setAppname(final String appname) {
		this.appname = appname;
	}

//	public long getConfigid() {
//		return configid;
//	}
//
//	public void setConfigid(final long configid) {
//		this.configid = configid;
//	}


	/**
 * Gets the configurations.
 *
 * @return the configurations
 */
public Configurations getConfigurations() {
		return configurations;
	}

	/**
	 * Sets the configurations.
	 *
	 * @param configurations the new configurations
	 */
	public void setConfigurations(final Configurations configurations) {
		this.configurations = configurations;
	}

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

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 *
	 * @param username the new username
	 */
	public void setUsername(final String username) {
		this.username = username;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 *
	 * @param password the new password
	 */
	public void setPassword(final String password) {
		this.password = password;
	}


}
