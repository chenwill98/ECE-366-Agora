package com.model.Resource;


import com.Resource.UserResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.model.User;
import com.model.UserBuilder;
import com.spotify.apollo.Request;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.store.UserStore;
import io.norberg.automatter.jackson.AutoMatterModule;
import okio.ByteString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class userResourceTest {


    @Mock UserStore store;                      /* mock of the store class (interacting with db)    */
    @Mock BiMap<Integer, Integer> cookie_db;    /* mock of the cookie_db                            */

    @Mock Request request_test;                 /* mock for the Request object recieved from front-end */
    @Mock RequestContext ctx_test;                   /* mock of the request context that contains the request above */

    ObjectMapper object_mapper;                 /* real object mapper - simplifies the testing - I think        */
    private UserResource test_user_resource;    /* the actual class we are testing - so obv shouldn't be mocked */
    User test_user;


    @Before
    public void setup() {
        object_mapper = new ObjectMapper().registerModule(new AutoMatterModule());
        test_user_resource = new UserResource(object_mapper, store);
        test_user = new UserBuilder()
                .uid(1)
                .email("teser@gmail.com")
                .first_name("Tester")
                .last_name("Muchly")
                .pass_hash("Password1234")
                .build();

        when(ctx_test.request()).thenReturn(request_test);

    }

    @Test
    public void createUser() throws Exception {

        when(ctx_test.request()).thenReturn(request_test);

        when(request_test.payload()).thenReturn(Optional.of(ByteString.of(object_mapper.writeValueAsBytes(test_user))));

        when(store.createUser(test_user)).thenReturn(true);

        Response<ByteString> actual_response = test_user_resource.createUser(ctx_test);


        assertEquals(200, actual_response.status().code());
    }


    /*
     * Each @Test function is called the same name as the method that it calls and tests in the UserResource class.
     */

    @Test
    public void attemptLogin() throws Exception {

        when(store.getUserWithEmail(test_user.email())).thenReturn(test_user);

        when(request_test.payload())
                .thenReturn(Optional
                    .of(ByteString
                        .of(object_mapper
                            .writeValueAsBytes(test_user))));


        Response<Integer> actual_response = test_user_resource.attemptLogin(ctx_test);
        Integer actual_user_id = actual_response.payload().get();

        Integer expected_user_id = new Random(1234).nextInt(1000000);
        assertEquals(expected_user_id , actual_user_id);
    }

    @Test
    public void getUser() {
        when(ctx_test.pathArgs()).thenReturn(Collections.singletonMap("id", String.valueOf(test_user.uid())));

        when(store.getUserWithID(String.valueOf(test_user.uid()))).thenReturn(test_user);

        Response<User> actual_response = test_user_resource.getUser(ctx_test);

        assertEquals(test_user, actual_response.payload().get());
    }

    @Test
    public void getGroups() {

    }

    @Test
    public void getEvents() {

    }

    @Test
    public void createGroup() {

    }

    @Test
    public void updatePassword() {

    }

    @Test
    public void joinGroup() {

    }

    @Test
    public void leaveGroup() {

    }

    @Test
    public void joinEvent() {

    }
}
