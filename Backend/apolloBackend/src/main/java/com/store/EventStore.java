package com.store;

import com.model.Event;
import com.model.EventBuilder;
import com.typesafe.config.Config;

import java.sql.*;

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
     * @param name The name of the event.
     *
     * @return The retrieved event. Returns a null object on error/ if not located.
     */
    public Event getEvent(String name) {

        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select eventid, `desc`, groups_gid, Location, Date_Time from events where event_name = ?");
            stmt.setString(1, name);
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
                        .name(name)
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
}



