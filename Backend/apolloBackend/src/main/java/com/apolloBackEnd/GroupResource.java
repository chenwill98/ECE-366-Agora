package com.apolloBackEnd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Event;
import com.model.EventBuilder;
import com.model.Group;
import com.model.User;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.spotify.apollo.route.*;
import com.store.GroupStore;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import okio.ByteString;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class GroupResource implements RouteProvider {

    /* fields */
    private final GroupStore store;                 /* the group store instance used in the GroupResource class */
    private final ObjectMapper object_mapper;       /* used in the middleware for altering response formats     */



    /* methods */
    /**
     * Constructor for the Group Resource. Initialized the object mapper and the group store fields.
     *
     * @param objectMapper The object mapper object used for altering the format of the route responses.
     */
    public GroupResource(ObjectMapper objectMapper) {

        this.object_mapper = objectMapper;
        Config tmp_config = ConfigFactory.parseResources("apolloBackend.conf").resolve();
        this.store = new GroupStore(tmp_config);

    }


    @Override
    public Stream<Route<AsyncHandler<Response<ByteString>>>> routes() {
        return Stream.of(
                Route.sync("GET", "/group/<id>", ctx -> String.format("You have reached group number %d.\n", ctx.pathArgs().get("id")))
                        .withMiddleware(jsonMiddleware()),
                Route.sync("GET", "/group/<id>/get-users", this::getUsers)
                        .withMiddleware(jsonMiddleware()),
                Route.sync("POST", "/group/<id>/create-event", this::createEvent)
                        .withMiddleware(jsonMiddleware())

        );
    }


    /**
     * getUsers - Returns a list of users who are members of a certain group
     *
     * @param ctx The request context that contains the group ID to get the users of.
     *
     * @return A list of User objects that are members of the specified group.
     */
    private List<User> getUsers(RequestContext ctx) {
        String id = ctx.pathArgs().get("id");

        // some basic error checking
        if (id == null || id.isEmpty()) {
            return null;
        }

        // get the list of users from the database and return it
        return store.getUsers(id);



    }


    /**
     * groupExists - Determines if a group with the inputted name exists or not.
     *
     * @param name The name of the group to check for (a name is unique in the group db).
     *
     * @return boolean - true if the name exists, false if it doesn't.
     */
    public boolean groupExists(String name) {

        Group group = store.getGroup(name);

        if (group != null)
            return true;
        else
            return false;
    }


    /**
     * createEvent - Creates an event & saves it to the events table in the db.
     *
     * @param ctx The request context with the relevant info.
     * @return boolean - True on success and false otherwise.
     */
    private String createEvent(RequestContext ctx) {

        // convert request payload into JSON
        JsonNode node = null;
        try {
            node = object_mapper.readTree(ctx.request().payload().get().utf8());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // todo: give more relevant error message
        // check that all fields are filled
        if (    node.get("name").asText() == null           || node.get("name").asText().isEmpty()          ||
                node.get("description").asText() == null    || node.get("description").asText().isEmpty()   ||
                ctx.pathArgs().get("id") == null            || ctx.pathArgs().get("id").isEmpty()           ||
                node.get("location").asText() == null       || node.get("location").asText().isEmpty()      ||
                node.get("date").asText() == null           || node.get("date").asText().isEmpty() ) {
            return String.valueOf(false);
        }

        // make sure that the group does not exist yet
        EventResource tmp_even_resource = new EventResource(object_mapper);

        if (!tmp_even_resource.eventExists(node.get("name").asText())) {

            Event new_event = new EventBuilder()
                    .name(node.get("name").asText())
                    .description(node.get("description").asText())
                    .gid(Integer.valueOf(ctx.pathArgs().get("id")))
                    .location(node.get("description").asText())
                    .date(node.get("date").asText())
                    .build();

            return String.valueOf(store.createEvent(ctx.pathArgs().get("id"), new_event));
        }
        else {
            return String.valueOf(false);
        }
    }


    /**
     * jsonMiddleware - Standard middlware function that converts a the return type of an async handler into json, or
     * more specifically a ByteString type.
     * @param <T>
     * @return
     */
    private <T> Middleware<AsyncHandler<T>, AsyncHandler<Response<ByteString>>> jsonMiddleware() {
        return JsonSerializerMiddlewares.<T>jsonSerialize(object_mapper.writer())
                .and(Middlewares::httpPayloadSemantics)
                .and(responseAsyncHandler -> requestContext ->
                        responseAsyncHandler.invoke(requestContext)
                                .thenApply(response -> response.withHeader("Access-Control-Allow-Origin", "*")));

    }
}
