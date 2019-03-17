package com.apolloBackEnd;

import com.spotify.apollo.route.AsyncHandler;
import com.spotify.apollo.route.Route;
import com.spotify.apollo.route.RouteProvider;

import java.util.stream.Stream;


/**
 * EventResource -
 */
public class EventResource implements RouteProvider {

    @Override
    public Stream<? extends Route<? extends AsyncHandler<?>>> routes() {
        return Stream.of(
                Route.sync("GET", "/event/<id>", ctx -> String.format("%s\n", ctx.pathArgs().get("id")))
        );
    }
}
