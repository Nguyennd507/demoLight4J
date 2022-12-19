package com.networknt.petstore.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.body.BodyHandler;
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
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PetUpdateHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(PetUpdateHandler.class);
    private static PetService petStoreService = SingletonServiceFactory.getBean(PetService.class);
    private static final ObjectMapper objectMapper = Config.getInstance().getMapper();
    private  static final String API_ERROR = "ERR30000";

    public PetUpdateHandler() {
    }

    //generate method from openAPI  -->     operationId: "createPets"
    protected ResponseEntity updatePet(Long id, Pet pet) throws ApiException {
        HeaderMap responseHeaders = new HeaderMap();
        responseHeaders.add(Headers.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<String> responseEntity ;
        try {
            petStoreService.updatePet(id, pet);
            responseEntity = new ResponseEntity<>(objectMapper.writeValueAsString(pet), responseHeaders, HttpStatus.CREATED);
            return responseEntity;
        } catch(Exception e) {
            logger.error("Service call error:" + e);
            Status status = new Status(API_ERROR);
            throw new ApiException(status);
        }
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        Map<String, Object> bodyMap = (Map<String, Object>)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        Pet pet = Config.getInstance().getMapper().convertValue(bodyMap, Pet.class);
     //   Long petId = Long.valueOf(exchange.getPathParameters().get("petId").getFirst());
        ResponseEntity<String> responseEntity = updatePet(pet.getId(), pet);
        responseEntity.getHeaders().forEach(values -> {
            exchange.getResponseHeaders().add(values.getHeaderName(), values.getFirst());
        });
        exchange.setStatusCode(responseEntity.getStatusCodeValue());
        exchange.getResponseSender().send(responseEntity.getBody());
    }
}
