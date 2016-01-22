package com.neolynks.curator.manager.cachesupport;

import com.google.common.cache.CacheLoader;
import com.neolynks.curator.dto.Order;
import com.neolynks.dao.UnderProcessingOrderDAO;
import com.neolynks.model.UnderProcessingOrder;
import io.dropwizard.hibernate.UnitOfWork;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by nishantgupta on 22/1/16.
 */
@Slf4j
@AllArgsConstructor
public class OrderCacheLoader extends CacheLoader<String, Order> {

    private final UnderProcessingOrderDAO underProcessingOrderDAO;

    @UnitOfWork
    @Override
    public Order load(String orderId) throws Exception {

        Order order = null;
        if (StringUtils.isBlank(orderId)) {
            log.debug("Recieved call to load cart with Null Id");
        } else {
            UnderProcessingOrder underProcessingOrder = underProcessingOrderDAO.findOrderById(orderId);
            log.debug("Explicit call made to load cart [{}], expectedly returning null", orderId);
        }
        return order;
    }

}