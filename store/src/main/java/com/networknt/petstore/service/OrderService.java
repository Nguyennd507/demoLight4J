package com.networknt.petstore.service;

import com.networknt.petstore.domain.Order;

import java.util.List;

public interface OrderService {
    List<Order> getListOrder();

    void deleteOrderById(Long id);

    Order getOrderById(Long id);

    void addOrder(Order order);


}

