package com.apolloBackEnd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Event;
import com.model.EventBuilder;
import com.model.Group;
import com.model.User;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.spotify.apollo.Status;
import com.spotify.apollo.route.*;
import com.store.GroupStore;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import okio.ByteString;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GroupResource implements RouteProvider {

    /* fields */
    private static GroupStore store;                 /* the group store instance used in the GroupResource class */
    private final ObjectMapper object_mapper;       /* used in the middleware for altering response formats     */



    /* methods */
    /**
     * Constructor for the Group Resource. Initialized the object mapper and the group store fields.
     *
     * @param objectMapper The object mapper object used for altering the format of the route responses.
     */
    public GroupResource(ObjectMapper objectMapper) {

        this.object_mapper = objectMapper;
        Config tmp_config = ConfigFactory.parseResources("apolloBackend.conf").resolve();

        if (this.store == null)
            this.store = new GroupStore(tmp_config);

    }


    @Override
    public Stream<Route<AsyncHandler<Response<ByteString>>>> routes() {
        return Stream.of(
                Route.sync("GET", "/group/<id>", ctx ->
                                    String.format("You have reached group # %d.\n", ctx.pathArgs().get("id")))
                        .withMiddleware(jsonMiddleware()),
                Route.sync("GET", "/group/<id>/get-users", this::getUsers)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/group/<id>/create-event", this::createEvent)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/group/<id>/edit-event", this::editEvent)
                        .withMiddleware(GroupResource::groupAdminSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware())

        /* TODO: editEvent(), deleteEvent(), changeAdmin(), notifyGroup(), viewContacts() */

        );
    }



    /**
     * getUsers - Returns a list of users who are members of a certain group
     *
     * @param ctx The request context that contains the group ID to get the users of.
     *
     * @return A list of User objects that are members of the specified group.
     */
    private Response<List<User>> getUsers(RequestContext ctx) {
        String id = ctx.pathArgs().get("id");

        // some basic error checking
        if (id == null || id.isEmpty()) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
        }

        // get the list of users from the database and return it
        List<User> tmp = store.getUsers(id);

        if (tmp != null)
            return Response.ok().withPayload(tmp);
        else
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
    }


    /**
     * groupExists - Determines if a group with the inputted name exists or not.
     *
     * @param name The name of the group to check for (a name is unique in the group db).
     *
     * @return boolean - true if the name exists, false if it doesn't.
     */
    public boolean groupExists(String name) {

        Group group = store.getGroup(name);

        if (group != null)
            return true;
        else
            return false;
    }


    /**
     * createEvent - Creates an event & saves it to the events table in the db.
     *
     * @param ctx The request context with the relevant info.
     * @return boolean - True on success and false otherwise.
     */
    private Response<ByteString> createEvent(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode node = null;
        try {
            node = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check that all fields are filled
        if (    node.get("name").asText() == null           || node.get("name").asText().isEmpty()          ||
                node.get("description").asText() == null    || node.get("description").asText().isEmpty()   ||
                node.get("location").asText() == null       || node.get("location").asText().isEmpty()      ||
                node.get("date").asText() == null           || node.get("date").asText().isEmpty() ) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
        }

        // make sure that the group does not exist yet
        EventResource tmp_even_resource = new EventResource(object_mapper);

        if (!tmp_even_resource.eventExists(node.get("name").asText())) {

            Event new_event = new EventBuilder()
                    .name(node.get("name").asText())
                    .description(node.get("description").asText())
                    .gid(Integer.valueOf(ctx.pathArgs().get("id")))
                    .location(node.get("description").asText())
                    .date(node.get("date").asText())
                    .build();

            if (store.createEvent(ctx.pathArgs().get("id"), new_event))
                return Response.ok();
        }

        return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
    }


    /**
     * editEvent - Edits an event.
     *
     * @param ctx The request context.
     *
     * @return Response ok on sucess, else some error code.
     */
    private Response<ByteString> editEvent(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode node = null;
        try {
            node = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check that at least one field is filled (the field(s) to be edited
        if (    node.get("id").asText() == null             || node.get("id").asText().isEmpty() || (
                node.get("name").asText() == null           && node.get("name").asText().isEmpty()          &&
                node.get("description").asText() == null    && node.get("description").asText().isEmpty()   &&
                node.get("location").asText() == null       && node.get("location").asText().isEmpty()      &&
                node.get("date").asText() == null           && node.get("date").asText().isEmpty() ) ) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
        }

        // make sure that the group does not exist yet
        EventResource tmp_even_resource = new EventResource(object_mapper);

        Boolean try1 = true;
        Boolean try2 = true;
        Boolean try3 = true;
        Boolean try4 = true;

        if (!tmp_even_resource.eventExists(node.get("id").asText())) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("No such event found"));
        }
        else {
            if (node.get("id").asText() == null && node.get("name").asText().isEmpty())
                try1 = store.updateEventName(node.get("id").asText(), node.get("name").asText());
            if (node.get("id").asText() == null && node.get("description").asText().isEmpty())
                try2 = store.updateEventDescription(node.get("id").asText(), node.get("description").asText());
            if (node.get("id").asText() == null && node.get("location").asText().isEmpty())
                try3 = store.updateEventLocation(node.get("id").asText(), node.get("location").asText());
            if (node.get("id").asText() == null && node.get("date").asText().isEmpty())
                try4 = store.updateEventDate(node.get("id").asText(), node.get("date").asText());
        }

        if (try1 && try2 && try3 && try4)
            return Response.ok();
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
    private <T> Middleware<AsyncHandler<T>, AsyncHandler<Response<ByteString>>> jsonMiddleware() {

        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST");

        return JsonSerializerMiddlewares.<T>jsonSerialize(object_mapper.writer())
                .and(responseAsyncHandler -> ctx ->
                        responseAsyncHandler.invoke(ctx)
                                .thenApply(response -> response.withHeaders(headers)));
    }


    /**
     * userSessionMiddleware - Implements a user session checker on an incoming request, prior to the handler
     * invokation. In actuality, it parses the header of the incoming request and  checks if there is a cookie header
     * and that the cookie ID it contains matches the one saved in the classe's cookieID hash table. If all three of
     * these conditions holds, the middleware calls the corresponding route's handler and returns it's response.
     *
     * If there is no cookie header, we return a 401 Unauthorized responses.
     * If there is a cookie header and it does not contain the cookie ID corresponding to the same user ID that is
     * specified in the route, we return a 403 Forbidden response.
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
    public static <T> SyncHandler<Response<T>> groupAdminSessionMiddleware(SyncHandler<Response<T>> innerHandler) {

        return ctx -> {
            // check matching cookie id.
            if (ctx.request().headers().get("Cookie") == null || ctx.request().headers().get("Cookie").isEmpty())
                return Response.forStatus(Status.UNAUTHORIZED);

            String[] tokens = ctx.request().headers().get("Cookie").split("=");

            // get the user id from the cookie database
            String user_id = String.valueOf(UserResource.cookie_db.inverse().get(Integer.valueOf(tokens[1])));

            // check that the user_id is an admin in the group
            if (user_id == null || !store.isAdmin(user_id, ctx.pathArgs().get("id")))
                return Response.forStatus(Status.FORBIDDEN);

            // Call inner handler
            return innerHandler.invoke(ctx);
        };
    }
}
