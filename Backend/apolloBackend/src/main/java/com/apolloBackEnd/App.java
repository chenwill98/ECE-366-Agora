package com.apolloBackEnd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.apollo.AppInit;
import com.spotify.apollo.Environment;
import com.spotify.apollo.Response;
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
     * An implementation of the {@link AppInit} functional interface which sets
     * up the routes of the backEnd. It loads in the AutoMatter module, which is
     * used as the model for our database and also equally importantly allows our object
     * (such as users, groups, etc) to get serialized into jSON effortlessly.
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
                .registerAutoRoute(Route.sync("GET", "/ping",ctx -> "ping"))
                .registerRoutes(event_resource.routes())
                .registerRoutes(group_resource.routes())
                .registerRoutes(user_resource.routes());
    }



}