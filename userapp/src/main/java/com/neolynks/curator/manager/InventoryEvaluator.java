package com.neolynks.curator.manager;

import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.neolynks.api.common.ErrorCode;
import com.neolynks.api.common.Response;
import com.neolynks.api.common.inventory.InventoryInfo;
import com.neolynks.api.common.inventory.ItemInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.webservices.awsecommerceservice._2013_08_01.EditorialReview;
import com.amazon.webservices.awsecommerceservice._2013_08_01.Item;
import com.amazon.webservices.awsecommerceservice._2013_08_01.ItemLookupResponse;
import com.amazon.webservices.awsecommerceservice._2013_08_01.Items;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.LoadingCache;
import com.neolynks.curator.core.InventoryMaster;
import com.neolynks.curator.core.VendorItemMaster;
import com.neolynks.curator.core.VendorItemMasterWrapper;
import com.neolynks.curator.db.InventoryMasterDAO;
import com.neolynks.curator.util.Constants;
import com.neolynks.curator.util.aws.SignedRequestsHelper;

public class InventoryEvaluator {

	static Logger LOGGER = LoggerFactory.getLogger(InventoryEvaluator.class);
	private final LoadingCache<String, InventoryInfo> differentialInventoryCache;
	private final LoadingCache<String, InventoryInfo> recentItemCache;
	private final LoadingCache<Long, String> currentInventoryCache;
	private final InventoryMasterDAO invMasterDAO;

	public InventoryEvaluator(InventoryMasterDAO invMasterDAO,
			LoadingCache<String, InventoryInfo> differentialInventoryCache,
			LoadingCache<String, InventoryInfo> recentItemCache, LoadingCache<Long, String> currentInventoryCache) {
		super();
		this.differentialInventoryCache = differentialInventoryCache;
		this.recentItemCache = recentItemCache;
		this.currentInventoryCache = currentInventoryCache;
		this.invMasterDAO = invMasterDAO;
	}

	// Simply pull the data from the cache
	public Response<InventoryInfo> getInventoryDifferential(Long vendorId, Long dataVersionId) {
        Response<InventoryInfo> response = new Response<InventoryInfo>();
		InventoryInfo inventoryInfo = null;
		LOGGER.debug("Request received for inventory differential for vendor-version [{}-{}]", vendorId, dataVersionId);

		if (vendorId == null || dataVersionId == null) {
			LOGGER.debug("Invalid request received for missing vendor and/or version id.");

            inventoryInfo = new InventoryInfo();
            response.setIsError(Boolean.TRUE);
            inventoryInfo.setVendorId(vendorId);
            inventoryInfo.setCurrentDataVersionId(dataVersionId);
            response.setData(inventoryInfo);
		} else {

            inventoryInfo = this.differentialInventoryCache.getIfPresent(vendorId + "-" + dataVersionId);
			if (inventoryInfo == null) {
				LOGGER.debug(
						"Unable to get anydata from the cache for vendor-version [{}-{}], will instead pull latest inventory.",
						vendorId, dataVersionId);
                inventoryInfo = getLatestInventory(vendorId);
			}
            response.setData(inventoryInfo);
            response.setIsError(false);
		}

		return response;
	}

	public  Response<InventoryInfo> getLatestInventory(Long vendorId) {
        Response<InventoryInfo> response = new  Response<InventoryInfo>();
		InventoryInfo inventoryResponse =  new InventoryInfo();
        inventoryResponse.setVendorId(vendorId);
		if (vendorId == null) {
			LOGGER.debug("Invalid request received for NULL vendor id.");
			inventoryResponse = new InventoryInfo();
            response.setIsError(Boolean.TRUE);

            response.setData(inventoryResponse);
		} else {
			ObjectMapper mapper = new ObjectMapper();
			String latestInventory = this.currentInventoryCache.getIfPresent(vendorId);

			try {
				inventoryResponse = mapper.readValue(latestInventory, InventoryInfo.class);
                response.setIsError(Boolean.FALSE);
			} catch (Exception e) {
				LOGGER.error(
						"Unable to deserialize and return latest inventory for vendor [{}] with error message [{}]",
						vendorId, e.getMessage());

				e.printStackTrace();

				inventoryResponse = new InventoryInfo();
                response.setIsError(Boolean.TRUE);
                response.setData(inventoryResponse);
			}

			LOGGER.debug(
					"The latest version found for vendor [{}] is [{}], returning [{}] Added and [{}] Updated items.",
					vendorId, inventoryResponse.getNewDataVersionId(), inventoryResponse.getAddedItems().size(),
					inventoryResponse.getUpdatedItems().size());
		}

		return response;
	}

