/**
 * 
 */
package com.neolynks.curator.db;

import io.dropwizard.hibernate.AbstractDAO;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neolynks.curator.core.OrderDetail;

/**
 * Created by nitesh.garg on Oct 23, 2015
 *
 */
public class OrderDetailDAO extends AbstractDAO<OrderDetail> {

	static Logger LOGGER = LoggerFactory.getLogger(OrderDetailDAO.class);

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

	public OrderDetail findOrderById(Long orderId) {

		List<OrderDetail> orderDetails = list(namedQuery(
				"com.neolynks.curator.core.OrderDetail.findByOrderId")
				.setParameter("orderId", orderId));

		if (CollectionUtils.isEmpty(orderDetails)) {
			LOGGER.info("No order was found in the database for id [{}]",
					orderId);
			return null;
		} else if (orderDetails.size() != 1) {
			LOGGER.error(
					"Something is horribly wrong as multiple orders found in DB for order-id [{}]",
					orderId);
		}

		return orderDetails.get(0);

	}

}
