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
@Builder
@Entity
@Table(name = "under_processing_order")
@NamedQueries({
		@NamedQuery(name = "com.neolynks.model.UnderProcessingOrder.findByOrderId", query = "SELECT p FROM UnderProcessingOrder p where orderId = :orderId")})
public class UnderProcessingOrder {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "order_id", nullable = false, updatable = false)
	private String orderId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "vendor_id", nullable = false, updatable = false)
    private Long vendorId;

	@Column(name = "item_list")
	private String itemList;

	@Column(name = "version_id", nullable = false)
	private int versionId;

	@Column(name = "created_on", nullable = false, updatable = false)
	private Date createdOn;
	
	@Column(name = "last_modified_on", nullable = false)
	private Date lastModifiedOn;
}