	public Response<InventoryInfo> getLatestItemForVendorBarcode(Long vendorId, Long barcode) {
        Response<InventoryInfo> response = new Response<InventoryInfo>();
		InventoryInfo inventoryResponse = new InventoryInfo();
        response.setData(inventoryResponse);
        inventoryResponse.setVendorId(vendorId);

        if (vendorId == null || barcode == null) {
			LOGGER.debug("Invalid request received for NULL vendor id or NULL barcode.");
			inventoryResponse = new InventoryInfo();
            response.setIsError(Boolean.TRUE);

		} else {
			inventoryResponse = this.recentItemCache.getIfPresent(vendorId + Constants.CACHE_KEY_SEPARATOR_STRING
					+ barcode);

			if (inventoryResponse == null && vendorId.compareTo(Constants.AMAZON_VENDOR_ID) == 0) {

				LOGGER.debug("********************************************************************************************");
				LOGGER.debug(
						"The latest for item with vendor [{}], barcode [{}]  is not found and being looked at Amazon.",
						vendorId, barcode);
				LOGGER.debug("********************************************************************************************");

				InventoryMaster inventoryMaster = fetchItemDetailFromAmazon(barcode);
				if(inventoryMaster == null) {
					inventoryResponse = new InventoryInfo();
					inventoryResponse.setVendorId(Constants.AMAZON_VENDOR_ID);
                    response.setIsError(Boolean.TRUE);
                    response.getErrorDetail().add(ErrorCode.INVALID_OR_MISSING_BARCODE);
					return response;
				}
				
				this.invMasterDAO.create(inventoryMaster);

				VendorItemMaster vendorItemMaster = new VendorItemMaster();
				
				vendorItemMaster.setName(inventoryMaster.getName());
				vendorItemMaster.setBarcode(barcode);
				vendorItemMaster.setItemCode(inventoryMaster.getItemCode());

				vendorItemMaster.setTagLine(inventoryMaster.getTagLine());
				vendorItemMaster.setBenefits(inventoryMaster.getBenefits());
				vendorItemMaster.setBrandName(inventoryMaster.getBrandName());
				vendorItemMaster.setImageJSON(inventoryMaster.getImageJSON());
				vendorItemMaster.setHowToUse(inventoryMaster.getHowToUse());
				vendorItemMaster.setDescription(inventoryMaster.getDescription());

				vendorItemMaster.setVendorId(Constants.AMAZON_VENDOR_ID);
				vendorItemMaster.setVersionId(inventoryMaster.getVersionId());

				vendorItemMaster.setSellingPrice(inventoryMaster.getSellingPrice());

				vendorItemMaster.setCreatedOn(inventoryMaster.getCreatedOn());
				
				ItemInfo itemInfo = new VendorItemMasterWrapper(vendorItemMaster).generateVIMItemInfo();

				inventoryResponse = new InventoryInfo();
				inventoryResponse.setVendorId(Constants.AMAZON_VENDOR_ID);
				inventoryResponse.setNewDataVersionId(vendorItemMaster.getVersionId());
				inventoryResponse.getAddedItems().put(vendorItemMaster.getBarcode(), itemInfo);

			} else {

				LOGGER.debug("********************************************************************************************");
				LOGGER.debug("The latest for item with vendor [{}], barcode [{}]  is found and being returned.",
						vendorId, barcode);
				LOGGER.debug("********************************************************************************************");

			}

		}

		return response;
	}

