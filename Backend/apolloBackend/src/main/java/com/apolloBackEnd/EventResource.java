package com.apolloBackEnd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Event;
import com.model.EventBuilder;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.spotify.apollo.route.*;
import com.store.EventStore;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import okio.ByteString;

import java.io.IOException;
import java.util.stream.Stream;


/**
 * EventResource -
 */
public class EventResource implements RouteProvider {


    /* fields */
    private final EventStore store;             /* the event store instance used in the EventResource class */
    private final ObjectMapper object_mapper;   /* used in the middleware for altering response formats     */


    /* methods */
    /**
     * Constructor for the Event Resource. Initialized the object mapper and the event store fields.
     *
     * @param objectMapper The object mapper object used for altering the format of the route responses.
     */
    public EventResource(ObjectMapper objectMapper) {
        this.object_mapper = objectMapper;
        Config tmp_config = ConfigFactory.parseResources("apolloBackend.conf").resolve();
        this.store = new EventStore(tmp_config);
    }


    @Override
    public Stream<Route<AsyncHandler<Response<ByteString>>>> routes() {
        return Stream.of(
                Route.sync("GET", "/event/<id>", ctx -> String.format("%s\n", ctx.pathArgs().get("id")))
                        .withMiddleware(jsonMiddleware())


                /* TODO: notifyAttendies(), notifyUser() */
        );
    }


    /**
     * eventExists - Checks whether an event with the given name exists.
     *
     * @param event_id The name of the event to check for.
     *
     * @return boolean - true if it exists, else false.
     */
    public boolean eventExists(String event_id) {
        Event event = store.getEvent(event_id);

        if (event != null)
            return true;
        else
            return false;
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
