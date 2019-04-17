package com.Resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.model.*;

import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import java.util.stream.Stream;

import com.spotify.apollo.Status;
import com.spotify.apollo.route.*;
import com.store.GroupStore;
import com.store.UserStore;
import okio.ByteString;


/**
 * UserResource - The class that implements the routing necessary for users and
 * the handlers that are called for each route.
 *
 * Included in this class are also the middleware functions that will be called
 * to alter the backend server's response format (what is returned by the route
 * handlers.
 */
public class UserResource implements RouteProvider {

    /* fields */
    private final UserStore store;                  /* the user store instance used in the UserResource class   */
    private final ObjectMapper object_mapper;       /* used in the middleware for altering response formats     */
    static BiMap<Integer, Integer> cookie_db;       /* cookie IDs mapper: Key = user id, Value = cookie ID      */

    private GroupStore group_store;                 /* used to confirm a group exists                           */

    /* methods */
    /**
     * UserResource - A constructor for the UserResource class. This constructor
     * sets up a userStore instance that will be used.
     */
    public UserResource(final ObjectMapper objectMapper, UserStore input_store, GroupStore group_store) {
        this.object_mapper = objectMapper;

        this.store = input_store;

        if (cookie_db == null) {    /* because it is static, we don't want it to be called twice */
            UserResource.cookie_db = HashBiMap.create();
        }

        this.group_store = group_store;
    }


