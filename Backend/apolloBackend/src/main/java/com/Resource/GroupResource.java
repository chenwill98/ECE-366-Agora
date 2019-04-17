package com.Resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.model.*;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.spotify.apollo.Status;
import com.spotify.apollo.route.*;
import com.store.EventStore;
import com.store.GroupStore;
import okio.ByteString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GroupResource implements RouteProvider {

    /* fields */
    private final GroupStore store;                 /* the group store instance used in the GroupResource class */
    private final ObjectMapper object_mapper;       /* used in the middleware for altering response formats     */
    private final EventStore event_store;           /* used to access the events table in the database          */


    /* methods */
    /**
     * Constructor for the Group Resource. Initializes the class' object mapper and the store fields.
     *
     * @param objectMapper The object mapper object used for altering the format of the route responses.
     */
    public GroupResource(ObjectMapper objectMapper, GroupStore input_store, EventStore event_store) {

        this.object_mapper = objectMapper;
        store = input_store;
        this.event_store = event_store;

    }


    @Override
    public Stream<Route<AsyncHandler<Response<ByteString>>>> routes() {
        return Stream.of(
                Route.sync("GET", "/group/<id>", this::getGroup)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("GET", "/group/get-groups", this::getGroups)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("GET", "/group/<id>/get-users", this::getUsers)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("GET", "/group/<id>/get-events", this::getEvents)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<Boolean>>>create("GET", "/group/<id>/is-admin/<uid>", this::isAdmin)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/group/<id>/create-event", this::createEvent)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/group/<id>/edit-event", this::editEvent)
                        .withMiddleware(handler -> groupAdminSessionMiddleware(handler))
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/group/<id>/delete-event", this::deleteEvent)
                        .withMiddleware(handler -> groupAdminSessionMiddleware(handler))
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/group/<id>/update-admins", this::updateAdmins)
                        .withMiddleware(handler -> groupAdminSessionMiddleware(handler))
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<List<User>>>>create("POST", "/group/<id>/view-contacts", this::viewContacts)
                        .withMiddleware(handler -> groupAdminSessionMiddleware(handler))
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware())
        );
    }


    /* todo: comments and unit tests */
    private Response<List<Group>> getGroups(RequestContext ctx) {
        List<Group> groups = store.getGroups();

        if (groups == null)
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
        else
            return Response.ok().withPayload(groups);
    }


    /* todo: comments and unit tests */
    private Response<Boolean> isAdmin(RequestContext ctx) {
        // some basic error checking
        if (ctx.pathArgs().get("id") == null || ctx.pathArgs().get("id").isEmpty() ||
            ctx.pathArgs().get("uid") == null || ctx.pathArgs().get("uid").isEmpty()) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Group ID"));
        }


        return Response.ok().withPayload(
                store.isAdmin(ctx.pathArgs().get("uid"), ctx.pathArgs().get("id"))
        );
    }


    /* todo: comments and unit tests */
    private Response<List<Event>> getEvents(RequestContext ctx) {
        // some basic error checking
        if (ctx.pathArgs().get("id") == null || ctx.pathArgs().get("id").isEmpty()) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Group ID"));
        }

        // get the list of users from the database and return it
        List<Event> events = store.getEvents(ctx.pathArgs().get("id"));

        if (events != null)
            return Response.ok().withPayload(events);
        else
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
    }


    /* todo: comments and unit tests */
    private Response<Group> getGroup(RequestContext ctx) {
        String id = ctx.pathArgs().get("id");

        // some basic error checking
        if (id == null || id.isEmpty()) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing group id"));
        }

        // get the list of users from the database and return it
        Group group = store.getGroupByID(id);

        if (group != null)
            return Response.ok().withPayload(group);
        else
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
    }


    /**
     * viewContacts - View the first name, last name, and emails of the users belongign to a group. Can only be called
     * by an admin of the group.
     *
     * @param ctx The request context.
     *
     * @return A response which, on success, contains a payload containing a list of User objects, with their first
     * names, last names, and emails filled in.
     */
    @VisibleForTesting
    public Response<List<User>> viewContacts(RequestContext ctx) {
        String id = ctx.pathArgs().get("id");

        // some basic error checking
        if (id == null || id.isEmpty()) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
        }

        // get the list of users from the database and return it
        List<User> users = store.getUsers(id);

        if (!users.isEmpty())
            return Response.ok().withPayload(users);
        else
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);

    }


    /**
     * getUsersAdmin - Returns a list of users who are members of a certain group
     *
     * @param ctx The request context that contains the group ID to get the users of.
     *
     * @return A response which, on success, contains a paylod with a list of User objects, with only their first names
     * and last names filled in.
     */
    @VisibleForTesting
    public Response<List<User>> getUsers(RequestContext ctx) {
        // some basic error checking
        if (ctx.pathArgs().get("id") == null || ctx.pathArgs().get("id").isEmpty()) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Group id"));
        }

        // get the list of users from the database and return it
        List<User> users = store.getUsers(ctx.pathArgs().get("id"));

        // get rid of the emails of the users, because this function's caller is not privy to this information
        List<User> restricted_info_users = new ArrayList<User>();

        for (User user : users)
            restricted_info_users.add(new UserBuilder()
                    .first_name(user.first_name())
                    .last_name(user.last_name())
                    .email("")
                    .pass_hash("")
                    .build());

        if (restricted_info_users != null)
            return Response.ok().withPayload(restricted_info_users);
        else
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
    }


    /**
     * createEvent - Creates an event & saves it to the events table in the db.
     *
     * @param ctx The request context with the relevant info: A request with a payload containing the keys: "name",
     *            "description", "location", and "date". The date needs to be in the format: "2019-03-27".
     *
     * @return Response with status code 200 on success, else some error status code.
     */
    @VisibleForTesting
    public Response<ByteString> createEvent(RequestContext ctx) {

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

        if (event_store.getEvent(node.get("name").asText()) != null) {

            Event new_event;

            /* this logic is needed soley for unit testing */
            if (node.get("id") != null && !node.get("id").asText().isEmpty()) {
                new_event = new EventBuilder()
                        .id(Integer.valueOf(node.get("id").asText()))
                        .name(node.get("name").asText())
                        .description(node.get("description").asText())
                        .gid(Integer.valueOf(ctx.pathArgs().get("id")))
                        .location(node.get("location").asText())
                        .date(node.get("date").asText())
                        .build();
            }
            else {
                new_event = new EventBuilder()
                        .name(node.get("name").asText())
                        .description(node.get("description").asText())
                        .gid(Integer.valueOf(ctx.pathArgs().get("id")))
                        .location(node.get("location").asText())
                        .date(node.get("date").asText())
                        .build();
            }

            if (store.createEvent(new_event))
                return Response.ok();
        }
        return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
    }


    /**
     * editEvent - Edits an event. Needs to contain at least one of the following things in the request payload to
     * have a chance of returning success: "name", "description", "location", "date". At least one of these keys needs
     * to be in the payload, but more than one is of course okay.
     *
     * @param ctx The request context.
     *
     * @return Response with status code 200 on success, else some error status code.
     */
    @VisibleForTesting
    public Response<ByteString> editEvent(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode node = null;
        try {
            node = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Boolean try1 = true;
        Boolean try2 = true;
        Boolean try3 = true;
        Boolean try4 = true;

        if (event_store.getEvent(node.get("id").asText()) != null) {
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
     * deleteEvent - Deletes an event from the database.
     *
     * @param ctx The request context that contains the event id. Note that the group ID needs to be specified in the
     *            routes, and the request payload's event id key should be "id".
     *
     * @return Response with 200 on success, otherwise some error code with reason phrase.
     */
    @VisibleForTesting
    public Response<ByteString> deleteEvent(RequestContext ctx) {
        // convert request payload into JSON
        JsonNode node = null;
        try {
            node = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (node.get("id").asText() == null && node.get("id").asText().isEmpty())
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
        else if (event_store.getEvent(node.get("id").asText()) == null) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("No such event found"));
        }

        if (store.deleteEvent(node.get("id").asText()))
            return Response.ok();
        else
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
    }


    /**
     * updateAdmins - Updates the admin status of a user in a group.
     *
     * @param ctx The request context containing the group ID in the routes and a request payload with the following
     *            fields: "user_id" and "make_admin", where make_admin should be either: 1=admin, 0=regular user.
     *
     * @return Response with 200 on success, otherwise some error code with reason phrase.
     */
    @VisibleForTesting
    public Response<ByteString> updateAdmins(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode node = null;
        try {
            node = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Group tmp = store.getGroup(ctx.pathArgs().get("id"));
        if (tmp == null) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("No such group found"));
        }

        else if (   node.get("user_id").asText() == null    || node.get("user_id").asText().isEmpty() ||
                    node.get("make_admin").asText() == null || node.get("make_admin").asText().isEmpty() ) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
        }

        if (store.updateAdmins(ctx.pathArgs().get("id"), node.get("user_id").asText(),
                                                                Integer.valueOf(node.get("make_admin").asText()))) {
            return Response.ok();
        } else
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
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST");
        headers.put("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");

        return JsonSerializerMiddlewares.<T>jsonSerializeResponse(object_mapper.writer())
                .and(Middlewares::httpPayloadSemantics)
                .and(responseAsyncHandler -> ctx ->
                        responseAsyncHandler.invoke(ctx)
                                .thenApply(response -> response.withHeaders(headers)));
    }


    /**
     * groupAdminSessionMiddleware - Implements a user session checker on an incoming request, prior to the handler
     * invocation. In actuality, it parses the header of the incoming request and  checks if there is a cookie header
     * and that the cookie ID is in the cookie_db hashtable. If so, it then confirms that the id corresponding to the
     * cookie_id is an admin of the group id specified in the url. If all three of these conditions holds, the
     * middleware calls the corresponding route's handler and returns it's response.
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
    public <T> SyncHandler<Response<T>> groupAdminSessionMiddleware(SyncHandler<Response<T>> innerHandler) {

        return ctx -> {
            // check matching cookie id.
            if (ctx.request().headers().get("Cookie") == null || ctx.request().headers().get("Cookie").isEmpty())
                return Response.forStatus(Status.UNAUTHORIZED);

            String[] tokens = ctx.request().headers().get("Cookie").split("=");

            // get the user id from the cookie database
            String user_id = String.valueOf(UserResource.cookie_db.inverse().get(Integer.valueOf(tokens[1])));

            // check that the user_id is an admin in the group
            String group_id = ctx.pathArgs().get("id");
            if (user_id == null || !store.isAdmin(user_id, group_id))
                return Response.forStatus(Status.FORBIDDEN);

            // Call inner handler
            return innerHandler.invoke(ctx);
        };
    }
}
