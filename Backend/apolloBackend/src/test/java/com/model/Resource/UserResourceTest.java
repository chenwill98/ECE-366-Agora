package com.model.Resource;


import com.Resource.UserResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.model.*;
import com.spotify.apollo.Request;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.store.GroupStore;
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

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(MockitoJUnitRunner.class)
public class UserResourceTest {


    @Mock UserStore store;                      /* mock of the store class (interacting with db)    */
    @Mock Request request_test;                 /* mock for the Request object recieved from front-end */
    @Mock RequestContext ctx_test;              /* mock of the request context that contains the request above */
    @Mock GroupStore group_store;
    @Mock ObjectMapper object_mapper;           /* mock of the object mapper. Helps keep the tests clean        */

    private ObjectMapper real_object_mapper;    /* real object mapper - simplifies the testing - I think        */
    private UserResource test_user_resource;    /* the actual class we are testing - so obv shouldn't be mocked */
    private User test_user;                     /* a (real) user object that we will often use in our tests     */
    private Group test_group;                   /* a (real) group object that we will use in our tests          */
    private Event test_event;                   /* a (real) event object that we will use in our tests          */


    @Before
    public void setup() {
        real_object_mapper = new ObjectMapper().registerModule(new AutoMatterModule());
        test_user_resource = new UserResource(object_mapper, store, group_store);
        when(ctx_test.request()).thenReturn(request_test);

        test_user = new UserBuilder()
                .uid(1)
                .email("teser@gmail.com")
                .first_name("Tester")
                .last_name("Muchly")
                .pass_hash("Password1234")
                .build();

        test_group = new GroupBuilder()
                .id(1)
                .name("Test Group")
                .description("This is a test group")
                .build();

        test_event = new EventBuilder()
                .id(1)
                .name("Test Event")
                .date("someDate")
                .description("this is a test event")
                .location("test place")
                .build();

        when(ctx_test.pathArgs()).thenReturn(Collections.singletonMap("id", String.valueOf(test_user.uid())));
    }


    /*
     * Each @Test function is called the same name as the method that it calls and tests in the UserResource class.
     */
    @Test
    public void createUser() throws Exception {
        when(request_test.payload()).thenReturn(Optional.of(ByteString.of(real_object_mapper.writeValueAsBytes(test_user))));

        when(object_mapper.readTree(Optional
                .of(ByteString.of(real_object_mapper.writeValueAsBytes(test_user))).get().utf8()))
                .thenReturn(real_object_mapper.readTree(Optional
                        .of(ByteString.of(real_object_mapper.writeValueAsBytes(test_user))).get().utf8()));

        when(store.createUser(test_user)).thenReturn(true);

        Response<ByteString> actual_response = test_user_resource.createUser(ctx_test);

        assertEquals(200, actual_response.status().code());
    }

    @Test
    public void attemptLogin() throws Exception {
        when(store.getUserWithEmail(test_user.email())).thenReturn(test_user);

        when(request_test.payload())
                .thenReturn(Optional
                    .of(ByteString
                        .of(real_object_mapper
                            .writeValueAsBytes(test_user))));

        when(object_mapper.readTree(Optional
                .of(ByteString.of(real_object_mapper.writeValueAsBytes(test_user))).get().utf8()))
                .thenReturn(real_object_mapper.readTree(Optional
                        .of(ByteString.of(real_object_mapper.writeValueAsBytes(test_user))).get().utf8()));


        Response<List<Integer>> actual_response = test_user_resource.attemptLogin(ctx_test);
        List<Integer> actual_user_id = actual_response.payload().get();

        Integer expected_user_id = new Random(1234).nextInt(1000000);
        assertEquals(expected_user_id , actual_user_id.get(0));
    }

    @Test
    public void attemptLogoff() {
        Response<ByteString> actual_response = test_user_resource.attemptLogout(ctx_test);
        assertEquals(200, actual_response.status().code());
    }


    @Test
    public void getUser() {
        when(store.getUserWithID(String.valueOf(test_user.uid()))).thenReturn(test_user);

        Response<User> actual_response = test_user_resource.getUser(ctx_test);

        assertEquals(test_user, actual_response.payload().get());
    }

    @Test
    public void getGroups() {
        when(store.getGroups(String.valueOf(test_user.uid()))).thenReturn(Collections.singletonList(test_group));

        Response<List<Group>> actual_response = test_user_resource.getGroups(ctx_test);

        assertEquals(Collections.singletonList(test_group), actual_response.payload().get());
    }

