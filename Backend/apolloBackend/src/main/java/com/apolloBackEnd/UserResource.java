package com.apolloBackEnd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.*;

import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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


    /* methods */
    /**
     * UserResource - A constructor for the UserResource class. This constructor
     * sets up a userStore instance that will be used throughout the
     */
    public UserResource(final ObjectMapper objectMapper) {
        this.object_mapper = objectMapper;

        Config tmp_config = ConfigFactory.parseResources("apolloBackend.conf").resolve();
        this.store = new UserStore(tmp_config);
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
                Route.sync("POST", "/ping", ctx -> "pong\n" )
                        .withMiddleware(jsonMiddleware()),
                Route.sync("GET", "/", ctx -> "you have reached Agora!\n" )
                        .withMiddleware(jsonMiddleware()),
                Route.sync("GET", "/user/<id>", ctx ->
                        String.format("you got user with id: %s\n", ctx.pathArgs().get("id")))
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/login", this::attemptLogin)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/login-test", this::attemptLoginTest)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/user/create", this::createUser)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/user/create-group", this::createGroup)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/user/change-password", this::updatePassword)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/user/join-group", this::joinGroup)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/user/leave-group", this::leaveGroup)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/user/leave-event", this::leaveEvent)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/user/join-event", this::joinEvent)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/user/<userID>/rsvp-event/<eventID>", this::rsvpEvent)
                        .withMiddleware(jsonMiddleware())
                );
    }


    /**
     * createGroup - Creates a group given the specified info.
     *
     * @param ctx The request context with the relevant info.
     * @return boolean - True on success and false otherwise.
     */
    private String createGroup(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode node = null;
        try {
            node = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // todo: give more relevant error message
        // check that all fields are filled
        if (    node.get("id").asText() == null             || node.get("id").asText().isEmpty()            ||
                node.get("description").asText() == null    || node.get("description").asText().isEmpty()   ||
                node.get("name").asText() == null           || node.get("name").asText().isEmpty() ) {
            return String.valueOf(false);
        }

        // make sure that the group does not exist yet
        GroupResource tmp_group_resource = new GroupResource(object_mapper);

        if (!tmp_group_resource.groupExists(node.get("name").asText())) {

            Group new_group = new GroupBuilder()
                    .name(node.get("name").asText())
                    .description(node.get("description").asText())
                    .build();

            return String.valueOf(store.createGroup(node.get("id").asText(), new_group));
        }
        else {
            return String.valueOf(false);
        }
    }


    /**
     * rsvpEvent - Given a user and an event ID, this function rsvps the user to the event.
     * Not sure about this one quite yet...
     *
     * @param ctx The request context with the relevant info.
     * @return boolean - true on success and false otherwise.
     */
    private boolean rsvpEvent(RequestContext ctx) {
        return false;
    }


    /**
     * joinEvent - Given a user and event ID, this funciton tells DB to update user to be attending the event.
     *
     * @param ctx The request context with the relevant user and event IDs.
     * @return boolean - true on success, and false otherwise.
     */
    private String joinEvent(RequestContext ctx) {


        JsonNode node = validateEmailHelper(ctx, false);
        if (node != null) {
            return String.valueOf(store.userJoinEvent(node.get("userid").asText(), node.get("eventname").asText(), 1));
        }
        else
            return String.valueOf(false);
    }


    /**
     * leaveEvent - Given a user and event ID, this function makes a user not attend the event
     *
     * @param ctx The request context with the relevant user and event IDs.
     * @return boolean - true on sucess and false otherwise.
     */
    private String leaveEvent(RequestContext ctx) {

        JsonNode node = validateEmailHelper(ctx, false);
        if (node != null) {
            return String.valueOf(store.userLeaveEvent(node.get("userid").asText(), node.get("eventname").asText()));
        }
        else
            return String.valueOf(false);
    }



    /**
     * leaveGroup - Given a user ID and group ID, this function makes user leave the group.
     *
     * @param ctx The request context with the relavant user and group IDs.
     * @return boolean - True of sucess, false otherwise.
     */
    private String leaveGroup(RequestContext ctx) {

        JsonNode node = validateEmailHelper(ctx, true);
        if (node != null) {
            return String.valueOf(store.userLeaveGroup(node.get("userid").asText(), node.get("groupname").asText()));
        }
        else
            return String.valueOf(false);

    }


    /**
     * joinGroup - This function adds a user to a group.
     *
     * @param ctx The request context which contains the relevant user and group ids.
     * @return boolean - true on success, else false.
     */
    private String joinGroup(RequestContext ctx) {

        JsonNode node = validateEmailHelper(ctx, true);
        if (node != null) {
            // confirm that group exists
            GroupResource tmp_group_resource = new GroupResource(object_mapper);

            if (tmp_group_resource.groupExists(node.get("groupname").asText()))
                // add yourself to the group
                return String.valueOf(store.userJoinGroup(node.get("userid").asText(), node.get("groupname").asText(), 0));
            else
                return String.valueOf(false);
        }
        else
            return String.valueOf(false);
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

        // todo: give more relevant error message
        // check that all fields are filled
        if( ids_json.get("userid").asText() != null && !ids_json.get("userid").asText().isEmpty() &&
            ids_json.get(entity_check).asText() != null && !ids_json.get(entity_check).asText().isEmpty() )
            return ids_json;
        else
            return null;
    }


    /**
     * updatePassword - Given a username, this function updates the users password in the database.
     *
     * @param ctx the request context containing the POST payload (user ID).
     * @return boolean - if the update is successful, returns true, otherwise returns false.
     */
    private String updatePassword(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode user_json = null;
        try {
            user_json = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // todo: give more relevant error message
        // check that all fields are filled
        if (    user_json.get("email").asText()     == null || user_json.get("email").asText().isEmpty()  ||
                user_json.get("oldpass").asText() == null   || user_json.get("oldpass").asText().isEmpty()  ||
                user_json.get("newpass").asText()  == null  || user_json.get("newpass").asText().isEmpty() ) {
            return String.valueOf(false);
        }

        String user_email = user_json.get("email").asText();
        String old_pass = user_json.get("oldpass").asText();
        String new_pass = user_json.get("newpass").asText();


        User db_user = store.getUser(user_email);

        if (db_user != null && db_user.pass_hash().equals(old_pass))
            return String.valueOf(store.updatePass(user_email, new_pass));
        else
            return String.valueOf(false);

    }



    /**
     * createUser - Creates a new user, adding them to the database.
     *
     * @param ctx The request context containing the POST request payload (all the user information).
     * @return A boolean, true if user successfully added to the database and false if not.
     */
    private String createUser(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode user_json = null;
        try {
            user_json = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // todo: give more relevant error message
        // check that all fields are filled
        if (    user_json.get("email").asText()     == null || user_json.get("email").asText().isEmpty()        ||
                user_json.get("firstname").asText() == null || user_json.get("firstname").asText().isEmpty()    ||
                user_json.get("lastname").asText()  == null || user_json.get("lastname").asText().isEmpty()    ||
                user_json.get("passhash").asText()  == null || user_json.get("passhash").asText().isEmpty()  ) {
            return String.valueOf(false);
        }

        // first make sure that a user with this email doesn't already exist
        User db_user = store.getUser(user_json.get("email").asText());

        if (db_user == null) {
            // add new user to the db
            User new_user = new UserBuilder()
                    .email(user_json.get("email").asText())
                    .first_name(user_json.get("firstname").asText())
                    .last_name(user_json.get("lastname").asText())
                    .pass_hash(user_json.get("passhash").asText())
                    .build();

            return String.valueOf(store.createUser(new_user));
        }
        else
            return String.valueOf(false);

    }



    /**
     * attemptLogin - Attempts to login and authenticate a user.
     *
     * @param ctx A request context that contains a username "usr" and a hashed password "pw".
     *
     * @return A UserTest object. If login is successful, than the actual user. Otherwise, a null object.
     */
    private User attemptLogin(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode user_json = null;
        try {
            user_json = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // todo: give more relevant error message
        // checking that all fields are filled
        if (    user_json.get("email").asText() == null || user_json.get("email").asText().isEmpty()  ||
                user_json.get("pass").asText()  == null || user_json.get("pass").asText().isEmpty() ) {
            return null;
        }

        // get the password of the user with that username from db and confirm it is the same.
        User auth_user = store.getUser(user_json.get("email").asText());

        // if it is the same, we return the user with all the info, otherwise we return a null object.
        if (auth_user.pass_hash().equals(user_json.get("pass").asText()))
            return auth_user;
        else
            return null;
    }


    /**
     * attemptLoginTest - Attempts to login a test user (only username and password).
     *
     * @param ctx A request context that contains a username "usr" and a hashed password "pw".
     *
     * @return A UserTest object. If login is successful, than the actual user. Otherwise, a null object.
     */
    private UserTest attemptLoginTest(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode user_json = null;
        try {
            user_json = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }


        // get the password of the user with that username from db and confirm it is the same.
        UserTest test_user = store.getUserTest(user_json.get("user").asText());

        // if it is the same, we return the user with all the info, otherwise we return a null object.
        if (test_user.PassHash().equals(user_json.get("pass").asText()))
            return test_user;
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
    private <T> Middleware<AsyncHandler<T>, AsyncHandler<Response<ByteString>>> jsonMiddleware() {

        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST");

        return JsonSerializerMiddlewares.<T>jsonSerialize(object_mapper.writer())
                    .and(Middlewares::httpPayloadSemantics)
                    .and(responseAsyncHandler -> ctx2 ->
                            responseAsyncHandler.invoke(ctx2)
                                    .thenApply(response -> response.withHeaders(headers)));
    }

}
