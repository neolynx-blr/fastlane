package com.neolynks.curator.manager;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynks.curator.core.ProductMaster;
import com.neolynks.curator.core.ProductVendorMap;
import com.neolynks.curator.db.ProductMasterDAO;
import com.neolynks.common.model.BaseResponse;

/**
 * Created by nitesh.garg on Oct 3, 2015
 */
public class ProductMasterService {

	final ProductMasterDAO productMasterDAO;
	final SessionFactory sessionFactory;

	static Logger LOGGER = LoggerFactory.getLogger(ProductMasterService.class);

	public ProductMasterService(SessionFactory sessionFactory, ProductMasterDAO productMasterDAO) {
		super();
		this.sessionFactory = sessionFactory;
		this.productMasterDAO = productMasterDAO;
	}

	/**
	 * Get list of product details which are associated with the given vendor.
	 * 
	 * @param vendorId
	 * @return
	 */
	public List<ProductMaster> getProductListForVendor(Long vendorId) {
		int exclusiveCount = 0;
		List<ProductMaster> productMasterList = this.productMasterDAO.findByVendor(vendorId);
		for (ProductMaster instance : productMasterList) {
			if (instance.getVendorId().trim().equalsIgnoreCase(String.valueOf(vendorId))) {
				exclusiveCount++;
			}
		}
		LOGGER.debug("Found [{}] products related to vendor [{}] of which [{}] are exclusive for this vendor.",
				productMasterList.size(), vendorId, exclusiveCount);

		return productMasterList;
	}

	/**
	 * Remove all product details which are exclusively associated with a given
	 * vendor.
	 * 
	 * @param vendorId
	 * @return
	 */
	public BaseResponse removeExclusiveProductsToVendor(Long vendorId) {
		BaseResponse response = new BaseResponse();
		this.productMasterDAO.deleteExclusiveToVendor(vendorId);
		return response;
	}

	/**
	 * Remove the given vendor from all product master entries, including
	 * complete removal of product entries only associated with given vendor.
	 * 
	 * @param vendorId
	 * @return
	 */
	public BaseResponse removeVendorFromInventory(Long vendorId) {

		BaseResponse response = new BaseResponse();

		/**
		 * Remove the exclusive production listings for this vendor first
		 */
		removeExclusiveProductsToVendor(vendorId);

		Session session = this.sessionFactory.openSession();

		List<ProductMaster> productMasterList = getProductListForVendor(vendorId);
		for (ProductMaster instance : productMasterList) {

			if (instance.getVendorId().trim().contains(String.valueOf(vendorId))) {

				ProductVendorMap pvMap = new ProductVendorMap(instance);
				pvMap.removeVendor(vendorId);

				instance.setVendorId(pvMap.getVendorsAsStringList());
				session.saveOrUpdate(instance);
				session.merge(instance);

			} else {
				LOGGER.debug(
						"This should never be reached as the query should have ruled out all product master entries, and exclusive products are also already removed for vendor [{}]",
						vendorId);
			}

		}

		session.close();
		return response;

	}

}
