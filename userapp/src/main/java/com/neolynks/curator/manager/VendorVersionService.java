package com.neolynks.curator.manager;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynks.curator.core.VendorVersionDetail;
import com.neolynks.curator.db.VendorVersionDetailDAO;
import com.neolynks.common.model.BaseResponse;

/**
 * Created by nitesh.garg on Oct 3, 2015
 */
public class VendorVersionService {
	
	static Logger LOGGER = LoggerFactory.getLogger(VendorVersionService.class);
	
	final VendorVersionDetailDAO vendorVersionDetailDAO;

	public VendorVersionService(VendorVersionDetailDAO vendorVersionDetailDAO) {
		super();
		this.vendorVersionDetailDAO = vendorVersionDetailDAO;
	}
	
	public BaseResponse removeAllVersionDetailsForVendor(Long vendorId) {
		BaseResponse response = new BaseResponse();
		
		this.vendorVersionDetailDAO.deleteByVendorId(vendorId);
		response.setIsError(Boolean.FALSE);
		return response;
	}
	
	public List<VendorVersionDetail> getVersionDetailsForVendor(Long vendorId) {
		return this.vendorVersionDetailDAO.findByVendor(vendorId);
	}
	
	public BaseResponse createDefaultVendorVersionEntry(Long vendorId) {
		BaseResponse response = new BaseResponse();
		
		VendorVersionDetail newVendorVersionDetail = new VendorVersionDetail();
		newVendorVersionDetail.setVendorId(vendorId);
		newVendorVersionDetail.setLatestSyncedVersionId(0L);
		newVendorVersionDetail.setLastModifiedOn(new Date(System.currentTimeMillis()));
		
		this.vendorVersionDetailDAO.create(newVendorVersionDetail);
		response.setIsError(Boolean.FALSE);
		return response;
		
	}

}
