package com.neolynx.curator.manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.amazon.webservices.awsecommerceservice._2013_08_01.ItemLookupResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.LoadingCache;
import com.neolynx.common.model.client.InventoryInfo;
import com.neolynx.common.model.client.ItemInfo;
import com.neolynx.common.model.client.ProductInfo;
import com.neolynx.common.model.client.price.ItemPrice;
import com.neolynx.curator.util.Constants;
import com.neolynx.curator.util.EanDataUtil;
import com.neolynx.curator.util.aws.SignedRequestsHelper;

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

	public InventoryInfo getLatestItemForVendorBarcode(Long vendorId, String barcode) {

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

			if (inventoryResponse == null && vendorId.compareTo(Constants.AMAZON_VENDOR_ID) == 0) {
				
				
				SignedRequestsHelper helper;
				try {
					helper = SignedRequestsHelper.getInstance(Constants.ENDPOINT, Constants.AWS_ACCESS_KEY_ID,
							Constants.AWS_SECRET_KEY);
				} catch (Exception e) {
					LOGGER.error(
							"Received exception [{}] with message [{}] while trying to get an instance of SignedRequestHelper",
							e.getClass().getName(), e.getMessage());
					e.printStackTrace();
					return inventoryResponse;
				}

				Map<String, String> params = new HashMap<String, String>();

				params.put("ItemId", barcode);
				params.put("Version", Constants.AMAZON_API_VERSION);
				params.put("IdType", Constants.getBarcodeType(barcode));
				params.put("Service", Constants.AMAZON_API_SERVICE_NAME);
				params.put("SearchIndex", Constants.AMAZON_API_SEARCH_INDEX);
				params.put("AssociateTag", Constants.AMAZON_API_ASSOCIATE_TAG);
				params.put("Operation", Constants.AMAZON_API_LOOKUP_OPERATION);
				params.put("ResponseGroup", Constants.AMAZON_API_RESPONSE_GROUP);
				params.put("ContentType", "application/json");

				String requestUrl = helper.sign(params);
				LOGGER.debug("Signed request for barcode [{}] is [{}]", barcode, requestUrl);
				System.out.println("Signed Request is \"" + requestUrl + "\"");
				
				if(Boolean.TRUE)
					return fetchItemDetail(requestUrl);

				HttpClient httpClient = HttpClientBuilder.create().build();
				try {
					HttpGet httpGetRequest = new HttpGet(
							"http://eandata.com/feed/?v=3&keycode=0A05AF1556489B10&mode=json&find=" + barcode
									+ "&get=any");
					HttpResponse httpResponse = httpClient.execute(httpGetRequest);

					System.out.println("----------------------------------------");
					System.out.println(httpResponse.getStatusLine());
					System.out.println("----------------------------------------");

					HttpEntity entity = httpResponse.getEntity();
					/*
					 * byte[] buffer = new byte[1024]; if (entity != null) {
					 * InputStream inputStream = entity.getContent(); try { int
					 * bytesRead = 0; BufferedInputStream bis = new
					 * BufferedInputStream(inputStream); while ((bytesRead =
					 * bis.read(buffer)) != -1) { String chunk = new
					 * String(buffer, 0, bytesRead); System.out.println(chunk);
					 * } } catch (Exception e) { e.printStackTrace(); } finally
					 * { try { inputStream.close(); } catch (Exception ignore)
					 * {} } }
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

						itemPrice.setMrp(Double.valueOf(EanDataUtil.getAttributeValue(responseText, "price_new")) * 65);
						itemInfo.setItemPrice(itemPrice);

						itemInfo.setBarcode(barcode.toString());

						inventoryResponse.getAddedItems().put("Unknown", itemInfo);

					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					// httpClient.getConnectionManager().shutdown();
				}

			}
		}

		return inventoryResponse;
	}

	public InventoryInfo checkForBarcodeOnAmazon(String barcode) {

		InventoryInfo itemInfo = new InventoryInfo();

		SignedRequestsHelper helper;
		try {
			helper = SignedRequestsHelper.getInstance(Constants.ENDPOINT, Constants.AWS_ACCESS_KEY_ID,
					Constants.AWS_SECRET_KEY);
		} catch (Exception e) {
			LOGGER.error(
					"Received exception [{}] with message [{}] while trying to get an instance of SignedRequestHelper",
					e.getClass().getName(), e.getMessage());
			e.printStackTrace();
			return itemInfo;
		}

		Map<String, String> params = new HashMap<String, String>();

		params.put("ItemId", barcode);
		params.put("Version", Constants.AMAZON_API_VERSION);
		params.put("IdType", Constants.getBarcodeType(barcode));
		params.put("Service", Constants.AMAZON_API_SERVICE_NAME);
		params.put("SearchIndex", Constants.AMAZON_API_SEARCH_INDEX);
		params.put("AssociateTag", Constants.AMAZON_API_ASSOCIATE_TAG);
		params.put("Operation", Constants.AMAZON_API_LOOKUP_OPERATION);
		params.put("ResponseGroup", Constants.AMAZON_API_RESPONSE_GROUP);
		params.put("ContentType", "application/json");

		String requestUrl = helper.sign(params);
		LOGGER.debug("Signed request for barcode [{}] is [{}]", barcode, requestUrl);
		System.out.println("Signed Request is \"" + requestUrl + "\"");

		try {
			itemInfo = fetchItemDetail(requestUrl);
		} catch (Exception e) {
			LOGGER.error(
					"Received exception [{}] with message [{}] while trying to retrieve data for barcode [{}] from Amazon",
					e.getClass().getName(), e.getMessage());
			e.printStackTrace();
		}

		return itemInfo;
	}

	/*
	 * Utility function to fetch the response from the service and extract the
	 * title from the XML.
	 */
	private static ItemInfo fetchTitle(String requestUrl) {

		ItemInfo itemInfo = new ItemInfo();

		String title = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(requestUrl);
			if (doc == null) {
				System.out.println("DOC itself is null.");
			} else {
				System.out.println("DOC itself is not null.");
			}
			System.out.println("!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()");
			System.out.println("!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()");
			System.out.println(doc.getTextContent());
			System.out.println(doc.getFirstChild().getTextContent());
			System.out.println("!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()");
			System.out.println("!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()!@#$%^&*()");
			Node titleNode = doc.getElementsByTagName("Title").item(0);
			title = titleNode.getTextContent();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return itemInfo;
	}

	private static InventoryInfo fetchItemDetail(String requestURL) {

		InventoryInfo inventoryResponse = null;

		HttpClient httpClient = HttpClientBuilder.create().build();
		try {

			HttpGet httpGetRequest = new HttpGet(requestURL);
			HttpResponse httpResponse = httpClient.execute(httpGetRequest);

			System.out.println("----------------------------------------");
			System.out.println(httpResponse.getStatusLine());
			System.out.println("----------------------------------------");

			HttpEntity entity = httpResponse.getEntity();

			String responseText = null;
			if (entity != null) {

				/*
				BufferedReader br = new BufferedReader(new InputStreamReader((entity.getContent())));

				StringBuilder response = new StringBuilder();
				String output;

				while ((output = br.readLine()) != null) {
					response.append(output);
				}

				responseText = response.toString();
				System.out.println("Data response from server is [" + response.toString() + "]");
				*/
				
				try {
		            JAXBContext context = JAXBContext.newInstance(ItemLookupResponse.class);
		            Unmarshaller un = context.createUnmarshaller();
		            ItemLookupResponse emp = (ItemLookupResponse) un.unmarshal(new InputStreamReader(entity.getContent()));
		            
		            System.out.println("----------------------------------------");
		            System.out.println("----------------------------------------");
		            System.out.println("----------------------------------------");
		            System.out.println("----------------------------------------");
		            System.out.println("----------------------------------------");
		            System.out.println("----------------------------------------");
		            
		            System.out.println(emp.getItems().get(0).getItem().get(0).getItemAttributes().getEAN());
		            
		            System.out.println("----------------------------------------");
		            System.out.println("----------------------------------------");
		            System.out.println("----------------------------------------");
		            System.out.println("----------------------------------------");
		            System.out.println("----------------------------------------");
		            System.out.println("----------------------------------------");
		            
		            //return emp;
		        } catch (JAXBException e) {
		            e.printStackTrace();
		        }
		        return null;
				/*
				
				  try {
			            JSONObject xmlJSONObj = XML.toJSONObject(response.toString());
			            String jsonPrettyPrintString = xmlJSONObj.toString(4);
			            System.out.println("----------------------------------------");
			            System.out.println("----------------------------------------");
			            System.out.println("----------------------------------------");
			            System.out.println("----------------------------------------");
			            System.out.println("----------------------------------------");
			            
			            System.out.println(jsonPrettyPrintString);
			        } catch (JSONException je) {
			            System.out.println(je.toString());
			        }
				*/
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

				itemPrice.setMrp(Double.valueOf(EanDataUtil.getAttributeValue(responseText, "price_new")) * 65);
				itemInfo.setItemPrice(itemPrice);

				//itemInfo.setBarcode(barcode.toString());

				inventoryResponse.getAddedItems().put("Unknown", itemInfo);

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// httpClient.getConnectionManager().shutdown();
		}
		
		return inventoryResponse;

	}

}
