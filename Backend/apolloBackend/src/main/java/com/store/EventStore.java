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
            this.connection = DriverManager.getConnection(config.getString("mysql.jdbc"),
                    config.getString("mysql.username"), config.getString("mysql.password"));
        }
        catch (SQLException e) {
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
            stmt = connection.prepareStatement("select Event_name, Description, Groop_id, Location, Date_time from Events where Event_id = ?");
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
                        .name(result_set.getString("Event_name"))
                        .description(result_set.getString("Description"))
                        .gid(result_set.getInt("Groop_id"))
                        .location(result_set.getString("Location"))
                        .date(result_set.getString("Date_time"))
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

        if (user_id == null || event_id == null || user_id.isEmpty() || event_id.isEmpty())
            return false;

        Event event = getEvent(event_id);
        if (event == null)
            return false;

        PreparedStatement stmt = null;
        ResultSet result_set;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select Is_admin from Groop_memberships where" +
                    "User_id = ? and Group_id =?");
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
            return result_set.getBoolean("Is_admin");
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
    public List<User> getUsers(String id, String user_id) {
        PreparedStatement stmt = null;
        ResultSet result_set = null;


        Boolean is_admin = isAdmin(user_id, id);

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement( "select U.User_id, U.First_name, U.Last_name, U.Email " +
                                                "from Event_attendance EA " +
                                                "inner join Users U on U.User_id = EA.User_id " +
                                                "inner join Events E on E.Event_id = EA.Event_id" +
                                                " where E.Event_id = ? and EA.Is_attending = 'YES'");
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
                if (isAdmin(user_id, id)) {
                    users.add(new UserBuilder()
                            .uid(result_set.getInt("User_id"))
                            .first_name(result_set.getString("First_name"))
                            .last_name(result_set.getString("Last_name"))
                            .email(result_set.getString("Email"))
                            .pass_hash("")
                            .build());
                }
                else {
                    users.add(new UserBuilder()
                            .uid(result_set.getInt("User_id"))
                            .first_name(result_set.getString("First_name"))
                            .last_name(result_set.getString("Last_name"))
                            .email("")
                            .pass_hash("")
                            .build());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }


    /**
     * getEvents - Get all events from a database.
     *
     * @return  A list of events.
     */
    public List<Event> getEvents() {
        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select * from Events");
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
        List<Event> events= new ArrayList<>();

        try {
            while (result_set.next()) {
                events.add(new EventBuilder()
                        .id(Integer.valueOf(result_set.getString("Event_id")))
                        .name(result_set.getString("Event_name"))
                        .description(result_set.getString("Description"))
                        .gid(result_set.getInt("Groop_id"))
                        .location(result_set.getString("Location"))
                        .date(result_set.getString("Date_time"))
                        .build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return events;
    }
}



