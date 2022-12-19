package com.networknt.petstore.service;

import com.networknt.petstore.domain.Order;
import com.networknt.petstore.domain.OrderRepository;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OrderServiceImpl implements OrderService{
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static OrderRepository orderRepository = SingletonServiceFactory.getBean(OrderRepository.class);

    public OrderServiceImpl (OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    @Override
    public List<Order> getListOrder() {
        logger.info("OrderService get all pets");
        return orderRepository.findAll();
    }

    @Override
    public void deleteOrderById(Long id) {
        logger.info("OrderService delete by Id ++" + id);
        orderRepository.delete(id);
    }

    @Override
    public Order getOrderById(Long id) {
        logger.info("OrderService get by Id ++" + id);
        return  orderRepository.findOne(id);
    }

    @Override
    public void addOrder(Order order) {
        logger.info("OrderService add new Order ");
        orderRepository.save(order);
    }

}
