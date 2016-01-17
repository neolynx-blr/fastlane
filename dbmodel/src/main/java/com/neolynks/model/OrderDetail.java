/**
 * 
 */
package com.neolynks.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by nitesh.garg on Oct 23, 2015
 *
 */

@Data
@Entity
@Table(name = "order_detail")
@NamedQueries({
		@NamedQuery(name = "com.neolynks.curator.core.OrderDetail.findByOrderId", query = "SELECT p FROM OrderDetail p where orderId = :orderId")})
public class OrderDetail {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "status", nullable = false)
	private Integer status;

	@Column(name = "item_list")
	private String itemList;

	@Column(name = "item_list_delivery")
	private String itemListForDelivery;

	@Column(name = "delivery_address_id")
	private Integer deliverAddressId;
	
	@Column(name = "delivery_mode")
	private Integer deliveryMode;

	@Column(name = "vendor_id")
	private Long vendorId;

	@Column(name = "server_data_version_id", nullable = false)
	private Long serverDataVersionId;
	
	@Column(name = "device_data_version_id", nullable = false)
	private Long deviceDataVersionId;

	@Column(name = "net_amount", nullable = false)
	private Double netAmount;

	@Column(name = "tax_amount")
	private Double taxAmount;
	
	@Column(name = "taxable_amount")
	private Double taxableAmount;

	@Column(name = "discount_amount")
	private Double discountAmount;

	@Column(name = "created_on", nullable = false)
	private Date createdOn;
	
	@Column(name = "last_modified_on", nullable = false)
	private Date lastModifiedOn;
}
