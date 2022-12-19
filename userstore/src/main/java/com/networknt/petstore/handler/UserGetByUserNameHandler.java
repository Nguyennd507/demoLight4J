package com.networknt.petstore.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.handler.LightHttpHandler;
import com.networknt.http.HttpStatus;
import com.networknt.http.MediaType;
import com.networknt.http.ResponseEntity;
import com.networknt.petstore.domain.User;
import com.networknt.petstore.service.UserService;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
For more information on how to write business handlers, please check the link below.
https://doc.networknt.com/development/business-handler/rest/
*/
public class UserGetByUserNameHandler implements LightHttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserGetByUserNameHandler.class);
    private static UserService userService = SingletonServiceFactory.getBean(UserService.class);
    private static final ObjectMapper objectMapper = Config.getInstance().getMapper();
    private  static final String API_ERROR = "ERR30000";

    //generate method from openAPI  -->     operationId: "showOrderByUserName"
    protected ResponseEntity getUserByUserName(String userName) throws ApiException {
        HeaderMap responseHeaders = new HeaderMap();
        responseHeaders.add(Headers.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<String> responseEntity ;
        try {
            User user = userService.getUserByUserName(userName);
            responseEntity = new ResponseEntity<>(objectMapper.writeValueAsString(user), responseHeaders, HttpStatus.OK);
            return responseEntity;
        } catch(Exception e) {
            logger.error("User Service call error:" + e);
            Status status = new Status(API_ERROR);
            throw new ApiException(status);
        }
    }
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String userName = String.valueOf(exchange.getPathParameters().get("userName").getFirst());
        ResponseEntity<String> responseEntity = getUserByUserName(userName);
        responseEntity.getHeaders().forEach(values -> {
            exchange.getResponseHeaders().add(values.getHeaderName(), values.getFirst());
        });
        exchange.setStatusCode(responseEntity.getStatusCodeValue());
        exchange.getResponseSender().send(responseEntity.getBody());
    }
}
