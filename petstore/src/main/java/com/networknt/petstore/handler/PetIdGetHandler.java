package com.networknt.petstore.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.config.Config;

import com.networknt.exception.ApiException;
import com.networknt.handler.LightHttpHandler;
import com.networknt.http.HttpStatus;
import com.networknt.http.MediaType;
import com.networknt.http.ResponseEntity;
import com.networknt.petstore.domain.Pet;
import com.networknt.petstore.service.PetService;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HeaderMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
For more information on how to write business handlers, please check the link below.
https://doc.networknt.com/development/business-handler/rest/
*/
public class PetIdGetHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(PetIdGetHandler.class);
    private static PetService petService = SingletonServiceFactory.getBean(PetService.class);
    private static final ObjectMapper objectMapper = Config.getInstance().getMapper();
    private  static final String API_ERROR = "ERR30000";

    //generate method from openAPI  -->     operationId: "showPetById"
    protected ResponseEntity showPetById(Long petId) throws ApiException {
        HeaderMap responseHeaders = new HeaderMap();
        responseHeaders.add(Headers.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<String> responseEntity ;
        try {
            Pet pet = petService.getPetById(petId);
            responseEntity = new ResponseEntity<>(objectMapper.writeValueAsString(pet), responseHeaders, HttpStatus.OK);
            return responseEntity;
        } catch(Exception e) {
            logger.error("Service call error:" + e);
            Status status = new Status(API_ERROR);
            throw new ApiException(status);
        }
    }
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Long petId = Long.valueOf(exchange.getPathParameters().get("petId").getFirst());
        ResponseEntity<String> responseEntity = showPetById(petId);
        responseEntity.getHeaders().forEach(values -> {
            exchange.getResponseHeaders().add(values.getHeaderName(), values.getFirst());
        });
        exchange.setStatusCode(responseEntity.getStatusCodeValue());
        exchange.getResponseSender().send(responseEntity.getBody());
    }
}
