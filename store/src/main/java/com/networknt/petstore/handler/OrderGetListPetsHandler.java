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

public class OrderGetListPetsHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(OrderGetListPetsHandler.class);
    private static OrderService orderService = SingletonServiceFactory.getBean(OrderService.class);
    private static final ObjectMapper objectMapper = Config.getInstance().getMapper();
    private  static final String API_ERROR = "ERR30000";
    static Cluster cluster = SingletonServiceFactory.getBean(Cluster.class);
    static String apibHost;
    static String path = "/v1/pets/inventory";
    static String tag = Server.getServerConfig().getEnvironment();

    static Http2Client client = Http2Client.getInstance();
    static ClientConnection connectionB;


    public OrderGetListPetsHandler() {
        try {
            apibHost = cluster.serviceToUrl("https", "com.networknt.ab-1.0.0", tag, null);
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
        //HeaderMap requestHeaders = exchange.getRequestHeaders();
        //HttpMethod httpMethod = HttpMethod.resolve(exchange.getRequestMethod().toString());
        HeaderMap responseHeaders = new HeaderMap();
        responseHeaders.add(Headers.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if(connectionB == null || !connectionB.isOpen()) {
            try {
                apibHost = cluster.serviceToUrl("https", "com.networknt.ab-1.0.0", tag, null);
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

        ResponseEntity<String> responseEntity ;

        try {
            ClientRequest requestB = new ClientRequest().setMethod(Methods.GET).setPath(path);
            if(false) client.propagateHeaders(requestB, exchange);
            connectionB.sendRequest(requestB, client.createClientCallback(referenceB, latch));

            latch.await();

            int statusCodeB = referenceB.get().getResponseCode();
            if(statusCodeB >= 300){
                throw new Exception("Failed to call API PetService: " + statusCodeB);
            }
            List<Object> apiListPet = Config.getInstance().getMapper().readValue(referenceB.get().getAttachment(Http2Client.RESPONSE_BODY),
                    new TypeReference<List<Object>>(){});

            responseEntity = new ResponseEntity<>(objectMapper.writeValueAsString(apiListPet), responseHeaders, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Exception:", e);
            throw new ClientException(e);
        }
        responseEntity.getHeaders().forEach(values -> {
            exchange.getResponseHeaders().add(values.getHeaderName(), values.getFirst());
        });
        exchange.setStatusCode(responseEntity.getStatusCodeValue());
        exchange.getResponseSender().send(responseEntity.getBody());
    }
}
