package com.networknt.petstore.handler;

import com.networknt.handler.HandlerProvider;
import com.networknt.health.HealthGetHandler;
import com.networknt.info.ServerInfoGetHandler;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

public class PathPetHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        return Handlers.routing()
                .add(Methods.GET, "/v1/health", new HealthGetHandler())
                .add(Methods.GET, "/v1/server/info", new ServerInfoGetHandler())
             //   .add(Methods.GET, "/v1/pets/{petId}", new PetIdGetHandler())
             //   .add(Methods.GET, "/v1/pet/all", new PetsGetHandler())
             //   .add(Methods.POST, "/v1/pet", new PetPostHandler())
            //    .add(Methods.PUT, "/v1/pets", new PetUpdateHandler())
                ;
    }
}
