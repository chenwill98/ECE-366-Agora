package com.apolloBackEnd;

import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.spotify.apollo.Status;
import com.spotify.apollo.route.AsyncHandler;
import com.spotify.apollo.route.Route;
import com.spotify.apollo.route.RouteProvider;

import java.util.stream.Stream;
import com.model.User;
import com.store.UserStore;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


/**
 * UserResource - The class that implements the routing necessary for users.
 */
public class UserResource implements RouteProvider {

    @Override
    public Stream<? extends Route<? extends AsyncHandler<?>>> routes() {
        return Stream.of(
                Route.sync("GET", "/user/<id>", ctx -> String.format("you got user with id: %d\n", ctx.pathArgs().get("id"))),
                Route.sync("GET", "/user", ctx -> "you have reached a user!\n"),
                Route.sync( "GET", "/login/<usr>/<pw>",ctx -> attemptLogin(ctx))
        );
    }


    /**
     * attemptLogin - Attempts to login a user. If successful, for now simply
     * prints a string stating that it is successful.
     *
     * @param ctx - A request context that contains a username "usr" and a hashed password "pw".
     *
     * @return - A response  - which is an optional wrapper for the return of route handlers.
     * This one will have a payload of a string and will state whether the function was successful
     * or not.
     */
    private static Response<String> attemptLogin(RequestContext ctx) {
        String username = ctx.pathArgs().get("usr");
        String password_hash = ctx.pathArgs().get("pw");

        // confirm that the username and password were both sent
        if (username == null || username.isEmpty() || password_hash == null | password_hash.isEmpty()) {
            return Response.forStatus(
                    Status.BAD_REQUEST.withReasonPhrase("Mandatory query parameters 'usr' or 'pw' is missing"));
        }


        // create dummy user class
        Config tmp_config = ConfigFactory.parseResources("apolloBackend.conf").resolve();
        UserStore store = new UserStore(tmp_config);
        User test_user = store.getUser(username);
        test_user.printUser();

        return Response.forPayload("Successful login attempt.\n");
    }

}
