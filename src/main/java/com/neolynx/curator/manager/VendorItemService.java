package com.neolynx.curator.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynx.common.model.BaseResponse;
import com.neolynx.curator.core.VendorItemHistory;
import com.neolynx.curator.core.VendorItemMaster;
import com.neolynx.curator.db.VendorItemHistoryDAO;
import com.neolynx.curator.db.VendorItemMasterDAO;

/**
 * Created by nitesh.garg on Oct 3, 2015
 */
public class VendorItemService {

	static Logger LOGGER = LoggerFactory.getLogger(VendorItemService.class);
	
	final VendorItemMasterDAO vendorItemMasterDAO;
	final VendorItemHistoryDAO vendorItemHistoryDAO;
	
	public VendorItemService(VendorItemMasterDAO vendorItemMasterDAO, VendorItemHistoryDAO vendorItemHistoryDAO) {
		super();
		this.vendorItemMasterDAO = vendorItemMasterDAO;
		this.vendorItemHistoryDAO = vendorItemHistoryDAO;
	}
	
	public List<VendorItemMaster> getAllItemRecordsForVendor(Long vendorId) {
		return this.vendorItemMasterDAO.findByVendor(vendorId);
	}
	
	/**
	 * TODO: Add transactional aspect
	 * 
	 * @param vendorId
	 * @return
	 */
	public BaseResponse removeAllItemRecordsForVendor(Long vendorId) {
		BaseResponse response = new BaseResponse();
		
		List<VendorItemMaster> itemRecords = getAllItemRecordsForVendor(vendorId);
		for(VendorItemMaster instance : itemRecords) {
			
			VendorItemHistory itemHistoryRecord = new VendorItemHistory(instance);
			this.vendorItemHistoryDAO.create(itemHistoryRecord);
			
		}

		this.vendorItemMasterDAO.deleteByVendorId(vendorId);
		
		response.setIsError(Boolean.FALSE);
		return response;
	}
	
	

}
