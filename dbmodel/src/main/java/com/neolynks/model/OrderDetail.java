/**
 * 
 */
package com.neolynks.model;

import lombok.Data;
import lombok.experimental.Builder;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by nitesh.garg on Oct 23, 2015
 *
 */

@Data
@Entity
@Builder
@Table(name = "order_detail")
@NamedQueries({
		@NamedQuery(name = "com.neolynks.model.OrderDetail.findByOrderId", query = "SELECT p FROM OrderDetail p where orderId = :orderId")})
public class OrderDetail {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "order_id", nullable = false, updatable = false)
	private String orderId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "vendor_id", nullable = false, updatable = false)
    private Long vendorId;

    @Column(name = "status", nullable = false)
	private Integer status;

	@Column(name = "item_list")
	private String itemList;

	@Column(name = "version_id", nullable = false)
	private Integer versionId;

	@Column(name = "net_amount", nullable = false)
	private Double netAmount;

	@Column(name = "tax_amount")
	private Double taxAmount;
	
	@Column(name = "taxable_amount")
	private Double taxableAmount;

	@Column(name = "discount_amount")
	private Double discountAmount;

	@Column(name = "created_on", nullable = false, updatable = false)
	private Date createdOn;
	
	@Column(name = "last_modified_on", nullable = false)
	private Date lastModifiedOn;
}
