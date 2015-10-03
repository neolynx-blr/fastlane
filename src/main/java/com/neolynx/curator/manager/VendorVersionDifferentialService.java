package com.neolynx.curator.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynx.common.model.BaseResponse;
import com.neolynx.curator.core.VendorVersionDifferential;
import com.neolynx.curator.db.VendorVersionDifferentialDAO;

/**
 * Created by nitesh.garg on Oct 3, 2015
 */
public class VendorVersionDifferentialService {
	
	static Logger LOGGER = LoggerFactory.getLogger(VendorVersionDifferentialService.class);
	
	final VendorVersionDifferentialDAO vendorVersionDiffDAO;

	public VendorVersionDifferentialService(VendorVersionDifferentialDAO vendorVersionDiffDAO) {
		super();
		this.vendorVersionDiffDAO = vendorVersionDiffDAO;
	}
	
	public BaseResponse removeAllVersionDifferentialForVendor(Long vendorId) {
		BaseResponse response = new BaseResponse();
		
		this.vendorVersionDiffDAO.deleteByVendorId(vendorId);
		response.setIsError(Boolean.FALSE);
		return response;
	}
	
	public List<VendorVersionDifferential> getVersionDiffDetailsForVendor(Long vendorId) {
		return this.vendorVersionDiffDAO.findByVendor(vendorId);
	}

}
