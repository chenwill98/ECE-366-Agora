package com.apolloBackEnd;

import com.spotify.apollo.AppInit;
import com.spotify.apollo.Environment;
import com.spotify.apollo.route.Route;
import com.spotify.apollo.httpservice.LoadingException;
import com.spotify.apollo.httpservice.HttpService;


public final class App {

    /**
     * The main entry point of the java process which will delegate to
     * {@link HttpService#boot(AppInit, String, String...)}.
     *
     * @param args  program arguments passed in from the command line
     * @throws LoadingException if anything goes wrong during the service boot sequence
     */
    public static void main(String... args) throws LoadingException {
        HttpService.boot(App::init, "guy-server", args);
    }

    /**
     * An implementation of the {@link AppInit} functional interface which simply sets
     * up a "hello world" handler on the root route "/".
     *
     * @param environment  The Apollo {@link Environment} that the service is in.
     */
    private static void init(Environment environment) {
        environment.routingEngine()
                .registerAutoRoute(Route.sync("GET", "/", requestContext -> "hello world"));
    }
}