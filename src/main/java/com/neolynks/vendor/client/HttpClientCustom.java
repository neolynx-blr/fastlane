package com.neolynks.vendor.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neolynks.vendor.model.CurationConfig;
import com.neolynks.common.model.InventoryRequest;
import com.neolynks.common.model.ResponseAudit;

/**
 * Created by nitesh.garg on 12-Sep-2015
 */
public class HttpClientCustom {

	final CurationConfig curationConfig;
	static Logger LOGGER = LoggerFactory.getLogger(HttpClientCustom.class);

	public HttpClientCustom(CurationConfig curationConfig) {
		super();
		this.curationConfig = curationConfig;
	}

	public ResponseAudit postData(InventoryRequest request) {

		ResponseAudit responseAudit = new ResponseAudit();
		// create HTTP Client
		// TODO Create some pool and/or stay-alive/persistent connection
		HttpClient httpClient = HttpClientBuilder.create().build();

		// Create new getRequest with below mentioned URL
		HttpPost getRequest = new HttpPost("http://localhost:8080/curator/vendor/load/");
		String authStr = this.curationConfig.getVendorUserName() + ":" + this.curationConfig.getVendorPassword();
		String encoding = java.util.Base64.getEncoder().encodeToString(authStr.getBytes());
		getRequest.setHeader("Authorization", "Basic " + encoding);

		// Add additional header to getRequest which accepts
		// application/xml data
		getRequest.addHeader("accept", "application/json");
		getRequest.setHeader("Content-type", "application/json");

		ObjectMapper mapper = new ObjectMapper();
		try {
			getRequest.setEntity(new ByteArrayEntity(mapper.writeValueAsString(request).getBytes("UTF8")));

			// Execute your request and catch response
			HttpResponse callResponse;
			callResponse = httpClient.execute(getRequest);
			HttpEntity entity = callResponse.getEntity();

			if (entity != null) {

				BufferedReader br = new BufferedReader(new InputStreamReader((entity.getContent())));

				StringBuilder response = new StringBuilder();
				String output;

				// Simply iterate through XML response and show on console.
				while ((output = br.readLine()) != null) {
					response.append(output);
				}

				System.out.println("============Output:============");
				System.out.println(response.toString());

				responseAudit = mapper.readValue(response.toString(), ResponseAudit.class);
				System.out.println("Data response from server is [" + responseAudit.toString() + "]");

			}

			// Check for HTTP response code: 200 = success
			if (callResponse.getStatusLine().getStatusCode() != 200) {
				System.out.println("Non 200 Error Code\n" + callResponse.toString() + "\n"
						+ callResponse.getStatusLine().toString());
				throw new RuntimeException("Failed : HTTP error code : " + callResponse.getStatusLine());
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(JsonParseException jpe) {
			LOGGER.error("Unable to process response JSON, very likely indicating credentials issue");
			responseAudit.setIsError(Boolean.TRUE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		     // httpClient.getConnectionManager().shutdown();
		    }

		return responseAudit;

	}

}
