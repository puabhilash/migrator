/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.restcontrollers;

import java.io.IOException;

import org.apache.commons.compress.utils.FileNameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tcs.constants.MigratorConstants;
import com.tcs.services.CSVService;
import com.tcs.services.ExcelService;

/**
 * The Class FileController.
 */
@RestController
public class FileController {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);
	
	/** The Constant START. */
	private static final int START = 0;
	
	/** The Constant MAX. */
	private static final int MAX = 50;
	
	/** The csv service. */
	@Autowired
	CSVService csvService;
	
	/** The excel service. */
	@Autowired
	ExcelService excelService;
	
	/**
	 * Gets the csv columns.
	 *
	 * @param filedata the filedata
	 * @return the csv columns
	 */
	@PostMapping(value = "/retrievefileheader", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getCsvColumns(@RequestParam(required = false) MultipartFile filedata) {
		final JSONObject resJson  = new JSONObject();
		LOGGER.info("executing URL {}","/retrievefileheader");
		try {
			if(null == filedata) {
				resJson.put("columns", new JSONArray());
			}else {
				final String fileName = filedata.getOriginalFilename();
				final String fileExt = FileNameUtils.getExtension(fileName);
				if(MigratorConstants.XLS.equalsIgnoreCase(fileExt) || MigratorConstants.XLSX.equalsIgnoreCase(fileExt)) {
					resJson.put(MigratorConstants.KEY_COLUMNS, excelService.getExcelHeaders(filedata));
				}else {
					resJson.put(MigratorConstants.KEY_COLUMNS, csvService.getCsvHeaders(filedata.getInputStream()));
				}
			}
		} catch (IOException ioException) {
			LOGGER.error("ioException occured while executing method getCsvColumns {}",ioException);
		}
		return resJson.toString();
	}
	
	/**
	 * Gets the csv migrated records.
	 *
	 * @param csvid the csvid
	 * @param status the status
	 * @param issuccess the issuccess
	 * @param skip the skip
	 * @param max the max
	 * @return the csv migrated records
	 */
	@GetMapping(value = {"/retrievecsvmigrateddata"})
	public String getCsvMigratedRecords(@RequestParam(required = false) Long csvid,@RequestParam(required = false) String status,@RequestParam(required = false) final Boolean issuccess,@RequestParam Integer skip,@RequestParam Integer max) {
		JSONObject responseJson = new JSONObject();
		int skipCount = START,maxItems=MAX;
		if(null!=skip) {
			skipCount = skip.intValue();
		}
		if(null!=max) {
			maxItems = max.intValue();
		}
		if(null==csvid) {
			responseJson.put(MigratorConstants.KEY_LIST, new JSONArray());
		}else {
			final boolean isSuccessValue = (null==issuccess)?false:issuccess.booleanValue();
			responseJson=csvService.getCsvMigratedData(csvid,status,isSuccessValue,skipCount,maxItems);
		}
		return responseJson.toString();
	}

}
