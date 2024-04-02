package com.migrator.services;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.migrator.constants.MigratorConstants;

@Service
public class SolrService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrService.class);
	
	private static final String FILTER_URL = "%s/alfresco/afts?facet.field=cm:description&facet=on&fl=[cached]LID,PARENT,ANCESTOR&indent=on&rows=1&wt=json&q=ANCESTOR:";
	private static final String QUERY = "\"workspace://SpacesStore/%s\" AND TYPE:\"cm:content\"";
	private static final String URL_FORMAT = "%s%s";
	
	@Value("${solr.url}")
	private String solrURL;
	
	public JSONObject getRecordsCountSolr(final String nodeId) {
		final JSONObject responseObj = new JSONObject();
		LOGGER.info("Node Id {}",nodeId);
		final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		final String query = String.format(QUERY, nodeId);
		LOGGER.info("{}",String.format(URL_FORMAT, String.format(FILTER_URL, solrURL) ,URLEncoder.encode(query, Charset.forName("UTF-8"))));
		final HttpGet httpGet = new HttpGet(String.format(URL_FORMAT, String.format(FILTER_URL, solrURL) ,URLEncoder.encode(query, Charset.forName("UTF-8"))));
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			final HttpEntity result = response.getEntity();
			final String responseString = EntityUtils.toString(result, StandardCharsets.UTF_8);
			final JSONObject responseJson = new JSONObject(responseString);
			LOGGER.info("solr response {}",response.getStatusLine().getStatusCode());
			if(HttpStatus.SC_OK==response.getStatusLine().getStatusCode()) {
				responseObj.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
				responseObj.put(MigratorConstants.KEY_MESSAGE, MigratorConstants.KEY_SUCCESS);
				final JSONArray detailsArray = new JSONArray();
				final JSONArray countArray = responseJson.getJSONObject("facet_counts").getJSONObject("facet_fields").getJSONArray(MigratorConstants.CM_DESCRIPTION);
				final List<Object> countList = new ArrayList<>();
				countList.addAll(countArray.toList());
				LOGGER.info("{}",countList);
				AtomicInteger counter = new AtomicInteger();
		        final Collection<List<Object>> partitionedList = 
		        		countList.stream().collect(Collectors.groupingBy(i -> counter.getAndIncrement() / 2)).values(); 
		        for(final List<Object> part : partitionedList) {
					final JSONObject csvJson= new JSONObject();
		        	LOGGER.info("{}",part);
		        	csvJson.put(MigratorConstants.KEY_NAME, part.get(0));
		        	csvJson.put(MigratorConstants.KEY_COUNT, part.get(1));
					detailsArray.put(csvJson);
		        }
				responseObj.put(MigratorConstants.KEY_DETAILS, detailsArray);
			}else {
				responseObj.put(MigratorConstants.KEY_STATUS, response.getStatusLine().getStatusCode());
				responseObj.put(MigratorConstants.KEY_MESSAGE, MigratorConstants.KEY_FAILED);
				responseObj.put(MigratorConstants.KEY_DETAILS, new JSONArray());
				LOGGER.error("error code {} message {}",response.getStatusLine().getStatusCode(),responseString);
			}
		}catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method getRecordsCountSolr {}",ioException);
			responseObj.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			responseObj.put(MigratorConstants.KEY_MESSAGE, MigratorConstants.KEY_FAILED);
			responseObj.put(MigratorConstants.KEY_DETAILS, new JSONArray());
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
				LOGGER.error("ioException2 occured while executing method getRecordsCountSolr {}",ioException2);
			}
		}
		return responseObj;
	}
}
