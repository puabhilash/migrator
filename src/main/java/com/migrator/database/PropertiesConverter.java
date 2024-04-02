/**
 * @author Pulluri.Abhilash
 * */
package com.migrator.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class PropertiesConverter.
 */
public class PropertiesConverter implements AttributeConverter<Map<String, Object>, String> {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConverter.class);

	/**
	 * Convert to database column.
	 *
	 * @param propertiesMap the properties map
	 * @return the string
	 */
	@Override
	public String convertToDatabaseColumn(Map<String, Object> propertiesMap) {
		String propertiesJson = null;
        try {
        	ObjectMapper objectMapper = new ObjectMapper();
        	propertiesJson = objectMapper.writeValueAsString(propertiesMap);
        } catch (final JsonProcessingException e) {
            LOGGER.error("JSON writing error", e);
        }

        return propertiesJson;
	}

	/**
	 * Convert to entity attribute.
	 *
	 * @param propertiesJson the properties json
	 * @return the map
	 */
	@Override
	public Map<String, Object> convertToEntityAttribute(String propertiesJson) {
		Map<String, Object> propertiesMap = null;
        try {
        	ObjectMapper objectMapper = new ObjectMapper();
        	propertiesMap = objectMapper.readValue(propertiesJson, 
            	new TypeReference<HashMap<String, Object>>() {});
        } catch (final IOException e) {
        	LOGGER.error("JSON reading error", e);
        }

        return propertiesMap;
	}

}
