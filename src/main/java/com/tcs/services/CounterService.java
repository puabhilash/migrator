/**
 * @author Pulluri.Abhilash
 * 
* */
package com.tcs.services;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.tcs.constants.MigratorConstants;

@Service
public class CounterService {
	
	int success;
	
	int failed;
	
	int total;

	public int getSuccess() {
		return success;
	}

	public void setSuccess(final int success) {
		this.success = success;
	}

	public int getFailed() {
		return failed;
	}

	public void setFailed(final int failed) {
		this.failed = failed;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(final int total) {
		this.total = total;
	}
	
	@Override
	public String toString() {
		final JSONObject stringJson = new JSONObject();
		stringJson.put(MigratorConstants.KEY_SUCCESS, success);
		stringJson.put(MigratorConstants.KEY_FAILED, failed);
		stringJson.put(MigratorConstants.KEY_TOTAL, total);
		return stringJson.toString();
	}

}
