package com.apolloBackEnd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.apollo.Response;
import com.spotify.apollo.route.*;
import okio.ByteString;

import java.util.stream.Stream;


/**
 * EventResource -
 */
public class EventResource implements RouteProvider {


    /* fields */
    private final ObjectMapper object_mapper;


    /* methods */
    /**
     * Constructor for the Event Resource. Initialized the object mapper and the event store fields.
     *
     * @param objectMapper The object mapper object used for altering the format of the route responses.
     */
    public EventResource(ObjectMapper objectMapper) {
        this.object_mapper = objectMapper;
    }


    @Override
    public Stream<Route<AsyncHandler<Response<ByteString>>>> routes() {
        return Stream.of(
                Route.sync("GET", "/event/<id>", ctx -> String.format("%s\n", ctx.pathArgs().get("id")))
                        .withMiddleware(jsonMiddleware())
        );
    }


    /**
     * jsonMiddleware - Standard middlware function that converts a the return type of an async handler into json, or
     * more specifically a ByteString type.
     * @param <T>
     * @return
     */
    private <T> Middleware<AsyncHandler<T>, AsyncHandler<Response<ByteString>>> jsonMiddleware() {
        return JsonSerializerMiddlewares.<T>jsonSerialize(object_mapper.writer())
                .and(Middlewares::httpPayloadSemantics)
                .and(responseAsyncHandler -> requestContext ->
                        responseAsyncHandler.invoke(requestContext)
                                .thenApply(response -> response.withHeader("Access-Control-Allow-Origin", "*")));

    }

}
