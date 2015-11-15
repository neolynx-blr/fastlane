package com.neolynx.curator.manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.client.InventoryInfo;
import com.neolynx.common.model.client.ItemInfo;
import com.neolynx.common.model.client.ProductInfo;
import com.neolynx.common.model.client.price.ItemPrice;
import com.neolynx.curator.util.Constants;
import com.neolynx.curator.util.EanDataUtil;

public class InventoryEvaluator {

	static Logger LOGGER = LoggerFactory.getLogger(InventoryEvaluator.class);
	private final LoadingCache<String, InventoryInfo> differentialInventoryCache;
	private final LoadingCache<String, InventoryInfo> recentItemCache;
	private final LoadingCache<Long, String> currentInventoryCache;

	public InventoryEvaluator(LoadingCache<String, InventoryInfo> differentialInventoryCache,
			LoadingCache<String, InventoryInfo> recentItemCache, LoadingCache<Long, String> currentInventoryCache) {
		super();
		this.differentialInventoryCache = differentialInventoryCache;
		this.recentItemCache = recentItemCache;
		this.currentInventoryCache = currentInventoryCache;
	}

	// Simply pull the data from the cache
	public InventoryInfo getInventoryDifferential(Long vendorId, Long dataVersionId) {

		InventoryInfo inventoryResponse = null;
		LOGGER.debug("Request received for inventory differential for vendor-version [{}-{}]", vendorId, dataVersionId);

		if (vendorId == null || dataVersionId == null) {
			LOGGER.debug("Invalid request received for missing vendor and/or version id.");

			inventoryResponse = new InventoryInfo();
			inventoryResponse.setIsError(Boolean.TRUE);
			inventoryResponse.setVendorId(vendorId);
			inventoryResponse.setCurrentDataVersionId(dataVersionId);

		} else {

			inventoryResponse = this.differentialInventoryCache.getIfPresent(vendorId + "-" + dataVersionId);
			if (inventoryResponse == null) {
				LOGGER.debug(
						"Unable to get anydata from the cache for vendor-version [{}-{}], will instead pull latest inventory.",
						vendorId, dataVersionId);
				inventoryResponse = getLatestInventory(vendorId);
			}
		}

		return inventoryResponse;
	}

	public InventoryInfo getLatestInventory(Long vendorId) {

		InventoryInfo inventoryResponse = null;

		if (vendorId == null) {
			LOGGER.debug("Invalid request received for NULL vendor id.");
			inventoryResponse = new InventoryInfo();
			inventoryResponse.setIsError(Boolean.TRUE);
			inventoryResponse.setVendorId(vendorId);

		} else {
			ObjectMapper mapper = new ObjectMapper();
			String latestInventory = this.currentInventoryCache.getIfPresent(vendorId);

			try {
				inventoryResponse = mapper.readValue(latestInventory, InventoryInfo.class);
			} catch (Exception e) {
				LOGGER.error(
						"Unable to deserialize and return latest inventory for vendor [{}] with error message [{}]",
						vendorId, e.getMessage());

				e.printStackTrace();

				inventoryResponse = new InventoryInfo();
				inventoryResponse.setIsError(Boolean.TRUE);
				inventoryResponse.setVendorId(vendorId);

			}

			LOGGER.debug(
					"The latest version found for vendor [{}] is [{}], returning [{}] Added and [{}] Updated items.",
					vendorId, inventoryResponse.getNewDataVersionId(), inventoryResponse.getAddedItems().size(),
					inventoryResponse.getUpdatedItems().size());
		}

		return inventoryResponse;
	}

	public InventoryInfo getLatestItemForVendorBarcode(Long vendorId, Long barcode) {

		InventoryInfo inventoryResponse = null;

		if (vendorId == null || barcode == null) {
			LOGGER.debug("Invalid request received for NULL vendor id or NULL barcode.");
			inventoryResponse = new InventoryInfo();
			inventoryResponse.setIsError(Boolean.TRUE);
			inventoryResponse.setVendorId(vendorId);

		} else {
			inventoryResponse = this.recentItemCache.getIfPresent(vendorId + Constants.CACHE_KEY_SEPARATOR_STRING
					+ barcode);
			LOGGER.debug("The latest for item with vendor [{}], barcode [{}]  is found and being returned.", vendorId,
					barcode);
			
			if(inventoryResponse == null) {
				
				HttpClient httpClient = HttpClientBuilder.create().build();
			    try {
			      HttpGet httpGetRequest = new HttpGet("http://eandata.com/feed/?v=3&keycode=0A05AF1556489B10&mode=json&find="+barcode+"&get=any");
			      HttpResponse httpResponse = httpClient.execute(httpGetRequest);
			 
			      System.out.println("----------------------------------------");
			      System.out.println(httpResponse.getStatusLine());
			      System.out.println("----------------------------------------");
			 
			      HttpEntity entity = httpResponse.getEntity();
			 /*
			      byte[] buffer = new byte[1024];
			      if (entity != null) {
			        InputStream inputStream = entity.getContent();
			        try {
			          int bytesRead = 0;
			          BufferedInputStream bis = new BufferedInputStream(inputStream);
			          while ((bytesRead = bis.read(buffer)) != -1) {
			            String chunk = new String(buffer, 0, bytesRead);
			            System.out.println(chunk);
			          }
			        } catch (Exception e) {
			          e.printStackTrace();
			        } finally {
			          try { inputStream.close(); } catch (Exception ignore) {}
			        }
			      }
			    */  
			      
			      String responseText = null;
					if (entity != null) {

						BufferedReader br = new BufferedReader(new InputStreamReader((entity.getContent())));

						StringBuilder response = new StringBuilder();
						String output;

						while ((output = br.readLine()) != null) {
							response.append(output);
						}

						responseText = response.toString();
						System.out.println("Data response from server is [" + response.toString() + "]");
					}

					// Check for HTTP response code: 200 = success
					if (httpResponse.getStatusLine().getStatusCode() != 200) {
						System.out.println("Non 200 Error Code\n" + httpResponse.toString() + "\n"
								+ httpResponse.getStatusLine().toString());
						throw new RuntimeException("Failed : HTTP error code : " + httpResponse.getStatusLine());
					} else {
						
						inventoryResponse = new InventoryInfo();
						ItemInfo itemInfo = new ItemInfo();
						ItemPrice itemPrice = new ItemPrice();
						ProductInfo productInfo = new ProductInfo();
						
						productInfo.setName(EanDataUtil.getAttributeValue(responseText, "product"));
						productInfo.setBenefits(EanDataUtil.getAttributeValue(responseText, "features"));
						productInfo.setDescription(EanDataUtil.getAttributeValue(responseText, "long_desc"));
						productInfo.setImageJSON(EanDataUtil.getAttributeValue(responseText, "image"));
						itemInfo.setProductInfo(productInfo);
						
						itemPrice.setMrp(Double.valueOf(EanDataUtil.getAttributeValue(responseText, "price_new"))*65);
						itemInfo.setItemPrice(itemPrice);
						
						itemInfo.setBarcode(barcode.toString());
						
						inventoryResponse.getAddedItems().put("Unknown", itemInfo);
						
					}
			      
			    } catch (Exception e) {
			      e.printStackTrace();
			    } finally {
			      //httpClient.getConnectionManager().shutdown();
			    }
				
				
			}
		}

		return inventoryResponse;
	}

}
