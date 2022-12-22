package com.networknt.petstore.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.handler.LightHttpHandler;
import com.networknt.http.HttpStatus;
import com.networknt.http.MediaType;
import com.networknt.http.ResponseEntity;
import com.networknt.petstore.domain.DTO.PetDTO;
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

public class PetUpdateInventoryHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(PetUpdateInventoryHandler.class);
    private static PetService petStoreService = SingletonServiceFactory.getBean(PetService.class);
    private static final ObjectMapper objectMapper = Config.getInstance().getMapper();
    private  static final String API_ERROR = "ERR30000";

    public PetUpdateInventoryHandler() {
    }

    //generate method from openAPI  -->     operationId: "petUpdateInventory"
    protected ResponseEntity updatePet(PetDTO pet) throws ApiException {
        HeaderMap responseHeaders = new HeaderMap();
        responseHeaders.add(Headers.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<String> responseEntity ;
        try {
            petStoreService.updatePetInventory(pet);
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
        PetDTO pet = Config.getInstance().getMapper().convertValue(bodyMap, PetDTO.class);
     //   Long petId = Long.valueOf(exchange.getPathParameters().get("petId").getFirst());
        ResponseEntity<String> responseEntity = updatePet(pet);
        responseEntity.getHeaders().forEach(values -> {
            exchange.getResponseHeaders().add(values.getHeaderName(), values.getFirst());
        });
        exchange.setStatusCode(responseEntity.getStatusCodeValue());
        exchange.getResponseSender().send(responseEntity.getBody());
    }
}
