package com.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.model.Event;
import com.model.User;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.spotify.apollo.Status;
import com.spotify.apollo.route.*;
import com.store.EventStore;
import okio.ByteString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


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
    public EventResource(ObjectMapper objectMapper, EventStore input_store) {
        this.object_mapper = objectMapper;

        store = input_store;
    }


    @Override
    public Stream<Route<AsyncHandler<Response<ByteString>>>> routes() {
        return Stream.of(
                Route.sync("GET", "/event/get-events", this::getEvents)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("GET", "/event/<id>", this::getEventByID)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("GET", "/event/<id>/get-users", this::getUsers)
                        .withMiddleware(jsonMiddleware())
        );
    }


    /**
     * getEvents - Get all events
     * @param ctx - the request context
     * @return a list of events
     */
    private Response<List<Event>> getEvents(RequestContext ctx) {
        List<Event> events= store.getEvents();

        if (events == null)
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
        else
            return Response.ok().withPayload(events);
    }


    /**
     * getEventByID - gets an event from its ID
     * @param ctx - the request context
     * @return The event object.
     */
    private Response<Event> getEventByID(RequestContext ctx) {

        String id = ctx.pathArgs().get("id");
        // some basic error checking
        if (id == null || id.isEmpty()) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing event id"));
        }

        // get the list of users from the database and return it
        Event event = store.getEvent(id);

        if (event != null)
            return Response.ok().withPayload(event);
        else
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
    }





    /**
     * getUsersAdmin - Returns the users that are subscribed to an event.
     *
     * @param ctx The request context.
     *
     * @return A dynamic response. On success, payload contains a list of User objects with their first name, last
     * name, and emails.
     */
    @VisibleForTesting
    public Response<List<User>> getUsers(RequestContext ctx) {
        // some basic error checking
        if (ctx.pathArgs().get("id") == null || ctx.pathArgs().get("id").isEmpty()) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
        }

        // get the list of users from the database and return it
        List<User> users;
        if (ctx.request().headers().get("Cookie") != null) {

            String[] tokens = ctx.request().headers().get("Cookie").split("=");
            users = store.getUsers(ctx.pathArgs().get("id"),
                    String.valueOf(UserResource.cookie_db.inverse().get(Integer.valueOf(tokens[1]))));
        }
        else {
            users = store.getUsers(ctx.pathArgs().get("id"),null);
        }



        if (users != null)
            return Response.ok().withPayload(users);
        else
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
    }


    /**
     * jsonMiddleware - Standard middleware function that converts the return type of an async handler into json as
     * well as sets it up as a standard HTTP response.
     *
     * @param <T>   The object returned by the handler (could be a user, group, etc).
     * @return      Returns an HTTP response with the inputted object as a jSON payload.
     */
    private <T> Middleware<AsyncHandler<Response<T>>, AsyncHandler<Response<ByteString>>> jsonMiddleware() {

        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "http://localhost:3000");
        headers.put("Access-Control-Allow-Methods", "OPTIONS, GET, POST");
        headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization, Cookie");
        headers.put("Access-Control-Allow-Credentials", "true");

        return JsonSerializerMiddlewares.<T>jsonSerializeResponse(object_mapper.writer())
                .and(Middlewares::httpPayloadSemantics)
                .and(responseAsyncHandler -> ctx ->
                        responseAsyncHandler.invoke(ctx)
                                .thenApply(response -> response.withHeaders(headers)));
    }


    /**
     * eventAuthorizationMiddleware - Implements a user session checker on an incoming request, prior to the handler
     * invocation. In actuality, it parses the header of the incoming request and  checks if there is a cookie header
     * and that the cookie ID is in the cookie_db hashtable. If so, it then confirms that the id corresponding to the
     * cookie_id is an admin of the group that owns the event whose id is specified in the ur. If all three of these
     * conditions holds, the middleware calls the corresponding route's handler and returns it's response.
     *
     * NOTE: This middleware needs to be followed by an Apollo-implemented middleware called Middleware::syncToAsync.
     * This is so that the expected types of the route provider line up.
     *
     * @param innerHandler The handler function that corresponds with the route.
     * @param <T> The object that is returned in the response of the handler function (User, Group, String, etc).
     *
     * @return A response wrapped as a handler function for concurrency with the methods are are called after this
     * middleware. However what we are nevertheless returning from this middleware is a Response<T>, not a
     * SyncHandler<Response<T>>.
     */
    public <T> SyncHandler<Response<T>> eventAuthorizationMiddleware(SyncHandler<Response<T>> innerHandler) {

        return ctx -> {
            // check matching cookie id.
            if (ctx.request().headers().get("Cookie") == null || ctx.request().headers().get("Cookie").isEmpty())
                return Response.forStatus(Status.UNAUTHORIZED);

            String[] tokens = ctx.request().headers().get("Cookie").split("=");

            // get the user id from the cookie database
            String user_id = String.valueOf(UserResource.cookie_db.inverse().get(Integer.valueOf(tokens[1])));

            // check that the user_id exists
            if (user_id == null)
                return Response.forStatus(Status.FORBIDDEN);

            // Call inner handler
            return innerHandler.invoke(ctx);
        };
    }
}
