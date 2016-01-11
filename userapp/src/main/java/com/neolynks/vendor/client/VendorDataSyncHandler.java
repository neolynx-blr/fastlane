package com.neolynks.vendor.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neolynks.vendor.model.CurationConfig;
import com.neolynks.common.model.InventoryRequest;
import com.neolynks.common.model.ResponseAudit;

/**
 * Created by nitesh.garg on 12-Sep-2015
 */
@Slf4j
public class VendorDataSyncHandler {

	final CurationConfig curationConfig;
	
	public VendorDataSyncHandler(CurationConfig curationConfig) {
		super();
		this.curationConfig = curationConfig;
	}

	public ResponseAudit postData(InventoryRequest request) {

		ResponseAudit responseAudit = new ResponseAudit();
		// create HTTP Client
		HttpClient httpClient = HttpClientBuilder.create().build();

		// Create new getRequest with below mentioned URL
		HttpPost postRequest = new HttpPost("http://localhost:8080/curator/vendor/load/");
		String authStr = this.curationConfig.getVendorUserName() + ":" + this.curationConfig.getVendorPassword();
		String encoding = new String(Base64.encodeBase64(authStr.getBytes()));
		postRequest.setHeader("Authorization", "Basic " + encoding);

		// Add additional header to getRequest which accepts
		// application/xml data
		postRequest.addHeader("accept", "application/json");
		postRequest.setHeader("Content-type", "application/json");

		ObjectMapper mapper = new ObjectMapper();
		try {
			postRequest.setEntity(new ByteArrayEntity(mapper.writeValueAsString(request).getBytes("UTF8")));

			// Execute your request and catch response
			HttpResponse callResponse;
			callResponse = httpClient.execute(postRequest);

            // Check for HTTP response code: 200 = success
            if (callResponse.getStatusLine().getStatusCode() != 200) {
                log.info("Non 200 Error Code\n" + callResponse.toString() + "\n"
                        + callResponse.getStatusLine().toString());
                throw new RuntimeException("Failed : HTTP error code : " + callResponse.getStatusLine());
            }

			HttpEntity entity = callResponse.getEntity();

			if (entity != null) {

				BufferedReader br = new BufferedReader(new InputStreamReader((entity.getContent())));

				StringBuilder response = new StringBuilder();
				String output;

				while ((output = br.readLine()) != null) {
					response.append(output);
				}

				System.out.println("============Output:============");
				System.out.println(response.toString());

				responseAudit = mapper.readValue(response.toString(), ResponseAudit.class);
				System.out.println("Data response from server is [" + responseAudit.toString() + "]");
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
            log.error("Unable to send request", e);
        } catch(JsonParseException jpe) {
			log.error("Unable to process response JSON, very likely indicating credentials issue", jpe);
		} catch (IOException e) {
            log.error("Unable to send request", e);
		} finally {
            responseAudit.setIsError(Boolean.TRUE);
        }
		return responseAudit;

	}

}
