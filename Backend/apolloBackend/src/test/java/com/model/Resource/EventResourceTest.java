package com.model.Resource;

import com.Resource.EventResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.*;
import com.spotify.apollo.Request;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.store.EventStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(MockitoJUnitRunner.class)
public class EventResourceTest {


    @Mock EventStore store;                     /* mock of the store class (interacting with db)                */
    @Mock Request request_test;                 /* mock for the Request object recieved from front-end          */
    @Mock RequestContext ctx_test;              /* mock of the request context that contains the request above  */
    @Mock ObjectMapper object_mapper;           /* mock of an object mapper - cleans the testing                */

    private EventResource test_event_resource;  /* the actual class we are testing - so obv shouldn't be mocked */
    private User test_user;                     /* a (real) user object that we will often use in our tests     */
    private Group test_group;                   /* a (real) group object that we will use in our tests          */
    private Event test_event;                   /* a (real) event object that we will use in our tests          */


    @Before
    public void setup() {
        test_event_resource = new EventResource(object_mapper, store);
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
                .gid(test_group.id())
                .date("someDate")
                .description("this is a test event")
                .location("test place")
                .build();

        when(ctx_test.pathArgs()).thenReturn(Collections.singletonMap("id", String.valueOf(test_event.gid())));
    }


    /*
     * Each @Test function is called the same name as the method that it calls and tests in the UserResource class.
     *
     * Todo: fix up this test.
     */
    @Test
    public void getUsers() {
        when(store.getUsers(String.valueOf(test_event.id()), String.valueOf(test_user.uid())))
                .thenReturn(Collections.singletonList(test_user));

        String cookie_id = "USER_SESSION=1";
//        when(request_test.headers().get("Cookie")).thenReturn(cookie_id);

        Response<List<User>> actual_response = test_event_resource.getUsers(ctx_test);

//        assertEquals(Collections.singletonList(test_user), actual_response.payload().get());
    }
}
