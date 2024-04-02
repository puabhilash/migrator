/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

import com.migrator.constants.MigratorConstants;
import com.migrator.database.ConfigurationDetails;

/**
 * The Class MigrationUtils.
 */
public class MigrationUtils {
	
	/** The Constant NODEREF_TEMPLATE. */
	private static final String NODEREF_TEMPLATE = "workspace://SpacesStore/%s";
	
	/** The Constant WORKSPACE_SPACESSTORE. */
	private static final String WORKSPACE_SPACESSTORE = "workspace://SpacesStore/";
	
	/** The Constant MINUTES_PER_HOUR. */
	private static final int MINUTES_PER_HOUR = 60;
	
	/** The Constant SECONDS_PER_MINUTE. */
	private static final int SECONDS_PER_MINUTE = 60;
	
	/** The Constant SECONDS_PER_HOUR. */
	private static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
	

	/**
	 * Generate context URL.
	 *
	 * @param protocol the protocol
	 * @param hostName the host name
	 * @param port the port
	 * @return the string
	 */
	public static String generateContextURL(final String protocol, final String hostName,final int port) {
		final StringBuilder contextBuilder = new StringBuilder();
		final String url = String.format(MigratorConstants.URL_TEMPLATE, protocol,hostName,port,"");
		contextBuilder.append(url);
		return contextBuilder.toString();
	}
	
	/**
	 * Generate alfresco URL.
	 *
	 * @param configurationDetails the configuration details
	 * @return the string
	 */
	public static String generateAlfrescoURL(final ConfigurationDetails configurationDetails) {
		final StringBuilder alfrescoURLBuilder = new StringBuilder();
		alfrescoURLBuilder.append(configurationDetails.getProtocol()).append(MigratorConstants.COLON_DOUBLE_FORWARD_SLASH).append(configurationDetails.getHost())
		.append(MigratorConstants.COLON).append(configurationDetails.getPort()).append(MigratorConstants.FORWARD_SLASH).append(MigratorConstants.ALFRESCO);
		return alfrescoURLBuilder.toString();
	}
	
	/**
	 * Builds the node ref.
	 *
	 * @param nodeId the node id
	 * @return the string
	 */
	public static String buildNodeRef(final String nodeId) {
		final StringBuilder nodeRefBuilder = new StringBuilder();
		if(nodeId.contains(WORKSPACE_SPACESSTORE)) {
			nodeRefBuilder.append(nodeId);
		}else {
			nodeRefBuilder.append(String.format(NODEREF_TEMPLATE, nodeId));
		}
		return nodeRefBuilder.toString();
	}
	
	/**
	 * Format date time.
	 *
	 * @param dateTime the date time
	 * @return the local date time
	 */
	public static LocalDateTime formatDateTime(final String dateTime) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(MigratorConstants.LOCAL_DATE_TIME_FORMAT);
		return LocalDateTime.parse(dateTime, dateTimeFormatter);
	}
	
	/**
	 * To node id.
	 *
	 * @param nodeRef the node ref
	 * @return the string
	 */
	public static String toNodeId(final String nodeRef) {
		if(StringUtils.isEmpty(nodeRef) || StringUtils.isBlank(nodeRef)) {
			return StringUtils.EMPTY;
		}else {
			final int storeIndex = nodeRef.indexOf(MigratorConstants.WORKSPACE_SPACES_STORE);
			if(storeIndex>=0) {
				return nodeRef.replace(MigratorConstants.WORKSPACE_SPACES_STORE, StringUtils.EMPTY);
			}else {
				return nodeRef;
			}
		}
	}
	
	/**
	 * Calculate time taken.
	 *
	 * @param startDate the start date
	 * @param endDate the end date
	 * @return the string
	 */
	public static String calculateTimeTaken(final LocalDateTime startDate, final LocalDateTime endDate) {
		final StringBuilder timeBuilder = new StringBuilder();
		Duration diff = Duration.between(startDate, endDate);
		long seconds = diff.getSeconds();
		System.out.println(seconds);
        long hours = seconds / SECONDS_PER_HOUR;
        long minutes = ((seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
        long secs = (seconds % SECONDS_PER_MINUTE);
        timeBuilder.append(String.format(MigratorConstants.TIME_FORMAT, hours,minutes,secs));
		return timeBuilder.toString();
	}
	
}