	private static InventoryMaster fetchItemDetailFromAmazon(Long barcode) {

		SignedRequestsHelper helper;
		InventoryMaster inventoryMasterInstance = null;

		try {

			helper = SignedRequestsHelper.getInstance(Constants.ENDPOINT, Constants.AWS_ACCESS_KEY_ID,
					Constants.AWS_SECRET_KEY);

			Map<String, String> params = new HashMap<String, String>();

			params.put("ItemId", barcode.toString());
			params.put("Version", Constants.AMAZON_API_VERSION);
			params.put("IdType", Constants.getBarcodeType(barcode));
			params.put("Service", Constants.AMAZON_API_SERVICE_NAME);
			params.put("SearchIndex", Constants.AMAZON_API_SEARCH_INDEX);
			params.put("AssociateTag", Constants.AMAZON_API_ASSOCIATE_TAG);
			params.put("Operation", Constants.AMAZON_API_LOOKUP_OPERATION);
			params.put("ResponseGroup", Constants.AMAZON_API_RESPONSE_GROUP);

			String requestUrl = helper.sign(params);
			LOGGER.debug("Signed request for barcode [{}] is [{}]", barcode, requestUrl);
			System.out.println("Signed request for barcode [{" + barcode + "}] is [{" + requestUrl + "}]");

			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet httpGetRequest = new HttpGet(requestUrl);
			HttpResponse httpResponse = httpClient.execute(httpGetRequest);

			// Check for HTTP response code: 200 = success
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				LOGGER.error("Error code [{}] recieved while checking for barcode [{}] using request URL [{}]",
						httpResponse.getStatusLine().getStatusCode(), barcode, requestUrl);
			} else {

				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {

					JAXBContext context = JAXBContext.newInstance(ItemLookupResponse.class);
					Unmarshaller un = context.createUnmarshaller();
					ItemLookupResponse itemLookupResponse = (ItemLookupResponse) un.unmarshal(new InputStreamReader(
							entity.getContent()));

					Item itemOfInterest = null;

					Items itemsOfInterest = (itemLookupResponse == null ? null : (CollectionUtils
							.isEmpty(itemLookupResponse.getItems()) ? null : itemLookupResponse.getItems().get(0)));

					if (itemsOfInterest == null || !itemsOfInterest.getRequest().getIsValid().equalsIgnoreCase("True")) {
						LOGGER.error("Received invalid response back from Amazon for barcode [{}]", barcode);
						return null;
					}

					/**
					 * Among the multiple instances of item available, pick the
					 * one which has EAN as exact match. If you don't find any,
					 * just pick the first entry from the response.
					 */
					for (Item itemInstance : itemsOfInterest.getItem()) {
						if (itemInstance.getItemAttributes().getEAN().equals(barcode)) {
							itemOfInterest = itemInstance;
							break;
						}
					}

					if (itemOfInterest == null) {
						itemOfInterest = itemLookupResponse.getItems().get(0).getItem().get(0);
					}

					/**
					 * Once an item detail is identified, transform the returned
					 * data into inventory master, and store in the master table
					 * just like it is pushed up from vendor's store with Amazon
					 * as a vendor-id
					 */
					if (itemOfInterest != null) {
						inventoryMasterInstance = new InventoryMaster();

						inventoryMasterInstance.setBarcode(barcode);
						inventoryMasterInstance.setVendorId(Constants.AMAZON_VENDOR_ID);
						inventoryMasterInstance.setVersionId(System.currentTimeMillis());
						inventoryMasterInstance.setCreatedOn(new Date(System.currentTimeMillis()));

						String itemCode = itemOfInterest.getASIN();
						inventoryMasterInstance.setItemCode(itemCode == null ? Constants.AMAZON_ITEM_CODE : itemCode);

						try {
							String description = itemOfInterest.getEditorialReviews().getEditorialReview().get(0).getContent();
							for (EditorialReview editorialInstance : itemOfInterest.getEditorialReviews()
									.getEditorialReview()) {
								if (editorialInstance.getSource().equalsIgnoreCase("Product Description")) {
									description = editorialInstance.getContent();
								}
							}
							inventoryMasterInstance.setDescription(description);
						} catch (NullPointerException npe) {
							LOGGER.warn("NPE received while reading description from Amazon for barcode [{}]", barcode);
						}

						/**
						 * Check for any image available starting with the large
						 * size, then fall back to medium and then small
						 */
						String imageSrc = itemOfInterest.getLargeImage() == null ? null : itemOfInterest.getLargeImage().getURL();
						imageSrc = imageSrc == null ? (itemOfInterest.getMediumImage() == null ? null : itemOfInterest.getMediumImage().getURL()) : imageSrc;
						imageSrc = imageSrc == null ? (itemOfInterest.getSmallImage() == null ? null : itemOfInterest.getSmallImage().getURL()) : imageSrc;
						imageSrc = imageSrc == null ? imageSrc : "{ \"image-src\" : \"" + imageSrc + "\"";
						inventoryMasterInstance.setImageJSON(itemOfInterest.getMediumImage().getURL());
						
						inventoryMasterInstance.setName(itemOfInterest.getItemAttributes() == null ? null : itemOfInterest.getItemAttributes().getTitle());
						inventoryMasterInstance.setBrandName(itemOfInterest.getItemAttributes() == null ? null : itemOfInterest.getItemAttributes().getBrand());
						inventoryMasterInstance.setTagLine(itemOfInterest.getItemAttributes() == null ? null : (CollectionUtils.isEmpty(itemOfInterest.getItemAttributes().getFeature()) ? null : itemOfInterest.getItemAttributes().getFeature().get(0)));

						String amountStr = null;
						if(itemOfInterest.getOfferSummary() == null) {
							try {
								amountStr = itemOfInterest.getOffers().getOffer().get(0).getOfferListing().get(0).getSalePrice().getAmount().toString();
								if(amountStr == null) {
									amountStr = itemOfInterest.getOffers().getOffer().get(0).getOfferListing().get(0).getPrice().getAmount().toString();
								}
							} catch (NullPointerException npe) {
								LOGGER.warn("NPE received while reading amount from Amazon for barcode [{}]", barcode);
							}
							
						} else {
							
							if(! itemOfInterest.getOfferSummary().getTotalNew().equals(Constants.AMAZON_ITEM_UNAVAILABLE_COUNT)) {
								amountStr = itemOfInterest.getOfferSummary().getLowestNewPrice().getAmount().toString();
							} else if(! itemOfInterest.getOfferSummary().getTotalUsed().equals(Constants.AMAZON_ITEM_UNAVAILABLE_COUNT)) {
								amountStr = itemOfInterest.getOfferSummary().getLowestUsedPrice().getAmount().toString();
							} else if(! itemOfInterest.getOfferSummary().getTotalRefurbished().equals(Constants.AMAZON_ITEM_UNAVAILABLE_COUNT)) {
								amountStr = itemOfInterest.getOfferSummary().getLowestRefurbishedPrice().getAmount().toString();
							} else if(! itemOfInterest.getOfferSummary().getTotalCollectible().equals(Constants.AMAZON_ITEM_UNAVAILABLE_COUNT)) {
								amountStr = itemOfInterest.getOfferSummary().getLowestCollectiblePrice().getAmount().toString();
							}
							
						}
						
						inventoryMasterInstance.setSellingPrice(amountStr == null ? null : (Double.parseDouble(amountStr) / 100));

					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Received exception [{}] with message [{}] while trying to read barcode [{}] from Amazon", e
					.getClass().getName(), e.getMessage(), barcode);
			e.printStackTrace();
			inventoryMasterInstance = null;
		}

		return inventoryMasterInstance;

	}

}