    @Test
    public void getEvents() {
        when(store.getEvents(String.valueOf(test_user.uid()))).thenReturn(Collections.singletonList(test_event));

        Response<List<Event>> actual_response = test_user_resource.getEvents(ctx_test);

        assertEquals(Collections.singletonList(test_event), actual_response.payload().get());
    }

    @Test
    public void createGroup() throws Exception {
        when(request_test.payload()).thenReturn(Optional.of(ByteString.of(real_object_mapper.writeValueAsBytes(test_group))));

        when(object_mapper.readTree(Optional
                .of(ByteString.of(real_object_mapper.writeValueAsBytes(test_group))).get().utf8()))
                .thenReturn(real_object_mapper.readTree(Optional
                        .of(ByteString.of(real_object_mapper.writeValueAsBytes(test_group))).get().utf8()));

        when(store.createGroup(String.valueOf(test_user.uid()), test_group)).thenReturn(true);

        Response<ByteString> actual_response = test_user_resource.createGroup(ctx_test);

        assertEquals(200, actual_response.status().code());
    }

    @Test
    public void updatePassword() throws Exception {
        when(request_test.payload()).thenReturn(Optional.of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                .of("oldpass", test_user.pass_hash(), "newpass", "456")))));

        when(object_mapper.readTree(Optional
                .of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                        .of("oldpass", test_user.pass_hash(), "newpass", "456")))).get().utf8()))
                .thenReturn(real_object_mapper.readTree(Optional
                        .of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                                .of("oldpass", test_user.pass_hash(), "newpass", "456")))).get().utf8()));

        when(store.getUserWithID(String.valueOf(test_user.uid()))).thenReturn(test_user);
        when(store.updatePass(String.valueOf(test_user.uid()), "456")).thenReturn(true);

        Response<ByteString> actual_response = test_user_resource.updatePassword(ctx_test);

        assertEquals(200, actual_response.status().code());
    }

    @Test
    public void joinGroup() throws Exception {
        when(request_test.payload()).thenReturn(Optional.of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                .of("groupname", test_group.name())))));

        when(object_mapper.readTree(Optional
                .of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                        .of("groupname", test_group.name())))).get().utf8()))
                .thenReturn(real_object_mapper.readTree(Optional
                        .of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                                .of("groupname", test_group.name())))).get().utf8()));

        when(store.userJoinGroup(String.valueOf(test_user.uid()), test_group.name(), 0)).thenReturn(true);
        when(group_store.getGroup(test_group.name())).thenReturn(test_group);

        Response<ByteString> actual_response = test_user_resource.joinGroup(ctx_test);

        assertEquals(200, actual_response.status().code());
    }

    @Test
    public void leaveGroup() throws Exception {
        when(request_test.payload()).thenReturn(Optional.of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                .of("groupname", test_group.name())))));

        when(object_mapper.readTree(Optional
                .of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                        .of("groupname", test_group.name())))).get().utf8()))
                .thenReturn(real_object_mapper.readTree(Optional
                        .of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                                .of("groupname", test_group.name())))).get().utf8()));

        when(store.userLeaveGroup(String.valueOf(test_user.uid()), test_group.name())).thenReturn(true);
        when(group_store.getGroup(test_group.name())).thenReturn(test_group);

        Response<ByteString> actual_response = test_user_resource.leaveGroup(ctx_test);

        assertEquals(200, actual_response.status().code());
    }

    @Test
    public void joinEvent() throws Exception {
        when(request_test.payload()).thenReturn(Optional.of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                .of("eventname", test_event.name())))));

        when(object_mapper.readTree(Optional
                .of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                        .of("eventname", test_event.name())))).get().utf8()))
                .thenReturn(real_object_mapper.readTree(Optional
                        .of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                                .of("eventname", test_event.name())))).get().utf8()));

        when(store.userJoinEvent(String.valueOf(test_user.uid()), test_event.name(), 1)).thenReturn(true);

        Response<ByteString> actual_response = test_user_resource.joinEvent(ctx_test);

        assertEquals(200, actual_response.status().code());
    }

    @Test
    public void leaveEvent() throws Exception {
        when(request_test.payload()).thenReturn(Optional.of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                .of("eventname", test_event.name())))));

        when(object_mapper.readTree(Optional
                .of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                        .of("eventname", test_event.name())))).get().utf8()))
                .thenReturn(real_object_mapper.readTree(Optional
                        .of(ByteString.of(real_object_mapper.writeValueAsBytes(ImmutableMap
                                .of("eventname", test_event.name())))).get().utf8()));

        when(store.userLeaveEvent(String.valueOf(test_user.uid()), test_event.name())).thenReturn(true);

        Response<ByteString> actual_response = test_user_resource.leaveEvent(ctx_test);

        assertEquals(200, actual_response.status().code());
    }

}
