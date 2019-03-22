package com.apolloBackEnd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.spotify.apollo.route.*;

import java.util.stream.Stream;
import com.model.User;
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
                Route.sync("GET", "/user/<id>", ctx ->
                        String.format("you got user with id: %s\n", ctx.pathArgs().get("id")))
                        .withMiddleware(jsonMiddleware()),
                Route.sync("GET", "/user", ctx -> "you have reached a user!\n")
                        .withMiddleware(jsonMiddleware()),
                Route.sync( "GET", "/login/<usr>/<pw>", this::attemptLogin)
                        .withMiddleware(jsonMiddleware())
        );
    }


    /**
     * attemptLogin - Attempts to login a user. If successful, for now simply
     * prints a string stating that it is successful.
     *
     * @param ctx A request context that contains a username "usr" and a hashed password "pw".
     *
     * @return A User object. If login is successful, than the actual user. Otherwise, a null object.
     */
    private User attemptLogin(RequestContext ctx) {
        String username = ctx.pathArgs().get("usr");
        String password_hash = ctx.pathArgs().get("pw");

        // confirm that the username and password were both sent
        if (username == null || username.isEmpty() || password_hash == null | password_hash.isEmpty()) {
            return null;
        }

        User test_user = store.getUser(username);
        //test_user.printUser();

        return test_user;
    }


    /**
     * jsonMiddleware - Standard middleware function that converts a the return type of an async handler into json, or
     * more specifically a ByteString type.
     *
     * I do not yet feel qualified to describe the parameter and return types.
     *
     * @param <T>
     * @return
     */
    private <T> Middleware<AsyncHandler<T>, AsyncHandler<Response<ByteString>>> jsonMiddleware() {

            return JsonSerializerMiddlewares.<T>jsonSerialize(object_mapper.writer())
                    .and(Middlewares::httpPayloadSemantics)
                    .and(responseAsyncHandler -> ctx2 ->
                            responseAsyncHandler.invoke(ctx2)
                                    .thenApply(response -> response.withHeader("Access-Control-Allow-Origin", "*")));
        }

}
