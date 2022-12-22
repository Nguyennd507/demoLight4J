package com.networknt.petstore.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.body.BodyHandler;
import com.networknt.client.Http2Client;
import com.networknt.cluster.Cluster;
import com.networknt.config.Config;

import com.networknt.exception.ApiException;
import com.networknt.exception.ClientException;
import com.networknt.handler.LightHttpHandler;
import com.networknt.http.HttpStatus;
import com.networknt.http.MediaType;
import com.networknt.http.ResponseEntity;
import com.networknt.petstore.domain.DTO.PetDTO;
import com.networknt.petstore.domain.Order;
import com.networknt.petstore.service.OrderService;
import com.networknt.server.Server;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import io.undertow.UndertowOptions;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.OptionMap;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
For more information on how to write business handlers, please check the link below.
https://doc.networknt.com/development/business-handler/rest/
*/
public class OrderPostHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(OrderPostHandler.class);
    private static OrderService orderService = SingletonServiceFactory.getBean(OrderService.class);
    private static final ObjectMapper objectMapper = Config.getInstance().getMapper();
    private  static final String API_ERROR = "ERR30000";
    static Cluster cluster = SingletonServiceFactory.getBean(Cluster.class);
    static String apibHost;
    static String path = "/v1/pets/inventory/update";
    static String tag = Server.getServerConfig().getEnvironment();

    static Http2Client client = Http2Client.getInstance();
    static ClientConnection connectionB;


    public OrderPostHandler() {
        try {
            apibHost = cluster.serviceToUrl("https", "pet.301", tag, null);
            connectionB = client.connect(new URI(apibHost),
                    Http2Client.WORKER, Http2Client.SSL,
                    Http2Client.BUFFER_POOL,
                    OptionMap.create(UndertowOptions.ENABLE_HTTP2, true))
                            .get();
        } catch (Exception e) {
                logger.error(e.getMessage());
        }
    }


    //generate method from openAPI  -->     operationId: "createOrder"
    protected ResponseEntity createOrder(Order order) throws ApiException {
        HeaderMap responseHeaders = new HeaderMap();
        responseHeaders.add(Headers.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<String> responseEntity ;
        try {
            orderService.addOrder(order);
            responseEntity = new ResponseEntity<>(objectMapper.writeValueAsString(order), responseHeaders, HttpStatus.CREATED);
            return responseEntity;
        } catch(Exception e) {
            logger.error("OrderService call error:" + e);
            Status status = new Status(API_ERROR);
            throw new ApiException(status);
        }
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        List<Object> list = new ArrayList<>();
        if(connectionB == null || !connectionB.isOpen()) {
            try {
                apibHost = cluster.serviceToUrl("https", "pet.301", tag, null);
                connectionB = client.connect(new URI(apibHost),
                                Http2Client.WORKER,
                                Http2Client.SSL,
                                Http2Client.BUFFER_POOL,
                                OptionMap.create(UndertowOptions.ENABLE_HTTP2, true))
                                .get();
            } catch (Exception e) {
                logger.error("Exeption:", e);
                throw new ClientException(e);
            }
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<ClientResponse> referenceB = new AtomicReference<>();

        Map<String, Object> bodyMap = (Map<String, Object>)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        Order order = Config.getInstance().getMapper().convertValue(bodyMap, Order.class);
        ResponseEntity<String> responseEntity = createOrder(order);
        responseEntity.getHeaders().forEach(values -> {
            exchange.getResponseHeaders().add(values.getHeaderName(), values.getFirst());
        });
        exchange.setStatusCode(responseEntity.getStatusCodeValue());
        exchange.getResponseSender().send(responseEntity.getBody());

        try {
            ClientRequest requestB =  new ClientRequest().setMethod(Methods.PUT)
                     .setPath(path);

            if(false) client.propagateHeaders(requestB, exchange);
            requestB.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/json");
            requestB.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
            PetDTO pet = convertToPet(order);
            connectionB.sendRequest(requestB, client.createClientCallback(referenceB, latch, objectMapper.writeValueAsString(pet)));
            latch.await();

            int statusCodeB = referenceB.get().getResponseCode();
            if(statusCodeB >= 300){
                throw new Exception("Failed to call API PetService: " + statusCodeB);
            }
            List<Object> apibList = Config.getInstance().getMapper().readValue(referenceB.get().getAttachment(Http2Client.RESPONSE_BODY),
                    new TypeReference<List<Object>>(){});
            list.addAll(apibList);
        } catch (Exception e) {
            logger.error("Exception:", e);
            throw new ClientException(e);
        }
    }

    private PetDTO convertToPet(Order order) {
        PetDTO pet = new PetDTO();
        pet.setId(order.getPetId());
        pet.setAmount(order.getQuantity());
        return pet;
    }
}
