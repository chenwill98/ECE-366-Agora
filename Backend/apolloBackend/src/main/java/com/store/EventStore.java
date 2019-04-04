package com.store;

import com.model.Event;
import com.model.EventBuilder;
import com.model.User;
import com.model.UserBuilder;
import com.typesafe.config.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EventStore - the events endpoint that interacts with the mysql events table.
 */
public class EventStore {

    /* fields */
    private final Connection connection;


    /* methods */

    /**
     * EventStore - The constructor of EventStore. This function sets up the
     * mysql connection based on the inputted configuration and throws a
     * runtime exception on error.
     *
     * @param config A Config class that includes the mysql database location.
     */
    public EventStore(final Config config) {


        // try connecting to the database
        try {
            this.connection = DriverManager.getConnection(config.getString("mysql.jdbc"), "guy", "");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * getEvent - Retrives an event with the specified name from the db.
     *
     * @param event_id The name of the event.
     *
     * @return The retrieved event. Returns a null object on error/ if not located.
     */
    public Event getEvent(String event_id) {

        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select event_name, `desc`, groups_gid, Location, Date_Time from events where eventid = ?");
            stmt.setString(1, event_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            result_set = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //check the ResultSet
        Event event = null;

        try {
            while (result_set.next()) {
                event = new EventBuilder()
                        .id(Integer.valueOf(event_id))
                        .name(result_set.getString("event_name"))
                        .description(result_set.getString("desc"))
                        .gid(result_set.getInt("groups_gid"))
                        .location(result_set.getString("Location"))
                        .date(result_set.getString("Date_Time"))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return event;
    }


    /**
     * isAdmin - This method determins if a user is an admin of the group that owns a specified event.
     *
     * @param user_id The id of the user in question.
     * @param event_id The id of the event in question.
     *
     * @return boolean - True if the user is an admin, else false.
     */
    public boolean isAdmin(String user_id, String event_id) {

        Event event = getEvent(event_id);
        if (event == null)
            return false;

        PreparedStatement stmt = null;
        ResultSet result_set;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select is_admin from group_memberships where" +
                    "users_uid = ? and groups_gid =?");
            stmt.setInt(1, Integer.valueOf(user_id));
            stmt.setInt(2, event.gid());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            result_set = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try {
            result_set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            return result_set.getBoolean("is_admin");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * getUsers - Get the users that are subscribed to an event.
     *
     * @param id The id of the event.
     *
     * @return A list of user objects with first names, last names, and emails.
     */
    public List<User> getUsers(String id) {
        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement( "select U.uid, U.firstname, U.lastname, U.email from event_attendance EA " +
                                                "inner join users U on U.uid = EA.users_uid " +
                                                "inner join events E on E.gid = EA.events_eventid" +
                                                " where G.gid = ?");
            stmt.setString(1, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            result_set = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //check the ResultSet
        List<User> users = new ArrayList<>();

        try {
            while (result_set.next()) {
                users.add(new UserBuilder()
                        .uid(result_set.getInt("uid"))
                        .first_name(result_set.getString("firstname"))
                        .last_name(result_set.getString("lastname"))
                        .email(result_set.getString("email"))
                        .pass_hash("")
                        .build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}



