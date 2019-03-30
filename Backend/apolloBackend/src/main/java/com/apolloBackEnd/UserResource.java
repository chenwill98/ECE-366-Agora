package com.apolloBackEnd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.*;

import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import java.util.stream.Stream;

import com.spotify.apollo.Status;
import com.spotify.apollo.route.*;
import com.store.UserStore;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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
    private static Map<Integer, Integer> cookie_db;        /* cookie IDs mapper: Key = user id, Value = cookie ID      */


    /* methods */
    /**
     * UserResource - A constructor for the UserResource class. This constructor
     * sets up a userStore instance that will be used throughout the
     */
    UserResource(final ObjectMapper objectMapper) {
        this.object_mapper = objectMapper;

        Config tmp_config = ConfigFactory.parseResources("apolloBackend.conf").resolve();
        this.store = new UserStore(tmp_config);

        if (cookie_db == null) {  /* because it is static, we don't want it to be called twice */
            UserResource.cookie_db = new HashMap<>();
        }
    }


    /**
     * routes - Defines the routes that are related to the UserResource. These include most of the
     * functionality related to users (duh).
     *
     * @return The asynchronous returns of the route handlers, converted into byteString through the middleware,
     * which is basically an immutable byte array.
     */
    @Override
    public Stream<Route<AsyncHandler<Response<ByteString>>>> routes() {
        return Stream.of(
                Route.sync("GET", "/", ctx -> Response.ok().withPayload("you have reached Agora!\n"))
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/login", this::attemptLogin)
                .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/login-test", this::attemptLoginTest)
                .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/user/create", this::createUser)
                .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<User>>>create("POST", "/user/<id>", this::getUser)
                        .withMiddleware(this::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/create-group", this::createGroup)
                        .withMiddleware(this::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/change-password", this::updatePassword)
                        .withMiddleware(this::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/join-group", this::joinGroup)
                        .withMiddleware(this::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/leave-group", this::leaveGroup)
                        .withMiddleware(this::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/leave-event", this::leaveEvent)
                        .withMiddleware(this::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.<SyncHandler<Response<ByteString>>>create("POST", "/user/<id>/join-event", this::joinEvent)
                        .withMiddleware(this::userSessionMiddleware)
                        .withMiddleware(Middleware::syncToAsync)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/user/<id>/rsvp-event/<eventID>", this::rsvpEvent)
                        .withMiddleware(jsonMiddleware())
                );
    }


    /**
     * getUser - Returns a User with the specific user id. Checks that the request header contains the user's cookie ID,
     * otherwise doesn't satisfy request.
     *
     *  @param ctx The request context containing the user id.
     *
     *  @return The User object that was asked for, or a null object on error.
     */
    private Response<User> getUser(RequestContext ctx) {

        // get and return user
        return Response.ok().withPayload(store.getUserWithID(ctx.pathArgs().get("id")));
    }


    /**
     * createGroup - Creates a group given the specified info.
     *
     * @param ctx The request context with the relevant info.
     * @return boolean - True on success and false otherwise.
     */
    private Response<ByteString> createGroup(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode node = null;
        try {
            node = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check that all fields are filled
        if ((ctx.pathArgs().get("id") == null)          || ctx.pathArgs().get("id").isEmpty()           ||
            (node.get("description").asText() == null)  || node.get("description").asText().isEmpty()   ||
            (node.get("name").asText() == null)         || node.get("name").asText().isEmpty()  )  {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Entries"));
        }

        // make sure that the group does not exist yet
        GroupResource tmp_group_resource = new GroupResource(object_mapper);

        if (!tmp_group_resource.groupExists(node.get("name").asText())) {

            Group new_group = new GroupBuilder()
                    .name(node.get("name").asText())
                    .description(node.get("description").asText())
                    .build();

            if (store.createGroup(ctx.pathArgs().get("id"), new_group))
                return Response.ok();
        }

        return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Group already exists!"));
    }


    /**
     * rsvpEvent - Given a user and an event ID, this function rsvps the user to the event.
     * Not sure about this one quite yet...
     *
     * @param ctx The request context with the relevant info.
     * @return boolean - true on success and false otherwise.
     */
    private Response<ByteString> rsvpEvent(RequestContext ctx) {
        return Response.ok();
    }


    /**
     * joinEvent - Given a user and event ID, this funciton tells DB to update user to be attending the event.
     *
     * @param ctx The request context with the relevant user and event IDs.
     * @return boolean - true on success, and false otherwise.
     */
    private Response<ByteString> joinEvent(RequestContext ctx) {

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
     * leaveEvent - Given a user and event ID, this function makes a user not attend the event
     *
     * @param ctx The request context with the relevant user and event IDs.
     * @return boolean - true on sucess and false otherwise.
     */
    private Response<ByteString> leaveEvent(RequestContext ctx) {

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
     * leaveGroup - Given a user ID and group ID, this function makes user leave the group.
     *
     * @param ctx The request context with the relavant user and group IDs.
     * @return boolean - True of sucess, false otherwise.
     */
    private Response<ByteString> leaveGroup(RequestContext ctx) {

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
     * @param ctx The request context which contains the relevant user and group ids.
     * @return boolean - true on success, else false.
     */
    private Response<ByteString> joinGroup(RequestContext ctx) {

        JsonNode node = validateEmailHelper(ctx, true);
        if (node != null) {
            // confirm that group exists
            GroupResource tmp_group_resource = new GroupResource(object_mapper);

            if (tmp_group_resource.groupExists(node.get("groupname").asText())) {
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
     * updatePassword - Given a username, this function updates the users password in the database.
     *
     * @param ctx the request context containing the POST payload (user ID).
     * @return boolean - if the update is successful, returns true, otherwise returns false.
     */
    private Response<ByteString> updatePassword(RequestContext ctx) {

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
     * @param ctx The request context containing the POST request payload (all the user information).
     * @return A boolean, true if user successfully added to the database and false if not.
     */
    private Response<ByteString> createUser(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode user_json = null;
        try {
            user_json = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check that all fields are filled
        if (    user_json!= null && (
                (user_json.get("email").asText() == null)       || user_json.get("email").asText().isEmpty()        ||
                (user_json.get("firstname").asText() == null)   || user_json.get("firstname").asText().isEmpty()    ||
                (user_json.get("lastname").asText() == null)    || user_json.get("lastname").asText().isEmpty()     ||
                (user_json.get("passhash").asText() == null)    || user_json.get("passhash").asText().isEmpty()) ) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
        }

        User new_user = new UserBuilder()
                .email(user_json.get("email").asText())
                .first_name(user_json.get("firstname").asText())
                .last_name(user_json.get("lastname").asText())
                .pass_hash(user_json.get("passhash").asText())
                .build();

        if (store.createUser(new_user))
            return Response.ok();
        else
            return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
    }



    /**
     * attemptLogin - Attempts to login and authenticate a user.
     *
     * @param ctx A request context that contains a username "usr" and a hashed password "pw".
     *
     * @return A UserTest object. If login is successful, than the actual user. Otherwise, a null object.
     */
    private Response<Integer> attemptLogin(RequestContext ctx) {

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
                (user_json.get("pass").asText() == null)  || user_json.get("pass").asText().isEmpty()) ) {
            return Response.forStatus(Status.BAD_REQUEST.withReasonPhrase("Missing Queries"));
        }

        // get the password of the user with that username from db and confirm it is the same.
        User auth_user = store.getUserWithEmail(user_json.get("email").asText());

        // if it is the same, we return the user with all the info, otherwise we return a null object.
        if (auth_user.pass_hash().equals(user_json.get("pass").asText())) {
            return Response.ok().withPayload(getCookieID(auth_user.uid()));
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
            Random rand = new Random();
            Integer new_cookie_id = rand.nextInt(1000000);
            cookie_db.put(user_id, new_cookie_id);
            return new_cookie_id;
        }
    }


    /**
     * attemptLoginTest - Attempts to login a test user (only username and password).
     *
     * @param ctx A request context that contains a username "usr" and a hashed password "pw".
     *
     * @return A UserTest object. If login is successful, than the actual user. Otherwise, a null object.
     */
    private Response<UserTest> attemptLoginTest(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode user_json = null;
        try {
            user_json = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }


        // get the password of the user with that username from db and confirm it is the same.
        assert user_json != null;
        UserTest test_user = store.getUserTest(user_json.get("user").asText());

        // if it is the same, we return the user with all the info, otherwise we return a null object.
        if (test_user.PassHash().equals(user_json.get("pass").asText()))
            return Response.ok().withPayload(test_user);
        else
            return null;
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
    private <T> SyncHandler<Response<T>> userSessionMiddleware(SyncHandler<Response<T>> innerHandler) {

        return ctx -> {
            // check matching cookie id.
            if (ctx.request().headers().get("Cookie") == null || ctx.request().headers().get("Cookie").isEmpty())
                return Response.forStatus(Status.UNAUTHORIZED);

            String[] tokens = ctx.request().headers().get("Cookie").split("=");
            String cookie_id = String.valueOf(cookie_db.get(Integer.valueOf(ctx.pathArgs().get("id"))));

            if (cookie_id == null || !tokens[0].equals("USER_TOKEN") || !cookie_id.equals(tokens[1]))
                return Response.forStatus(Status.FORBIDDEN);

            // Call inner handler
            return innerHandler.invoke(ctx);
        };
    }
}
