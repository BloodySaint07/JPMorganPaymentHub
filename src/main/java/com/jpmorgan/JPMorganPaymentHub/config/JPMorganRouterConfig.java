package com.jpmorgan.JPMorganPaymentHub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class JPMorganRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> routeFunction() {
        return route(RequestPredicates.GET("/resource/{id}"), this::getResource)
                .andRoute(RequestPredicates.POST("/resource"), this::createResource)
                ;
    }

    public Mono<ServerResponse> getResource(ServerRequest request) {
        String id = request.pathVariable("id");
        return Mono.error(new Exception("Resource not found with id: " + id));
    }

    public Mono<ServerResponse> createResource(ServerRequest request) {
        return Mono.error(new Exception("Invalid request to create resource"));
    }

    private RouterFunction<ServerResponse> errorRoutes() {
        return route()
                .onError(Exception.class, this::handleResourceNotFoundException)
                .onError(Exception.class, this::handleBadRequestException)
                .onError(Exception.class, this::handleGenericException)
                .build();
    }

    private Mono<ServerResponse> handleResourceNotFoundException(Throwable throwable, ServerRequest request) {
        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(throwable.getMessage());
    }

    private Mono<ServerResponse> handleBadRequestException(Throwable throwable, ServerRequest request) {
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(throwable.getMessage());
    }

    private Mono<ServerResponse> handleGenericException(Throwable throwable, ServerRequest request) {
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("An unexpected error occurred: " + throwable.getMessage());
    }
}