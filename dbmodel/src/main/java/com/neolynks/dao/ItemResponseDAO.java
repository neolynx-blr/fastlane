package com.neolynks.dao;

import com.neolynks.model.VendorItemMaster;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

/**
 * Created by nitesh.garg on 25-Aug-2015
 */
public class ItemResponseDAO extends AbstractDAO<VendorItemMaster>{

	private SessionFactory sessionFactory; 
	
	/**
	 * @param sessionFactory
	 */
	public ItemResponseDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
		this.sessionFactory = sessionFactory;
	}
	
//	@SuppressWarnings("unchecked")
//	public List<ItemResponse> getLatestInventoryFast(Long vendorId) {
//
//		List<ItemResponse> inventoryList = new ArrayList<ItemResponse>();
//		Session session = this.sessionFactory.openSession();
//
//		Query query = session
//				.createSQLQuery(
//						"select vim.* "
//								+ " from vendor_item_master vim where vim.vendor_id = :vendorId ")
//				.addEntity("vim", VendorItemMaster.class)
//				.setLong("vendorId", vendorId);
//
//		List<VendorItemMaster> vendorItemMasterList = query.list();
//		System.out.println(vendorItemMasterList.size() + " rows found after executing query:" + query.getQueryString());
//
//		for(VendorItemMaster vimInstance : vendorItemMasterList)
//		{
//
//			ItemResponse irInstance = new ItemResponse();
//			irInstance.setName(vimInstance.getName());
//			irInstance.setTagline(vimInstance.getTagLine());
//			irInstance.setImageJSON(vimInstance.getImageJSON());
//			irInstance.setDescription(vimInstance.getDescription());
//
//			irInstance.setItemCode(vimInstance.getItemCode());
//			irInstance.setBarcode(vimInstance.getBarcode());
//			irInstance.setProductId(vimInstance.getProductId());
//			irInstance.setVersionId(vimInstance.getVersionId());
//
//			irInstance.setMrp(vimInstance.getMrp());
//			irInstance.setSellingPrice(vimInstance.getSellingPrice());
//
//			inventoryList.add(irInstance);
//		}
//
//		session.close();
//		return inventoryList;
//	}
//
//	@SuppressWarnings("unchecked")
//	public List<ItemResponse> getLatestInventory(Long vendorId) {
//
//		List<ItemResponse> inventoryList = new ArrayList<ItemResponse>();
//		Session session = this.sessionFactory.openSession();
//
//		Query query = session
//				.createSQLQuery(
//						"select {vim.*}, {pm.*} "
//								+ " from vendor_item_master vim join product_master pm on vim.product_id = pm.id "
//								+ " where vim.vendor_id = :vendorId ")
//				.addEntity("vim", VendorItemMaster.class)
//				.addEntity("pm", ProductMaster.class)
//				.setLong("vendorId", vendorId);
//
//		List<Object[]> allRowsn = query.list();
//		System.out.println(allRowsn.size() + " rows found after executing query:" + query.getQueryString());
//
//		for(Object[] row: allRowsn)
//		{
//			VendorItemMaster vimData = (VendorItemMaster) row[0];
//			ProductMaster pmData = (ProductMaster) row[1];
//
//			ItemResponse instance = new ItemResponse();
//			instance.setName(pmData.getName());
//			instance.setTagline(pmData.getTagLine());
//			instance.setImageJSON(pmData.getImageJSON());
//			instance.setDescription(pmData.getDescription());
//
//			instance.setItemCode(vimData.getItemCode());
//			instance.setBarcode(pmData.getBarcode());
//			instance.setProductId(pmData.getId());
//			instance.setVersionId(vimData.getVersionId());
//
//			instance.setMrp(vimData.getMrp());
//			instance.setSellingPrice(vimData.getSellingPrice());
//
//			inventoryList.add(instance);
//		}
//
//		session.close();
//		return inventoryList;
//	}

}
