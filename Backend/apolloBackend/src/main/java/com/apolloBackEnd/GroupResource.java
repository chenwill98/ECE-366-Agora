package com.apolloBackEnd;

import com.spotify.apollo.route.AsyncHandler;
import com.spotify.apollo.route.Route;
import com.spotify.apollo.route.RouteProvider;

import java.util.stream.Stream;

public class GroupResource implements RouteProvider {

    @Override
    public Stream<? extends Route<? extends AsyncHandler<?>>> routes() {
        return Stream.of(
                Route.sync("GET", "/group/<id>", ctx -> String.format("You have reached group number %d.\n", ctx.pathArgs().get("id")))
        );
    }
}
