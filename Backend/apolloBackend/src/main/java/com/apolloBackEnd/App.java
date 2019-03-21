package com.apolloBackEnd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.apollo.AppInit;
import com.spotify.apollo.Environment;
import com.spotify.apollo.route.Route;
import com.spotify.apollo.httpservice.LoadingException;
import com.spotify.apollo.httpservice.HttpService;
import io.norberg.automatter.jackson.AutoMatterModule;


public final class App {

    /**
     * The main entry point of the java process which will delegate to
     * {@link HttpService#boot(AppInit, String, String...)}.
     *
     * @param args  program arguments passed in from the command line
     * @throws LoadingException if anything goes wrong during the service boot sequence
     */
    public static void main(String... args) throws LoadingException {
        // run http server
        HttpService.boot(App::init, "apolloBackend", args);
    }


    /**
     * An implementation of the {@link AppInit} functional interface which simply sets
     * up a "hello world" handler on the root route "/".
     *
     * @param environment  The Apollo {@link Environment} that the service is in.
     */
    private static void init(Environment environment) {

        ObjectMapper object_mapper = new ObjectMapper().registerModule(new AutoMatterModule());

        /* instantiate the resources before initializing the routes */
        UserResource user_resource = new UserResource(object_mapper);
        GroupResource group_resource = new GroupResource(object_mapper);
        EventResource event_resource = new EventResource(object_mapper);


        environment.routingEngine()
                .registerAutoRoute(Route.sync("GET", "/ping", ctx -> "pong\n"))
                .registerAutoRoute(Route.sync("GET", "/", ctx -> "you have reached Agora!\n" /* how do I redirect to /login??? */ ))
                .registerRoutes(event_resource.routes())
                .registerRoutes(group_resource.routes())
                .registerRoutes(user_resource.routes());
    }



}