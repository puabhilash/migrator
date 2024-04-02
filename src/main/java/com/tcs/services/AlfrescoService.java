/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.services;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.tcs.constants.MigratorConstants;
import com.tcs.utils.MigrationUtils;

/**
 * The Class AlfrescoService.
 */
@Service
public class AlfrescoService {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoService.class);
	
	/** The Constant UPLOAD_URL. */
	private static final String UPLOAD_URL = "%s/s/api/upload";
	
	/** The Constant PROPERTIES_UPDATE_URL. */
	private static final String PROPERTIES_UPDATE_URL = "%s/s/api/node/%s/formprocessor";
	
	/** The Constant CREATE_FOLDER_URL. */
	private static final String CREATE_FOLDER_URL = "%s/api/-default-/public/alfresco/versions/1/nodes/%s/children?autoRename=true&include=allowableOperations";
	
	/** The Constant NODE_DETAILS_URL. */
	private static final String NODE_DETAILS_URL = "%s/api/-default-/public/alfresco/versions/1/nodes/%s?include=allowableOperations,path";
	
	/**
	 * Validate connection.
	 *
	 * @param URL the url
	 * @param userName the user name
	 * @param password the password
	 * @return the JSON object
	 */
	public JSONObject validateConnection(final String URL,final String userName, final String password) {
		final JSONObject responseJson = new JSONObject();
		LOGGER.info("URL {}",URL);
		final JSONObject userJson = new JSONObject();
		userJson.put(MigratorConstants.KEY_USER_ID, userName);
		userJson.put(MigratorConstants.KEY_PASSWORD, password);
		final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		final HttpPost httpPost = new HttpPost(URL+MigratorConstants.ALFRESCO_TICKET_SERVICE);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
		final StringEntity requestEntity = new StringEntity(
				userJson.toString(),
				ContentType.APPLICATION_JSON);
		httpPost.setEntity(requestEntity);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			final HttpEntity result = response.getEntity();
			final String responseString = EntityUtils.toString(result, StandardCharsets.UTF_8);
			LOGGER.info("response alfresco {}",responseString);
			LOGGER.info("status code {}",response.getStatusLine().getStatusCode());
			responseJson.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
			if(HttpStatus.SC_OK == response.getStatusLine().getStatusCode() || HttpStatus.SC_CREATED == response.getStatusLine().getStatusCode()) {
				responseJson.put(MigratorConstants.KEY_MESSAGE, "Alfresco connection success!");
			}else if(HttpStatus.SC_FORBIDDEN == response.getStatusLine().getStatusCode()){
				responseJson.put(MigratorConstants.KEY_MESSAGE, "Login Failed!");
			}else {
				LOGGER.info("reason phrase {}",response.getStatusLine().getReasonPhrase());
				responseJson.put(MigratorConstants.KEY_MESSAGE, response.getStatusLine().getReasonPhrase());
			}
		}catch (IOException ioException) {
			responseJson.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseJson.put(MigratorConstants.KEY_MESSAGE, ioException.getMessage());
			LOGGER.error("ioException occured while executing method validateConnection {}",ioException);
		}
		finally {
			try {
				if(null!=response) {
					response.close();
				}
				if(null != httpClient) {
					httpClient.close();
				}
			} catch (IOException ioException2) {
				LOGGER.error("ioException2 occured while executing method validateConnection {}",ioException2);
			}
		}
		return responseJson;
	}
	
	/**
	 * Gets the alfresco types.
	 *
	 * @param URL the url
	 * @param userName the user name
	 * @param password the password
	 * @param namespace the namespace
	 * @return the alfresco types
	 */
	public Map<String, Map<String, String>> getAlfrescoTypes(final String URL,final String userName,final String password,final String namespace,final String type) {
		final Map<String, Map<String, String>> responseMap = new HashMap<>();
		final Map<String, String> fileTypes = new HashMap<>();
		final Map<String, String> aspects = new HashMap<>();
		String url = URL;
//		final Map<String, Map<String, String>> fileTypeProperties = new HashMap<>();
//		final Map<String, Map<String, String>> aspectProperties = new HashMap<>();
		if(StringUtils.isNotEmpty(namespace) || StringUtils.isNotBlank(namespace)) {
			url+="?nsp="+namespace;
		}
		if(StringUtils.isNotEmpty(type) || StringUtils.isNotBlank(type)) {
			url+="/"+type;
		}
		LOGGER.info("executing URL {}",url);
		final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		//"http://localhost:8080/alfresco/s/api/classes"
		final HttpGet httpGet = new HttpGet(url);
		final StringBuilder userPasswordBuilder = new StringBuilder();
		userPasswordBuilder.append(userName).append(":").append(password);
		final byte[] encodedAuth = Base64.encodeBase64(userPasswordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
		final String authHeader = "Basic " + new String(encodedAuth);
		httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			final HttpEntity result = response.getEntity();
			final String responseString = EntityUtils.toString(result, StandardCharsets.UTF_8);
//			LOGGER.info("response string {}",responseString);
			if(HttpStatus.SC_OK==response.getStatusLine().getStatusCode()) {
				JSONArray responseJson = new JSONArray();
				if(StringUtils.isNotBlank(type) && StringUtils.isNotEmpty(type)) {
					responseJson.put(new JSONObject(responseString));
				}else {
					responseJson = new JSONArray(responseString);
				}
				for(int index=0;index<responseJson.length();index++) {
					final boolean isAspect = responseJson.getJSONObject(index).getBoolean(MigratorConstants.KEY_ISASPECT);
					if(isAspect) {
						final JSONObject properties = responseJson.getJSONObject(index).getJSONObject(MigratorConstants.KEY_PROPERTIES);
						final Map<String, String> propDetails = new HashMap<>();
						for(String key:properties.keySet()) {
							propDetails.put(key, properties.getJSONObject(key).getString(MigratorConstants.KEY_TITLE));
						}
//						aspectProperties.put(responseJson.getJSONObject(index).getString(MigratorConstants.KEY_NAME), propDetails);
						aspects.put(responseJson.getJSONObject(index).getString(MigratorConstants.KEY_NAME), responseJson.getJSONObject(index).getString(MigratorConstants.KEY_TITLE));
					}else {
						
						final JSONObject properties = responseJson.getJSONObject(index).getJSONObject(MigratorConstants.KEY_PROPERTIES);
						final Map<String, String> propDetails = new HashMap<>();
						for(String key:properties.keySet()) {
							propDetails.put(key, properties.getJSONObject(key).getString(MigratorConstants.KEY_TITLE));
						}
//						fileTypeProperties.put(responseJson.getJSONObject(index).getString(MigratorConstants.KEY_NAME), propDetails);
						fileTypes.put(responseJson.getJSONObject(index).getString(MigratorConstants.KEY_NAME), responseJson.getJSONObject(index).getString(MigratorConstants.KEY_TITLE));
					}
				}
				responseMap.put(MigratorConstants.KEY_FILETYPE_DEF, fileTypes);
//				responseMap.put(MigratorConstants.KEY_FILETYPE_PROP_MAP, responseJson);
				responseMap.put(MigratorConstants.KEY_ASPECT_DEF, aspects);
//				responseMap.put(MigratorConstants.KEY_ASPECT_PROP_MAP, aspectProperties);
			}else {
				responseMap.put(MigratorConstants.KEY_FILETYPE_DEF, new HashMap<>());
//				responseMap.put(MigratorConstants.KEY_FILETYPE_PROP_MAP, new HashMap<>());
				responseMap.put(MigratorConstants.KEY_ASPECT_DEF, new HashMap<>());
//				responseMap.put(MigratorConstants.KEY_ASPECT_PROP_MAP, new HashMap<>());
			}
		}catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method getAlfrescoTypes {}",ioException);
			responseMap.put(MigratorConstants.KEY_FILETYPE_DEF, new HashMap<>());
			responseMap.put(MigratorConstants.KEY_ASPECT_DEF, new HashMap<>());
		}
		finally {
			try {
				if(null!=response) {
					response.close();
				}
				if(null!=httpClient) {
					httpClient.close();
				}
			} catch (IOException ioException2) {
				LOGGER.error("ioException occured while executing method getAlfrescoTypes {}",ioException2);
			}
		}
		return responseMap;
	}
	
	
	/**
	 * Gets the alfresco types properties.
	 *
	 * @param URL the url
	 * @param userName the user name
	 * @param password the password
	 * @return the alfresco types properties
	 */
	public JSONArray getAlfrescoTypesProperties(final String URL,final String userName,final String password) {
		JSONArray respArray = new JSONArray();
		LOGGER.info("executing URL {}",URL);
		final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		//"http://localhost:8080/alfresco/s/api/classes"
		final HttpGet httpGet = new HttpGet(URL);
		final StringBuilder userPasswordBuilder = new StringBuilder();
		userPasswordBuilder.append(userName).append(":").append(password);
		final byte[] encodedAuth = Base64.encodeBase64(userPasswordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
		final String authHeader = "Basic " + new String(encodedAuth);
		httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			final HttpEntity result = response.getEntity();
			final String responseString = EntityUtils.toString(result, StandardCharsets.UTF_8);
			final JSONObject responseJson = new JSONObject(responseString);
			LOGGER.info("code ----> {}",response.getStatusLine().getStatusCode());
			if(HttpStatus.SC_OK==response.getStatusLine().getStatusCode()) {
				final JSONObject properties = responseJson.getJSONObject(MigratorConstants.KEY_PROPERTIES);
				respArray=getProperties(properties);
			}else {
				respArray.put(new JSONObject());
			}
		}catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method getAlfrescoTypes {}",ioException);
		}
		finally {
			try {
				if(null!=response) {
					response.close();
				}
				if(null!=httpClient) {
					httpClient.close();
				}
			} catch (IOException ioException2) {
				LOGGER.error("ioException occured while executing method getAlfrescoTypes {}",ioException2);
			}
		}
		return respArray;
	}
	
	/**
	 * Gets the properties.
	 *
	 * @param propertiesJson the properties json
	 * @return the properties
	 */
	private JSONArray getProperties(final JSONObject propertiesJson) {
		final JSONArray optimisedArrayProperties = new JSONArray();
		LOGGER.info("properties {}",propertiesJson);
		final Set<String> keys = propertiesJson.keySet();
		keys.forEach(keyname->{
			final JSONObject propJson = propertiesJson.getJSONObject(keyname);
			final JSONObject optimisedProperties = new JSONObject();
			optimisedProperties.put(MigratorConstants.KEY_PROPERTY_QNAME,keyname);
			final String qNameTitle = (StringUtils.isEmpty(propJson.getString(MigratorConstants.KEY_TITLE))|| StringUtils.isBlank(propJson.getString(MigratorConstants.KEY_TITLE)))?keyname:propJson.getString(MigratorConstants.KEY_TITLE);
			optimisedProperties.put(MigratorConstants.KEY_PROPERTY_TITLE, qNameTitle);
			optimisedArrayProperties.put(optimisedProperties);
		});
		return optimisedArrayProperties;
	}
	
	/**
	 * Gets the children folders.
	 *
	 * @param URL the url
	 * @param username the username
	 * @param password the password
	 * @param nodeId the node id
	 * @return the children folders
	 */
	public JSONObject getChildrenFolders(final String URL,final String username,final String password,final String nodeId) {
		JSONObject resJson = new JSONObject();
		final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		final HttpGet httpGet = new HttpGet(URL+"/api/-default-/public/alfresco/versions/1/nodes/"+nodeId+"/children?skipCount=0&maxItems="+Integer.MAX_VALUE+"&where=(isFolder%3Dtrue)&include=allowableOperations%2Cproperties");
		final StringBuilder userPasswordBuilder = new StringBuilder();
		userPasswordBuilder.append(username).append(MigratorConstants.COLON).append(password);
		final byte[] encodedAuth = Base64.encodeBase64(userPasswordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
		final String authHeader = "Basic " + new String(encodedAuth);
		httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			final HttpEntity result = response.getEntity();
			final String responseString = EntityUtils.toString(result, StandardCharsets.UTF_8);
			final JSONObject responseJson = new JSONObject(responseString);
			LOGGER.info("response status {}",response.getStatusLine().getStatusCode());
			if(HttpStatus.SC_OK==response.getStatusLine().getStatusCode()) {
				resJson = responseJson.getJSONObject(MigratorConstants.KEY_LIST);
				resJson.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
				resJson.put(MigratorConstants.KEY_MESSAGE, response.getStatusLine().getReasonPhrase());
			}else {
				resJson.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
				resJson.put(MigratorConstants.KEY_MESSAGE, response.getStatusLine().getReasonPhrase());
			}
		}catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method {}",ioException);
			if(ioException instanceof HttpHostConnectException) {
				resJson.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_REQUEST_TIMEOUT);
				resJson.put(MigratorConstants.KEY_MESSAGE, ioException.getMessage());
			}else {
				resJson.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
				resJson.put(MigratorConstants.KEY_MESSAGE, ioException.getMessage());
			}
		}
		finally {
			try {
				if(null!=response) {
					response.close();
				}
				if(null!=httpClient) {
					httpClient.close();
				}
			} catch (IOException ioException2) {
				resJson.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
				resJson.put(MigratorConstants.KEY_MESSAGE, ioException2.getMessage());
				LOGGER.error("ioException2 occured while executing method {}",ioException2);
			}
		}
		return resJson;
	}
	
	
	/**
	 * Upload file.
	 *
	 * @param file the file
	 * @param properties the properties
	 * @param URL the url
	 * @param isNativeFile the is native file
	 * @param fileType the file type
	 * @param destination the destination
	 * @param username the username
	 * @param password the password
	 * @return the JSON object
	 */
	public JSONObject UploadFile(final File file,final JSONObject properties, final String URL, final boolean isNativeFile,final String fileType,
			final String destination,final String username,final String password) {
		final JSONObject responseJsonObj = new JSONObject();
		final CloseableHttpClient httpClient = HttpClientBuilder.create().disableAutomaticRetries().build();
		final String uploadUrl = String.format(UPLOAD_URL, URL);
		LOGGER.info("Upload URL {}",uploadUrl);
		final HttpPost httpPost = new HttpPost(uploadUrl);
		final StringBuilder userPasswordBuilder = new StringBuilder();
		userPasswordBuilder.append(username).append(MigratorConstants.COLON).append(password);
		final byte[] encodedAuth = Base64.encodeBase64(userPasswordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
		final String authHeader = "Basic " + new String(encodedAuth);
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		httpPost.setHeader(HttpHeaders.ACCEPT,ContentType.APPLICATION_JSON.toString());
		CloseableHttpResponse response = null;
		LOGGER.info("upload destination {}",destination);
		try {
			final HttpEntity entity = MultipartEntityBuilder.create().addTextBody(MigratorConstants.KEY_CONTENT_TYPE, fileType)
					.addTextBody(MigratorConstants.KEY_DESTINATION, MigratorConstants.WORKSPACE_SPACES_STORE+destination)
					.addTextBody(MigratorConstants.KEY_OVERWRITE, Boolean.FALSE.booleanValue()+"")
					.addBinaryBody(MigratorConstants.KEY_FILEDATA, file).build();
			ProgressEntityWrapper.ProgressListener pListener = 
				      percentage -> assertFalse(Float.compare(percentage, 100) > 0);
			httpPost.setEntity(new ProgressEntityWrapper(entity, pListener));
//			httpPost.setEntity(entity);
			response = httpClient.execute(httpPost);
			final HttpEntity result = response.getEntity();
			final String responseString = EntityUtils.toString(result, StandardCharsets.UTF_8);
			final JSONObject responseJson = new JSONObject(responseString);
			LOGGER.info("upload response {}",responseString);
			if(HttpStatus.SC_OK==response.getStatusLine().getStatusCode()) {
				final String nodeRef = responseJson.getString(MigratorConstants.KEY_NODE_REF);
				responseJsonObj.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
				responseJsonObj.put(MigratorConstants.KEY_STATUS_CODE, response.getStatusLine().getStatusCode());
				responseJsonObj.put(MigratorConstants.KEY_NODE_REF, nodeRef);
				responseJsonObj.put(MigratorConstants.KEY_STATUS_MESSAGE, responseJson.getJSONObject(MigratorConstants.KEY_STATUS).getString(MigratorConstants.KEY_DESCRIPTION));
				JSONObject propertiesResp = new JSONObject();
				propertiesResp = updateMetadata(URL, nodeRef, properties,username,password);
				if(propertiesResp.has(MigratorConstants.KEY_PERSISTED_OBJECT)) {
					responseJsonObj.put(MigratorConstants.KEY_PROPERTIES_STATUS, HttpStatus.SC_OK+"");
					responseJsonObj.put(MigratorConstants.KEY_PROPERTIES_MESSAGE, "Properties set successfully");
				}else {
					responseJsonObj.put(MigratorConstants.KEY_PROPERTIES_STATUS, propertiesResp.getJSONObject(MigratorConstants.KEY_STATUS).getInt(MigratorConstants.KEY_CODE)+"");
					responseJsonObj.put(MigratorConstants.KEY_PROPERTIES_MESSAGE, propertiesResp.getString(MigratorConstants.KEY_MESSAGE));
				}
			}else {
				LOGGER.info("response {}",responseString);
				final String message = responseJson.getString(MigratorConstants.KEY_MESSAGE);
				responseJsonObj.put(MigratorConstants.KEY_PROPERTIES, properties);
				responseJsonObj.put(MigratorConstants.KEY_NODE_REF, StringUtils.EMPTY);
				responseJsonObj.put(MigratorConstants.KEY_STATUS_CODE, response.getStatusLine().getStatusCode());
				responseJsonObj.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
				responseJsonObj.put(MigratorConstants.KEY_MESSAGE, message);
				responseJsonObj.put(MigratorConstants.KEY_PROPERTIES_STATUS, StringUtils.EMPTY);
				responseJsonObj.put(MigratorConstants.KEY_PROPERTIES_MESSAGE, StringUtils.EMPTY);
			}
			LOGGER.info("metadata {}",properties);
		}catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method UploadFile {}",ioException);
			LOGGER.error("--->{}",ioException instanceof UnknownHostException);
			if(ioException instanceof UnknownHostException) {
				responseJsonObj.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_NOT_FOUND);
				responseJsonObj.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_NOT_FOUND);
			}else {
				responseJsonObj.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_INTERNAL_SERVER_ERROR);
				responseJsonObj.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}
			responseJsonObj.put(MigratorConstants.KEY_PROPERTIES, properties);
			responseJsonObj.put(MigratorConstants.KEY_NODE_REF, StringUtils.EMPTY);
			responseJsonObj.put(MigratorConstants.KEY_STATUS_MESSAGE, ioException.getMessage());
			responseJsonObj.put(MigratorConstants.KEY_PROPERTIES_STATUS, StringUtils.EMPTY);
			responseJsonObj.put(MigratorConstants.KEY_PROPERTIES_MESSAGE, StringUtils.EMPTY);
		}
		finally {
			try {
				if(null!=response) {
					response.close();
				}
				if(null!=httpClient) {
					httpClient.close();
				}
				
			} catch (IOException ioException2) {
				LOGGER.error("ioException2 occured while executing method UploadFile {}",ioException2);
			}
		}
		LOGGER.info("upload response alfresco {}",responseJsonObj);
		return responseJsonObj;
	}
	
	/**
	 * Update metadata.
	 *
	 * @param alfrescoBaseURL the alfresco base URL
	 * @param nodeRef the node ref
	 * @param properties the properties
	 * @param userName the user name
	 * @param password the password
	 * @return the JSON object
	 */
	public JSONObject updateMetadata(final String alfrescoBaseURL,final String nodeRef,final JSONObject properties,final String userName,final String password) {
		JSONObject responseJson = null;
		final CloseableHttpClient httpClient = HttpClientBuilder.create().disableAutomaticRetries().build();
		final String updateURL = String.format(PROPERTIES_UPDATE_URL,alfrescoBaseURL, nodeRef.replace("://", "/"));
		LOGGER.info("update url {}",updateURL);
		final HttpPost httpPost = new HttpPost(updateURL);
		final StringBuilder userPasswordBuilder = new StringBuilder();
		userPasswordBuilder.append(userName).append(MigratorConstants.COLON).append(password);
		final byte[] encodedAuth = Base64.encodeBase64(userPasswordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
		final String authHeader = "Basic " + new String(encodedAuth);
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		httpPost.setHeader(HttpHeaders.ACCEPT,ContentType.APPLICATION_JSON.toString());
		CloseableHttpResponse response = null;
		LOGGER.info("properties json {}",properties);
		try {
			final String propertiesObj = properties.toString().replaceAll("mdrkm:", "prop_mdrkm_").replaceAll("cm:", "prop_cm_").replaceAll("mdr:", "prop_mdr_");
			final StringEntity requestEntity = new StringEntity(
					propertiesObj,
					ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);
			response = httpClient.execute(httpPost);
			final HttpEntity result = response.getEntity();
			final String responseString = EntityUtils.toString(result, StandardCharsets.UTF_8);
//			final JSONObject responseJson = new JSONObject(responseString);
			LOGGER.info("metadata response {}",responseString);
			responseJson = new JSONObject(responseString);
//			if(HttpStatus.SC_OK==response.getStatusLine().getStatusCode()) {
//				migrationTable.setPropertiesMessage(responseJson.getString(McDermottConstants.KEY_MESSAGE));
//				migrationTable.setPropertiesStatus(Boolean.TRUE.toString());
//			}else {
//				migrationTable.setPropertiesMessage(response.getStatusLine().getStatusCode()+":"+responseJson.getString(McDermottConstants.KEY_CODE));
//				migrationTable.setPropertiesStatus(Boolean.FALSE.toString());
//			}
		}catch (IOException ioException) {
			LOGGER.error("iioException occured while executing method updateMetadata {}",ioException);
		}
		finally {
			try {
				if(null!=response) {
					response.close();
				}
				if(null!=httpClient) {
					httpClient.close();
				}
			} catch (IOException ioException2) {
				LOGGER.error("ioException2 occured while executing method updateMetadata {}",ioException2);
			}
		}
		return responseJson;
	}
	
	/**
	 * Creates the folder.
	 *
	 * @param alfrescoBaseURL the alfresco base URL
	 * @param nodeRef the node ref
	 * @param folderName the folder name
	 * @param userName the user name
	 * @param password the password
	 * @return the JSON object
	 */
	public JSONObject createFolder(final String alfrescoBaseURL,final String nodeRef,final String folderName,final String userName,final String password) {
		JSONObject responseJson = null;
		final CloseableHttpClient httpClient = HttpClientBuilder.create().disableAutomaticRetries().build();
		final String updateURL = String.format(CREATE_FOLDER_URL,alfrescoBaseURL, nodeRef.replace("://", "/"));
		LOGGER.info("create folder url {}",updateURL);
		final HttpPost httpPost = new HttpPost(updateURL);
		final StringBuilder userPasswordBuilder = new StringBuilder();
		userPasswordBuilder.append(userName).append(MigratorConstants.COLON).append(password);
		final byte[] encodedAuth = Base64.encodeBase64(userPasswordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
		final String authHeader = "Basic " + new String(encodedAuth);
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		httpPost.setHeader(HttpHeaders.ACCEPT,ContentType.APPLICATION_JSON.toString());
		CloseableHttpResponse response = null;
		final JSONObject folderJson = new JSONObject();
		folderJson.put(MigratorConstants.KEY_NAME, folderName);
		folderJson.put(MigratorConstants.KEY_NODE_TYPE, MigratorConstants.TYPE_CM_FOLDER);
		LOGGER.info("properties json {}",folderJson);
		try {
			final StringEntity requestEntity = new StringEntity(
					folderJson.toString(),
					ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);
			response = httpClient.execute(httpPost);
			final HttpEntity result = response.getEntity();
			final String responseString = EntityUtils.toString(result, StandardCharsets.UTF_8);
//			final JSONObject responseJson = new JSONObject(responseString);
			LOGGER.info("metadata response {}",responseString);
			responseJson = new JSONObject(responseString);
//			if(HttpStatus.SC_OK==response.getStatusLine().getStatusCode()) {
//				migrationTable.setPropertiesMessage(responseJson.getString(McDermottConstants.KEY_MESSAGE));
//				migrationTable.setPropertiesStatus(Boolean.TRUE.toString());
//			}else {
//				migrationTable.setPropertiesMessage(response.getStatusLine().getStatusCode()+":"+responseJson.getString(McDermottConstants.KEY_CODE));
//				migrationTable.setPropertiesStatus(Boolean.FALSE.toString());
//			}
		}catch (IOException ioException) {
			LOGGER.error("iioException occured while executing method updateMetadata {}",ioException);
		}
		finally {
			try {
				if(null!=response) {
					response.close();
				}
				if(null!=httpClient) {
					httpClient.close();
				}
			} catch (IOException ioException2) {
				LOGGER.error("ioException2 occured while executing method updateMetadata {}",ioException2);
			}
		}
		return responseJson;
	}
	
	/**
	 * Gets the alfresco node details.
	 *
	 * @param nodeid the nodeid
	 * @param username the username
	 * @param password the password
	 * @param alfrescoBaseURL the alfresco base URL
	 * @return the alfresco node details
	 */
	public JSONObject getAlfrescoNodeDetails(final String nodeid, final String username, final String password,final String alfrescoBaseURL) {
		final JSONObject responseJson = new JSONObject();
		final CloseableHttpClient httpClient = HttpClientBuilder.create().disableAutomaticRetries().build();
		final String nodeURL = String.format(NODE_DETAILS_URL,alfrescoBaseURL, nodeid.replace("://", "/"));
		LOGGER.info("executing node url {}",nodeURL);
		LOGGER.info("node id {}",nodeid);
		final HttpGet httpGet = new HttpGet(nodeURL);
		final StringBuilder userPasswordBuilder = new StringBuilder();
		userPasswordBuilder.append(username).append(MigratorConstants.COLON).append(password);
		final byte[] encodedAuth = Base64.encodeBase64(userPasswordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
		final String authHeader = "Basic " + new String(encodedAuth);
		httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		httpGet.setHeader(HttpHeaders.ACCEPT,ContentType.APPLICATION_JSON.toString());
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			final HttpEntity result = response.getEntity();
			final String responseString = EntityUtils.toString(result, StandardCharsets.UTF_8);
			LOGGER.info("metadata response {}",responseString);
			if(HttpStatus.SC_OK==response.getStatusLine().getStatusCode()) {
				responseJson.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
				responseJson.put(MigratorConstants.KEY_DETAILS, new JSONObject(responseString).getJSONObject(MigratorConstants.KEY_ENTRY));
			}else {
				responseJson.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
				responseJson.put(MigratorConstants.KEY_DETAILS, new JSONObject(responseString).getJSONObject(MigratorConstants.KEY_ERROR));
			}
		}catch (IOException ioException) {
			responseJson.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseJson.put(MigratorConstants.KEY_DETAILS, new JSONObject());
			LOGGER.error("iioException occured while executing method updateMetadata {}",ioException);
		}
		finally {
			try {
				if(null!=response) {
					response.close();
				}
				if(null!=httpClient) {
					httpClient.close();
				}
			} catch (IOException ioException2) {
				LOGGER.error("ioException2 occured while executing method updateMetadata {}",ioException2);
			}
		}
		return responseJson;
	}
	//final File file,final JSONObject properties, final String URL, final boolean isNativeFile,final String fileType,
	
	/**
	 * Creates the node.
	 *
	 * @param alfrescoBaseURL the alfresco base URL
	 * @param properties the properties
	 * @param fileType the file type
	 * @param inputStream the input stream
	 * @param parentId the parent id
	 * @param originalFileName the original file name
	 * @param userName the user name
	 * @param password the password
	 * @return the JSON object
	 */
	public JSONObject createNode(final String alfrescoBaseURL,final JSONObject properties, final String fileType,final S3ObjectInputStream inputStream,final String parentId,final String originalFileName,
			final String userName,final String password) {
		final JSONObject createNodebuilBuilder = new JSONObject();
		final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		final String URL = String.format(MigratorConstants.DOUBLE_STRING, alfrescoBaseURL,MigratorConstants.NODE_URL+parentId+"/children?"+MigratorConstants.PARAM_AUTO_RENAME+"="+Boolean.TRUE.booleanValue());
		final HttpPost httpPost = new HttpPost(URL);
		final StringBuilder userPasswordBuilder = new StringBuilder();
		userPasswordBuilder.append(userName).append(":").append(password);
		LOGGER.info("username {} password {}",userName,password);
		final byte[] encodedAuth = Base64.encodeBase64(userPasswordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
		final String authHeader = MigratorConstants.BASIC + new String(encodedAuth);
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		if(LOGGER.isDebugEnabled()) {			
			LOGGER.debug("file type {}",fileType);
			LOGGER.debug("file Name {}",originalFileName);
		}
		LOGGER.info("executing create node url {}",URL);
		CloseableHttpResponse response = null;
		try {
			final HttpEntity entity = MultipartEntityBuilder.create().addTextBody(MigratorConstants.KEY_NODE_TYPE, fileType)
					.addTextBody(MigratorConstants.PARAM_AUTO_RENAME, Boolean.TRUE.toString())
					.addBinaryBody(MigratorConstants.FILEDATA,inputStream,ContentType.MULTIPART_FORM_DATA,originalFileName)
					.setCharset(Charset.forName(MigratorConstants.UTF_8)).setMode(HttpMultipartMode.BROWSER_COMPATIBLE).build();
			ProgressEntityWrapper.ProgressListener pListener = 
				      percentage -> assertFalse(Float.compare(percentage, 100) > 0);
			httpPost.setEntity(new ProgressEntityWrapper(entity, pListener));
//			httpPost.setEntity(entity);
			response = httpClient.execute(httpPost);
			final HttpEntity result = response.getEntity();
			final String responseString = EntityUtils.toString(result, StandardCharsets.UTF_8);
			createNodebuilBuilder.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode() || HttpStatus.SC_CREATED == response.getStatusLine().getStatusCode()) {
				final JSONObject responseJson = new JSONObject(responseString);
				final String nodeId = responseJson.getJSONObject(MigratorConstants.KEY_ENTRY).getString(MigratorConstants.KEY_ID);
				createNodebuilBuilder.put(MigratorConstants.KEY_NODE_REF, nodeId);
				LOGGER.info("response {}",responseJson);
				createNodebuilBuilder.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
				createNodebuilBuilder.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_OK);
				createNodebuilBuilder.put(MigratorConstants.KEY_STATUS_MESSAGE, "File Uploaded Successfully!");
				JSONObject propertiesResp = new JSONObject();
				propertiesResp = updateNodeProperties(alfrescoBaseURL, nodeId, properties,userName,password);
				if(propertiesResp.has(MigratorConstants.KEY_PERSISTED_OBJECT)) {
					createNodebuilBuilder.put(MigratorConstants.KEY_PROPERTIES_STATUS, HttpStatus.SC_OK+"");
					createNodebuilBuilder.put(MigratorConstants.KEY_PROPERTIES_MESSAGE, "Properties set successfully");
				}else {
					createNodebuilBuilder.put(MigratorConstants.KEY_PROPERTIES_STATUS, propertiesResp.getString(MigratorConstants.KEY_STATUS));
					createNodebuilBuilder.put(MigratorConstants.KEY_PROPERTIES_MESSAGE, propertiesResp.getString(MigratorConstants.KEY_MESSAGE));
				}
				LOGGER.info("upload response {}",createNodebuilBuilder);
			} 
//			else if(HttpStatus.SC_CONFLICT == response.getStatusLine().getStatusCode()){
//				//pass on duplicate file name error to re-trigger upload
//				createNodebuilBuilder.put(MigratorConstants.KEY_NODE_ID, StringUtils.EMPTY);
//				LOGGER.error("conflict error occured{}",responseString);
//				createNodebuilBuilder.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
//				createNodebuilBuilder.put(MigratorConstants.KEY_STATUS_CODE, response.getStatusLine().getStatusCode());
//				createNodebuilBuilder.put(MigratorConstants.KEY_STATUS_MESSAGE, response.getStatusLine().getStatusCode());
//			}
			else {
				createNodebuilBuilder.put(MigratorConstants.KEY_NODE_REF, StringUtils.EMPTY);
				LOGGER.error("error occured {}",responseString);
				createNodebuilBuilder.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
				createNodebuilBuilder.put(MigratorConstants.KEY_STATUS_CODE, response.getStatusLine().getStatusCode());
				if(HttpStatus.SC_BAD_REQUEST == response.getStatusLine().getStatusCode()) {
					final JSONObject responseJson = new JSONObject(responseString);
					createNodebuilBuilder.put(MigratorConstants.KEY_STATUS_MESSAGE, responseJson.getJSONObject(MigratorConstants.KEY_ERROR).getString(MigratorConstants.KEY_ERROR_KEY));
				}else if(HttpStatus.SC_UNPROCESSABLE_ENTITY == response.getStatusLine().getStatusCode()){
					final JSONObject responseJson = new JSONObject(responseString);
					createNodebuilBuilder.put(MigratorConstants.KEY_STATUS_MESSAGE, responseJson.getJSONObject(MigratorConstants.KEY_ERROR).getString(MigratorConstants.KEY_ERROR_KEY));
				}else {
					createNodebuilBuilder.put(MigratorConstants.KEY_STATUS_MESSAGE, response.getStatusLine().getReasonPhrase());
				}
				createNodebuilBuilder.put(MigratorConstants.KEY_PROPERTIES_STATUS, response.getStatusLine().getStatusCode());
				createNodebuilBuilder.put(MigratorConstants.KEY_PROPERTIES_MESSAGE, response.getStatusLine().getReasonPhrase());
			}
		} catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method createNode {} {}",ioException.getStackTrace(),ioException);
			createNodebuilBuilder.put(MigratorConstants.KEY_NODE_REF, StringUtils.EMPTY);
			createNodebuilBuilder.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			createNodebuilBuilder.put(MigratorConstants.KEY_STATUS_CODE, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			createNodebuilBuilder.put(MigratorConstants.KEY_STATUS_MESSAGE, ioException.getMessage());
			createNodebuilBuilder.put(MigratorConstants.KEY_PROPERTIES_STATUS,  HttpStatus.SC_INTERNAL_SERVER_ERROR);
			createNodebuilBuilder.put(MigratorConstants.KEY_PROPERTIES_MESSAGE, ioException.getMessage());
		}
		finally {
			IOUtils.closeQuietly(response);
			IOUtils.closeQuietly(httpClient);
//			IOUtils.closeQuietly(inputStream);
			try {
				inputStream.close();
			} catch (IOException ioException2) {
				LOGGER.error("ioException occured while executing method createNode {}",ioException2);
			}
		}
		return createNodebuilBuilder;
	}
	
	/**
	 * Update node properties.
	 *
	 * @param alfrescoBaseURL the alfresco base URL
	 * @param nodeId the node id
	 * @param properties the properties
	 * @param userName the user name
	 * @param password the password
	 * @return the JSON object
	 */
	public JSONObject updateNodeProperties(final String alfrescoBaseURL,final String nodeId, final JSONObject properties,final String userName,final String password) {
		final JSONObject nodeIdbuBuilder = new JSONObject();
		final JSONObject headerJson = new JSONObject();
		headerJson.put(MigratorConstants.KEY_PROPERTIES, properties);
//		headerJson.put(MigratorConstants.KEY_PERMISSIONS, getPermissionJson(properties));
		if(LOGGER.isDebugEnabled()) {			
			LOGGER.debug("set header properties {}",headerJson);
		}
		final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		final String URL = String.format("%s%s", alfrescoBaseURL,MigratorConstants.NODE_URL+nodeId);
		LOGGER.debug("formed updated url "+URL);
		final HttpPut httpPut = new HttpPut(URL);
		final StringBuilder userPasswordBuilder = new StringBuilder();
		userPasswordBuilder.append(userName).append(":").append(password);
		final byte[] encodedAuth = Base64.encodeBase64(userPasswordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
		final String authHeader = MigratorConstants.BASIC + new String(encodedAuth);
		httpPut.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		final StringEntity requestEntity = new StringEntity(
				headerJson.toString(),
				ContentType.APPLICATION_JSON);
		httpPut.setEntity(requestEntity);
		LOGGER.info("executing update properties and permissons url {}",URL);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPut);
			final HttpEntity result = response.getEntity();
			final String responseString = EntityUtils.toString(result, StandardCharsets.UTF_8);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode() || HttpStatus.SC_CREATED == response.getStatusLine().getStatusCode()) {
				final JSONObject responseJson = new JSONObject(responseString);
				final String noderef = responseJson.getJSONObject(MigratorConstants.KEY_ENTRY).getString(MigratorConstants.KEY_ID);
				nodeIdbuBuilder.put(MigratorConstants.KEY_PERSISTED_OBJECT,MigrationUtils.buildNodeRef(noderef));
				nodeIdbuBuilder.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
				nodeIdbuBuilder.put(MigratorConstants.KEY_MESSAGE, "Successfully persisted form for item [node]workspace/SpacesStore/"+nodeId);
				if(LOGGER.isInfoEnabled()) {					
					LOGGER.info("response {}",responseString);
					LOGGER.info("node id {}",nodeIdbuBuilder);
				}
			} else {
				LOGGER.error("error occured{}",responseString);
				nodeIdbuBuilder.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
				nodeIdbuBuilder.put(MigratorConstants.KEY_MESSAGE,  response.getStatusLine().getReasonPhrase());
			}
		} catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method createFolder {} {}",ioException.getStackTrace(),ioException);
			nodeIdbuBuilder.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			nodeIdbuBuilder.put(MigratorConstants.KEY_MESSAGE,  ioException.getMessage());
		}finally {
			IOUtils.closeQuietly(response);
			IOUtils.closeQuietly(httpClient);
		}
		return nodeIdbuBuilder;
	}
	
	/**
	 * Delete node.
	 *
	 * @param alfrescoBaseURL the alfresco base URL
	 * @param nodeid the nodeid
	 * @param permanentDelete the permanent delete
	 * @param userName the user name
	 * @param password the password
	 * @return the JSON object
	 */
	public JSONObject deleteNode(final String alfrescoBaseURL, final String nodeid, final String permanentDelete,final String userName,final String password) {
		final JSONObject responseJson = new JSONObject();
		final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		final String URL = String.format("%s%s", alfrescoBaseURL,MigratorConstants.NODE_URL+nodeid);
		LOGGER.debug("Node Delete URL {}",URL);
		final HttpDelete httpDelete = new HttpDelete(URL);
		final StringBuilder userPasswordBuilder = new StringBuilder();
		userPasswordBuilder.append(userName).append(":").append(password);
		final byte[] encodedAuth = Base64.encodeBase64(userPasswordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
		final String authHeader = MigratorConstants.BASIC + new String(encodedAuth);
		httpDelete.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpDelete);
			final HttpEntity result = response.getEntity();
			LOGGER.debug("code {}",response.getStatusLine().getStatusCode());
			if(HttpStatus.SC_NO_CONTENT == response.getStatusLine().getStatusCode()) {				
				responseJson.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
				responseJson.put(MigratorConstants.KEY_MESSAGE, "Node Deleted with ID "+nodeid);
			}else {
				final String responseString = EntityUtils.toString(result, StandardCharsets.UTF_8);
				responseJson.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
				responseJson.put(MigratorConstants.KEY_MESSAGE, responseString);
			}
		} catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method createFolder {} {}",ioException.getStackTrace(),ioException);
			responseJson.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseJson.put(MigratorConstants.KEY_MESSAGE, ioException.getMessage());
		}finally {
			IOUtils.closeQuietly(response);
			IOUtils.closeQuietly(httpClient);
		}
		return responseJson;
	}
	

}
