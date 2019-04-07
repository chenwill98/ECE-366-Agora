package com.model.Resource;

import com.Resource.GroupResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.model.*;
import com.spotify.apollo.Request;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.store.EventStore;
import com.store.GroupStore;
import io.norberg.automatter.jackson.AutoMatterModule;
import okio.ByteString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(MockitoJUnitRunner.class)
public class groupResourceTest {


    @Mock GroupStore store;                     /* mock of the store class (interacting with db)    */
    @Mock Request request_test;                 /* mock for the Request object recieved from front-end */
    @Mock RequestContext ctx_test;              /* mock of the request context that contains the request above */
    @Mock EventStore event_store;


    private ObjectMapper object_mapper;         /* real object mapper - simplifies the testing - I think        */
    private GroupResource test_group_resource;  /* the actual class we are testing - so obv shouldn't be mocked */
    private User test_user;                     /* a (real) user object that we will often use in our tests     */
    private Group test_group;                   /* a (real) group object that we will use in our tests          */
    private Event test_event;                   /* a (real) event object that we will use in our tests          */


    @Before
    public void setup() {
        object_mapper = new ObjectMapper().registerModule(new AutoMatterModule());
        test_group_resource = new GroupResource(object_mapper, store, event_store);
        when(ctx_test.request()).thenReturn(request_test);

        test_user = new UserBuilder()
                .uid(1)
                .email("teser@gmail.com")
                .first_name("Tester")
                .last_name("Muchly")
                .pass_hash("Password1234")
                .build();

        test_group = new GroupBuilder()
                .gid(1)
                .name("Test Group")
                .description("This is a test group")
                .build();

        test_event = new EventBuilder()
                .id(1)
                .name("Test Event")
                .gid(test_group.gid())
                .date("someDate")
                .description("this is a test event")
                .location("test place")
                .build();

        when(ctx_test.pathArgs()).thenReturn(Collections.singletonMap("id", String.valueOf(test_group.gid())));
    }


    /*
     * Each @Test function is called the same name as the method that it calls and tests in the UserResource class.
     */
    @Test
    public void getUsers() {
        when(store.getUsers(String.valueOf(test_group.gid()))).thenReturn(Collections.singletonList(test_user));

        Response<List<User>> actual_response = test_group_resource.getUsers(ctx_test);

        assertEquals(200, actual_response.status().code());
    }

    @Test
    public void createEvent() throws Exception {
        when(request_test.payload()).thenReturn(Optional.of(ByteString.of(object_mapper.writeValueAsBytes(test_event))));

        when(event_store.getEvent(test_event.name())).thenReturn(test_event);

        when(store.createEvent(String.valueOf(test_group.gid()), test_event)).thenReturn(true);

        Response<ByteString> actual_response = test_group_resource.createEvent(ctx_test);

        assertEquals(200, actual_response.status().code());
    }

    @Test
    public void editEvent() throws Exception {
        when(request_test.payload()).thenReturn(Optional.of(ByteString.of(object_mapper.writeValueAsBytes(test_event))));

        when(event_store.getEvent(test_event.name())).thenReturn(test_event);

        when(store.updateEventName(String.valueOf(test_event.id()), test_event.name())).thenReturn(true);
        when(store.updateEventLocation(String.valueOf(test_event.id()), test_event.location())).thenReturn(true);
        when(store.updateEventDescription(String.valueOf(test_event.id()), test_event.description())).thenReturn(true);
        when(store.updateEventDate(String.valueOf(test_event.id()), test_event.date())).thenReturn(true);

        Response<ByteString> actual_response = test_group_resource.editEvent(ctx_test);

        assertEquals(200, actual_response.status().code());
    }

    @Test
    public void deleteEvent() throws Exception {
        when(request_test.payload()).thenReturn(Optional.of(ByteString.of(object_mapper.writeValueAsBytes(test_event))));

        when(event_store.getEvent(String.valueOf(test_event.id()))).thenReturn(test_event);

        when(store.deleteEvent(String.valueOf(test_event.id()))).thenReturn(true);

        Response<ByteString> actual_response = test_group_resource.deleteEvent(ctx_test);

        assertEquals(200, actual_response.status().code());
    }

    @Test
    public void updateAdmins() throws Exception {
        when(store.getGroup(String.valueOf(test_group.gid()))).thenReturn(test_group);

        when(request_test.payload()).thenReturn(Optional.of(ByteString.of(object_mapper.writeValueAsBytes(ImmutableMap
                .of("user_id", test_user.uid(), "make_admin", 1)))));

        when(store.updateAdmins(String.valueOf(test_group.gid()), String.valueOf(test_user.uid()), 1))
                .thenReturn(true);

        Response<ByteString> actual_response = test_group_resource.updateAdmins (ctx_test);

        assertEquals(200, actual_response.status().code());
    }

    @Test
    public void viewContacts() {
        when(store.getUsers(String.valueOf(test_group.gid()))).thenReturn(Collections.singletonList(test_user));

        Response<List<User>> actual_response = test_group_resource.viewContacts(ctx_test);

        assertEquals(Collections.singletonList(test_user), actual_response.payload().get());
    }
}

