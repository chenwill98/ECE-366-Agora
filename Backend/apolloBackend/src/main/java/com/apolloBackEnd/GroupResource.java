package com.apolloBackEnd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Group;
import com.spotify.apollo.Response;
import com.spotify.apollo.route.*;
import com.store.GroupStore;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import okio.ByteString;

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
                        .withMiddleware(jsonMiddleware())
        );
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
