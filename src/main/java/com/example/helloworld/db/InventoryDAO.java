package com.example.helloworld.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.example.helloworld.core.Inventory;

/**
 * Created by nitesh.garg on 25-Aug-2015
 */
public class InventoryDAO extends AbstractDAO<Inventory>{

	private SessionFactory sessionFactory; 
	
	/**
	 * @param sessionFactory
	 */
	public InventoryDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
		this.sessionFactory = sessionFactory;
	}		
	
	@SuppressWarnings("unchecked")
	public List<Inventory> getLatestInventory(Long vendorId) {
		
		List<Inventory> inventoryList = new ArrayList<Inventory>();
		
		Session session = this.sessionFactory.openSession();
		Query queryOld = session
				.createQuery("select pc.id as productId, pc.name as name, pc.barcode as barcode, pc.tagLine as tagline, pc.description as description, ic.id as itemId, ic.vendorId as vendorId, id.versionId as versionId, id.mrp as mrp, id.price as price, id.imageJSON as imageJSON"
						+ " from ItemCore ic, ItemDetail id, ProductCore pc "
						+ " where id.vendorId = :vendorId "
						+ " and id.versionId = (select max(versionId) from ItemDetail where vendorId = :vendorId) "
						+ " and ic.id = id.itemId "
						+ " and pc.id = ic.productId");
		queryOld.setLong("vendorId", vendorId);
		
		System.out.println("Old::"+queryOld.getFetchSize());
		
		
		List<Object[]> allRowsn = queryOld.list();
		System.out.println("OldGoal::"+allRowsn.size());
/*
		Query query = session
				.createSQLQuery("select {ic.*], {id.*}, {pc.*} from ItemCore ic, ItemDetail id, ProductCore pc "
						+ " where id.vendorId = :vendorId "
						+ " and id.versionId = (select max(versionId) from ItemDetail where vendorId = :vendorId) "
						+ " and ic.id = id.itemId "
						+ " and pc.id = ic.productId");
		query.setLong("vendorId", vendorId);
		
		System.out.println("New::"+query.getFetchSize());
		
		
		List<Object[]> allRows = query.list();
		System.out.println("Goal::"+allRows.size());
		*/
		
		for(Object[] row: allRowsn)
		{
			/*ProductCore pc = (ProductCore) row[2];
			ItemDetail id = (ItemDetail) row[1];
			ItemCore ic = (ItemCore) row[0];
			
			Inventory instance = new Inventory();
			
			instance.setBarcode(pc.getBarcode());
			instance.setDescription(pc.getDescription());
			instance.setImageJSON(id.getImageJSON());
			instance.setItemId(ic.getId());
			instance.setMrp(id.getMrp());
			instance.setName(pc.getName());
			instance.setPrice(id.getPrice());
			instance.setProductId(pc.getId());
			instance.setTagline(pc.getTagLine());
			instance.setVendorId(ic.getVendorId());
			instance.setVersionId(id.getVersionId());*/
		
			Inventory instance = new Inventory();

			instance.setName(row[1].toString());
			instance.setTagline(row[3].toString());
			instance.setImageJSON(row[10].toString());
			instance.setDescription(row[4].toString());
			
			instance.setItemCode(row[5].toString());
			instance.setBarcode(Long.parseLong(row[2].toString()));
			instance.setProductId(Long.parseLong(row[0].toString()));
			instance.setVersionId(Long.parseLong(row[7].toString()));
			
			instance.setMrp(Double.parseDouble(row[8].toString()));
			instance.setPrice(Double.parseDouble(row[9].toString()));
			
			instance.setVendorId(Long.parseLong(row[6].toString()));
			
			
			inventoryList.add(instance);
		}
		
		//session.close();
		return inventoryList;
	}

}
