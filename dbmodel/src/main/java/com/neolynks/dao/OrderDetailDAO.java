/**
 * 
 */
package com.neolynks.dao;


import com.neolynks.model.OrderDetail;
import io.dropwizard.hibernate.AbstractDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.SessionFactory;

import java.util.List;

/**
 * Created by nitesh.garg on Oct 23, 2015
 *
 */
@Slf4j
public class OrderDetailDAO extends AbstractDAO<OrderDetail> {

	/**
	 * @param sessionFactory
	 */
	public OrderDetailDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public OrderDetail create(OrderDetail orderDetail) {
		return persist(orderDetail);
	}

	public OrderDetail update(OrderDetail orderDetail) {
		currentSession().merge(orderDetail);
		return (OrderDetail) currentSession().save(orderDetail);
	}

	public OrderDetail findOrderById(String orderId) {

		List<OrderDetail> orderDetails = list(namedQuery(
				"com.neolynks.model.OrderDetail.findByOrderId")
				.setParameter("orderId", orderId));

		if (CollectionUtils.isEmpty(orderDetails)) {
			log.info("No order was found in the database for id [{}]",
					orderId);
			return null;
		} else if (orderDetails.size() != 1) {
			log.error(
					"Something is horribly wrong as multiple orders found in DB for order-id [{}]",
					orderId);
		}

		return orderDetails.get(0);

	}

}