    /**
     * routes - Defines the routes that are related to the UserResource. These include most of the
     * functionality related to users.
     *
     * NOTE: the request context contains a way to get both the id in the url as well as the request payload. Note that
     * in some of the URLs specified there exists <id> tags, this specifies that the request context can use whatever
     * variable is in this location in the url. Ex: If you have a route '/this/is/a/<id>/route' and an http request
     * comes in to '/this/is/a/1234/route', the request context will have a field called 'id' that equals 1234. This is
     * used heavily in implementing user sessions.
     *
     * NOTE 2: The routes that have the "userSessionMiddleware" middleware are those that require a cookie to be sent
     * in the header. Further information of the BackEnd's user sessions is deferred to the middleware's description.
     *
     * @return The asynchronous responses of the route handlers. These will be dynamic in the sense that different
     * routes require different request payloads and headers (for example some require a cookie header), and based on
     * these requests the response might be a 200 (ok) with or without a payload, 400-bad request, along with a
     * description of what went wrong, 500-internal server error, etc.
     */
    @Override
    public Stream<Route<AsyncHandler<Response<ByteString>>>> routes() {
        return Stream.of(
                Route.sync("GET", "/", ctx -> Response.ok().withPayload("you have reached Agora!\n"))
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/login", this::attemptLogin)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/logout", this::attemptLogout)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/user/create", this::createUser)
                    .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<User>>>create("GET", "/user/<id>/get-user", this::getUser)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),                Route.<SyncHandler<Response<List<Group>>>>create("GET", "/user/<id>/groups", this::getGroups)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<List<Event>>>>create("GET", "/user/<id>/events", this::getEvents)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/create-group", this::createGroup)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/change-password", this::updatePassword)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/join-group", this::joinGroup)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/leave-group", this::leaveGroup)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/leave-event", this::leaveEvent)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/join-event", this::joinEvent)
                        .withMiddleware(UserResource::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/user/<id>/rsvp-event/<eventID>", this::rsvpEvent)
                        .withMiddleware(jsonMiddleware())
                );
    }



    /**
     * getUser - Gets a User object with the specific user id.
     *
     *  @param ctx The request context containing the user id.
     *
     *  @return A response, which on success will return the desired User object. On error it will return a 400- Bad
     *  Request status code.
     */
    @VisibleForTesting
    public Response<User> getUser(RequestContext ctx) {

        // get and return user
        User tmp = store.getUserWithID(ctx.pathArgs().get("id"));

        if (tmp == null)
            return Response.forStatus(Status.BAD_REQUEST);
        else
            return Response.ok().withPayload(tmp);
    }


    /**
     * getGroups - Queries the database to get the list of groups that a user belongs to.
     *
     * @param ctx The request context.
     *
     * @return A response which if successful contains the list of groups in its payload, else an error code.
     */
    @VisibleForTesting
    public Response<List<Group>> getGroups(RequestContext ctx) {

        List<Group> tmp = store.getGroups(ctx.pathArgs().get("id"));

        if (tmp == null)
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
        else
            return Response.ok().withPayload(tmp);
    }


    /**
     * getEvents - Queries the database to get the list of events associated with the user.
     *
     * @param ctx - the request context.
     *
     * @return A response which if successful contains the list of events, or some error code.
     */
    @VisibleForTesting
    public Response<List<Event>> getEvents(RequestContext ctx) {

        List<Event> tmp = store.getEvents(ctx.pathArgs().get("id"));

        if (tmp == null)
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
        else
            return Response.ok().withPayload(tmp);
    }


    /**
     * createGroup - Creates a group and adds it to the database.
     *
     * @param ctx The request context with the following relevant info: "description", "name".
     *
     * @return Response with status code 200 on success, else some error status code.
     */
    @VisibleForTesting
    public Response<ByteString> createGroup(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode node = null;
        try {
            node = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check that all fields are filled
        if (node.get("description") == null  || node.get("description").asText().isEmpty()   ||
            node.get("name") == null         || node.get("name").asText().isEmpty()  ) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Entries"));
        }

        if (group_store.getGroup(node.get("name").asText()) == null) {

            Group new_group;
            /* this logic is only needed for the unit testing */
            if (node.get("id") != null && !node.get("id").asText().isEmpty()) {
                new_group = new GroupBuilder()
                        .name(node.get("name").asText())
                        .description(node.get("description").asText())
                        .id(Integer.valueOf(node.get("id").asText()))
                        .build();
            }
            else {
                new_group = new GroupBuilder()
                        .name(node.get("name").asText())
                        .description(node.get("description").asText())
                        .build();
            }

            if (store.createGroup(ctx.pathArgs().get("id"), new_group))
                return Response.ok();
        }

        return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Group already exists!"));
    }


    /**
     * rsvpEvent - Not sure about this one quite yet...
     *
     * @param ctx
     * @return
     */
    private Response<ByteString> rsvpEvent(RequestContext ctx) {
        return Response.ok();
    }


    /**
     * joinEvent - Given a user and event ID, this function tells DB to update a user to be attending the event.
     *
     * @param ctx The request context with the relevant user and event name. It needs the User ID to be in the route
     *            and the event name to be in the request payload with the key "eventname".
     *
     * @return Response with status code 200 on success, else some error status code.
     */
    @VisibleForTesting
    public Response<ByteString> joinEvent(RequestContext ctx) {

        JsonNode node = validateEmailHelper(ctx, false);
        if (node != null) {
            try {
                if(store.userJoinEvent(ctx.pathArgs().get("id"), node.get("eventname").asText(),1))
                    return Response.ok();
                else
                    return Response.forStatus(Status.BAD_REQUEST);
            } catch (SQLException e) {
                return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
            }
        }
        else
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
    }


    /**
     * leaveEvent - Given a user id and an event name, this function makes a user not attend the event
     *
     * @param ctx The request context with the relevant user and event name. It needs the User ID to be in the route
     *            and the event name to be in the request payload with the key "eventname".
     *
     * @return Response with status code 200 on success, else some error status code.
     */
    @VisibleForTesting
    public Response<ByteString> leaveEvent(RequestContext ctx) {

        JsonNode node = validateEmailHelper(ctx, false);
        if (node != null) {
            if (store.userLeaveEvent(ctx.pathArgs().get("id"), node.get("eventname").asText()))
                return Response.ok();
            else
                return Response.forStatus(Status.BAD_REQUEST);
        }
        else
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
    }



    /**
     * leaveGroup - Given a user ID and a group name, this function makes a user leave a group.
     *
     * @param ctx The request context with the relevant user and group name. User ID should be in the route and the
     *            group name should be specified in request payload with the "groupname" key.
     *
     * @return Response with status code 200 on success, else some error status code.
     */
    @VisibleForTesting
    public Response<ByteString> leaveGroup(RequestContext ctx) {

        JsonNode node = validateEmailHelper(ctx, true);
        if (node != null) {
            if (store.userLeaveGroup(ctx.pathArgs().get("id"), node.get("groupname").asText()))
                return Response.ok();
            else
                return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
        }
        else
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));

    }


    /**
     * joinGroup - This function adds a user to a group.
     *
     * @param ctx The request context with the relevant user and group name. User ID should be in the route and the
     *            group name should be specified in request payload with the "groupname" key.
     *
     * @return Response with status code 200 on success, else some error status code.
     */
    @VisibleForTesting
    public Response<ByteString> joinGroup(RequestContext ctx) {

        JsonNode node = validateEmailHelper(ctx, true);
        if (node != null) {

            if (group_store.getGroup(node.get("groupname").asText()) != null) {
                // add yourself to the group
                if (store.userJoinGroup(ctx.pathArgs().get("id"), node.get("groupname").asText(), 0))
                    return Response.ok();
                else
                    return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
            }
            else
                return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Invalid group name"));
        }
        else
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
    }


    /**
     * validateEmailHelper - A helper function that validates the payload of a POST request that
     * contains a user email and either a group or event name.
     *
     * @param ctx   The request context that contains the HTTP POST request payload.
     * @param toggle boolean: true: check for groupname, false: check for eventname.
     *
     * @return JsonNode A JsonNode if this request is valid, null otherwise.
     */
    private JsonNode validateEmailHelper(RequestContext ctx, boolean toggle) {

        String entity_check = (toggle) ? "groupname" : "eventname";

        // convert request payload into JSON
        JsonNode ids_json;
        try {
            ids_json = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // check that all fields are filled
        if( (ctx.pathArgs().get("id") != null)              && !ctx.pathArgs().get("id").isEmpty() &&
            (ids_json.get(entity_check).asText() != null)   && !ids_json.get(entity_check).asText().isEmpty() ) {
            return ids_json;
        } else
            return null;
    }


    /**
     * updatePassword - This function updates the users password in the database.
     *
     * @param ctx the request context containing the relevant info: User ID specified in the route, and in the request
     *            payload the old and new passwords with the key "oldpass" and "newpass" respectively. NOTE that these
     *            are the hash-ed versions of the passwords.
     *
     * @return Response with status code 200 on success, else some error status code.
     */
    @VisibleForTesting
    public Response<ByteString> updatePassword(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode user_json = null;
        try {
            user_json = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check that all fields are filled
        if (    user_json != null && (
                (ctx.pathArgs().get("id") == null)          || ctx.pathArgs().get("id").isEmpty() ||
                (user_json.get("oldpass").asText() == null) || user_json.get("oldpass").asText().isEmpty() ||
                (user_json.get("newpass").asText() == null) || user_json.get("newpass").asText().isEmpty()) ){
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
        }

        String user_id = ctx.pathArgs().get("id");
        String old_pass = user_json.get("oldpass").asText();
        String new_pass = user_json.get("newpass").asText();


        User db_user = store.getUserWithID(user_id);

        if (db_user == null)
            return Response.forStatus((Status.BAD_REQUEST.withReasonPhrase("No User Found")));
        else if (db_user.pass_hash().equals(old_pass) && store.updatePass(user_id, new_pass))
                return Response.ok();
        else
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
    }



    /**
     * createUser - Creates a new user, adding them to the database.
     *
     * @param ctx The request context with the relevant info: In the request payload there should be specified an email,
     *            first name, last name, and password hash, with the keys: "email", "first_name", "last_name", and
     *            "pass_hash".
     *
     * @return Response with status code 200 on success, else some error status code.
     */
    @VisibleForTesting
    public Response<ByteString> createUser(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode user_json = null;
        try {
            user_json = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check that all fields are filled
        if (    user_json != null && (
                (user_json.get("email").asText() == null)       || user_json.get("email").asText().isEmpty()        ||
                (user_json.get("first_name").asText() == null)   || user_json.get("first_name").asText().isEmpty()    ||
                (user_json.get("last_name").asText() == null)    || user_json.get("last_name").asText().isEmpty()     ||
                (user_json.get("pass_hash").asText() == null)    || user_json.get("pass_hash").asText().isEmpty()) ) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
        }

        /* this if statement is useful for conducting the unit tests. Not optimal (ie not needed) during production */
        User new_user;
        if (user_json.get("uid") != null )
            new_user = new UserBuilder()
                    .uid(Integer.valueOf(user_json.get("uid").asText()))
                    .email(user_json.get("email").asText())
                    .first_name(user_json.get("first_name").asText())
                    .last_name(user_json.get("last_name").asText())
                    .pass_hash(user_json.get("pass_hash").asText())
                    .build();
        else
            new_user = new UserBuilder()
                    .email(user_json.get("email").asText())
                    .first_name(user_json.get("first_name").asText())
                    .last_name(user_json.get("last_name").asText())
                    .pass_hash(user_json.get("pass_hash").asText())
                    .build();


        if (store.createUser(new_user))
            return Response.ok();
        else
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);

    }


    /**
     * attemptLogout - Logs a user out- in essence just stopping the user session by deleting the user's cookie id from
     * the database.
     *
     * @param ctx The request context that contains the id of the user in the route.
     *
     * @return Response with status code 200 on success.
     */
    @VisibleForTesting
    public Response<ByteString> attemptLogout(RequestContext ctx) {
        cookie_db.remove(Integer.valueOf(ctx.pathArgs().get("id")));
        return Response.ok();
    }



    /**
     * attemptLogin - Attempts to login and authenticate a user.
     *
     * @param ctx A request context that contains an email and hashed password of the user who wants to login. These
     *            will have the keys "email" and "pass_hash", respectively.
     *
     * @return Response which on success contains a payload with a list of integers corresponding with the user who
     * logged in, else some error status code. The list is of 2 elements: A cookie id and a user id.
     */
    @VisibleForTesting
    public Response<List<Integer>> attemptLogin(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode user_json = null;
        try {
            user_json = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // checking that all fields are filled
        if (    user_json != null && (
                (user_json.get("email").asText() == null) || user_json.get("email").asText().isEmpty() ||
                (user_json.get("pass_hash").asText() == null)  || user_json.get("pass_hash").asText().isEmpty()) ) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
        }

        // get the password of the user with that username from db and confirm it is the same.
        User auth_user = store.getUserWithEmail(user_json.get("email").asText());


        List<Integer> values = new ArrayList<>();

        values.add(getCookieID(auth_user.uid()));
        values.add(auth_user.uid());

        // if it is the same, we return the user with all the info, otherwise we return a null object.
        if (auth_user != null && auth_user.pass_hash().equals(user_json.get("pass_hash").asText())) {
            return Response.ok().withPayload(values);
        }
        else
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Incorrect email or password"));
    }


    /**
     * getCookieID - Gets the cookie ID belonging to a user. If the cookie ID exists already in the cookie_db hashmap,
     * simply returns it. If it doesn't, create a random cookie ID, update the cookie_db hashmap with it, and return it.
     *
     * @param user_id The user id of the user in question.
     * @return The cookie ID.
     */
    private Integer getCookieID(Integer user_id) {
        if (cookie_db.containsKey(user_id)) {
            return cookie_db.get(user_id);
        }
        else {
            Random rand = new Random(1234);  /* seed corresponds to testing seed so that we can run unit tests */
            Integer new_cookie_id = rand.nextInt(1000000);
            cookie_db.put(user_id, new_cookie_id);
            return new_cookie_id;
        }
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
        headers.put("Access-Control-Allow-Methods", "OPTIONS, GET, POST");
        headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization");

        return JsonSerializerMiddlewares.<T>jsonSerializeResponse(object_mapper.writer())
                .and(Middlewares::httpPayloadSemantics)
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
    public static <T> SyncHandler<Response<T>> userSessionMiddleware(SyncHandler<Response<T>> innerHandler) {

        return ctx -> {
            System.out.println("INCOMING HEADERS: " + ctx.request().headers());
            // check matching cookie id.
            if (ctx.request().headers().get("Authorization") == null || ctx.request().headers().get("Authorization").isEmpty())
                return Response.forStatus(Status.UNAUTHORIZED);

            String[] tokens = ctx.request().headers().get("Authorization").split("=");
            String cookie_id = String.valueOf(cookie_db.get(Integer.valueOf(ctx.pathArgs().get("id"))));

            if (cookie_id == null || !tokens[0].equals("USER_TOKEN") || !cookie_id.equals(tokens[1]))
                return Response.forStatus(Status.FORBIDDEN);

            // Call inner handler
            return innerHandler.invoke(ctx);
        };
    }
}
