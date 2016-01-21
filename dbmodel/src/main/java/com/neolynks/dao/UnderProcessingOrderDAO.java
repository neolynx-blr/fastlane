/**
 *
 */
package com.neolynks.dao;


import com.neolynks.model.OrderDetail;
import com.neolynks.model.UnderProcessingOrder;
import io.dropwizard.hibernate.AbstractDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.SessionFactory;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by nitesh.garg on Oct 23, 2015
 */
@Slf4j
public class UnderProcessingOrderDAO extends AbstractDAO<UnderProcessingOrder> {

    /**
     * @param sessionFactory
     */
    public UnderProcessingOrderDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public UnderProcessingOrder create(UnderProcessingOrder underProcessingOrder) {
        return persist(underProcessingOrder);
    }

    public UnderProcessingOrder update(OrderDetail orderDetail) {
        currentSession().merge(orderDetail);
        return (UnderProcessingOrder) currentSession().save(orderDetail);
    }

    public UnderProcessingOrder findOrderById(String orderId) {

        List<UnderProcessingOrder> underProcessingOrders = list(namedQuery(
                "com.neolynks.model.UnderProcessingOrder.findByOrderId")
                .setParameter("orderId", orderId));

        if (CollectionUtils.isEmpty(underProcessingOrders)) {
            log.info("No order was found in the database for id [{}]", orderId);
            return null;
        } else if (underProcessingOrders.size() != 1) {
            log.error("Something is horribly wrong as multiple orders found in DB for order-id [{}]", orderId);
        }

        return underProcessingOrders.get(0);

    }

    public UnderProcessingOrder init(String orderId, long vendorId, int versionId, String userId) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        UnderProcessingOrder underProcessingOrder = UnderProcessingOrder.builder().orderId(orderId)
                .vendorId(vendorId)
                .versionId(versionId)
                .userId(userId)
                .createdOn(timestamp)
                .lastModifiedOn(timestamp).build();
        super.persist(underProcessingOrder);
        return underProcessingOrder;
    }
}
