package com.networknt.petstore.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.handler.LightHttpHandler;
import com.networknt.http.HttpStatus;
import com.networknt.http.MediaType;
import com.networknt.http.ResponseEntity;
import com.networknt.petstore.domain.Order;
import com.networknt.petstore.service.OrderService;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
For more information on how to write business handlers, please check the link below.
https://doc.networknt.com/development/business-handler/rest/
*/
public class OrdersGetHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(OrdersGetHandler.class);
    private static OrderService orderService = SingletonServiceFactory.getBean(OrderService.class);
    private static final ObjectMapper objectMapper = Config.getInstance().getMapper();
    private  static final String API_ERROR = "ERR30000";

    public OrdersGetHandler() {
    }


    //generate method from openAPI  -->     operationId: "listOrders"
    protected ResponseEntity listOrders() throws ApiException {
        if(logger.isDebugEnabled()) logger.debug("input values:" );
        HeaderMap responseHeaders = new HeaderMap();
        responseHeaders.add(Headers.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<String> responseEntity ;
        System.out.println("Get list orders");
        try {
            List<Order> orders = orderService.getListOrder();
            responseEntity = new ResponseEntity<>(objectMapper.writeValueAsString(orders), responseHeaders, HttpStatus.OK);
            return responseEntity;
        } catch(Exception e) {
            logger.error("OrderService call error:" + e);
            Status status = new Status(API_ERROR);
            throw new ApiException(status);
        }
    }
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if(logger.isDebugEnabled()) logger.debug("handleRequest is called");
//        Integer limit = Integer.valueOf(exchange.getQueryParameters().get("limit").getFirst());
        //HeaderMap requestHeaders = exchange.getRequestHeaders();
        //HttpMethod httpMethod = HttpMethod.resolve(exchange.getRequestMethod().toString());
        ResponseEntity<String> responseEntity = listOrders();
        responseEntity.getHeaders().forEach(values -> {
            exchange.getResponseHeaders().add(values.getHeaderName(), values.getFirst());
        });
        exchange.setStatusCode(responseEntity.getStatusCodeValue());
        exchange.getResponseSender().send(responseEntity.getBody());
    }
}
