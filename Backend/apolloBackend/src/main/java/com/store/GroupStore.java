package com.store;

import com.model.*;
import com.typesafe.config.Config;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GroupStore - the group endpoint that interacts with the mysql Group table.
 */
public class GroupStore {

    /* fields */
    private final Connection connection;


    /* methods */

    /**
     * GroupStore - The constructor of UserStore. This function sets up the
     * mysql connection based on the inputted configuration and throws a
     * runtime exception on error.
     *
     * @param config A Config class that includes the mysql database location.
     */
    public GroupStore(final Config config) {


        // try connecting to the database
        try {
            this.connection = DriverManager.getConnection(config.getString("mysql.jdbc"),
                    config.getString("mysql.username"), config.getString("mysql.password"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * getGroup - Tries to fetch the group with the given name.
     *
     * @param name The name of the group being searched for. This name is a unique identifier for the group.
     *
     * @return The group if it exists, otherwise null.
     */
    public Group getGroup(String name) {
        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select Name, Description from Groops where Name = ?");
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
        Group group = null;

        try {
            while (result_set.next()) {
                group = new GroupBuilder()
                        .name(result_set.getString("Name"))
                        .description(result_set.getString("Description"))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return group;
    }

    /**
     * getGroup - Tries to fetch the group with the given name.
     *
     * @param id The name of the group being searched for. This name is a unique identifier for the group.
     *
     * @return The group if it exists, otherwise null.
     */
    public Group getGroupByID(String id) {
        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select Name, Description from Groops where Groop_id = ?");
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
        Group group = null;

        try {
            while (result_set.next()) {
                group = new GroupBuilder()
                        .name(result_set.getString("Name"))
                        .description(result_set.getString("Description"))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return group;
    }

    /**
     * getUsersAdmin - Gets the list of users who are members of a group.
     *
     * @param id The ID of the group we are interested in.
     *
     * @return A list of users.
     */
    public List<User> getUsers(String id) {

        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement( "select U.User_id, U.First_name, U.Last_name, U.Email from Groop_memberships GM " +
                                                "inner join Users U on U.User_id = GM.User_id " +
                                                "inner join Groops G on G.Groop_id = GM.Groop_id" +
                                                " where G.Groop_id = ?");
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
                        .uid(result_set.getInt("User_id"))
                        .first_name(result_set.getString("First_name"))
                        .last_name(result_set.getString("Last_name"))
                        .email(result_set.getString("Email"))
                        .pass_hash("")
                        .build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }



    /**
     * createEvent - Adds an event to the database.
     *
     * @param new_event The new event that is being added to the db.
     *
     * @return boolean - true on success, else false.
     */
    public boolean createEvent(Event new_event) {

        PreparedStatement stmt = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement( "insert into Events (Event_name, Description, Groop_id, Location, Date_time)" +
                                                "values (?, ?, ?, ?, ?)");
            stmt.setString(1, new_event.name());
            stmt.setString(2, new_event.description());
            stmt.setInt(3, new_event.id());
            stmt.setString(4, new_event.location());
            stmt.setString(5, new_event.date());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * isAdmin - Checks if a user is an admin of a group
     *
     * @param user_id The id of the user.
     * @param group_id The id of the group.
     *
     * @return Boolean: True if the user is an admin, False else.
     */
    public boolean isAdmin(String user_id, String group_id) {

        PreparedStatement stmt = null;
        ResultSet result_set;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement( "select Is_admin from Groop_memberships where" +
                                                "User_id = ? and Groop_id =?");
            stmt.setString(1, user_id);
            stmt.setString(2, group_id);
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
     * updateEventName - Updates the name of an event (the event is already confirmed to exist.
     *
     * @param event_id The id of the event
     * @param new_name the name the event should change into
     *
     * @return  boolean: True on sucess, else false
     */
    public Boolean updateEventName(String event_id, String new_name) {
        PreparedStatement stmt = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement( "update Events set Event_name = ? where Event_id = ?");
            stmt.setString(1, new_name);
            stmt.setString(2, event_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * updateEventDescription - Updates the name of an event in the database
     * @param event_id The id of the event.
     * @param new_description The new description the event should have.
     *
     * @return boolean - true on sucess, else false.
     */
    public Boolean updateEventDescription(String event_id, String new_description) {
        PreparedStatement stmt = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement( "update Events set Description = ? where Event_id = ?");
            stmt.setString(1, new_description);
            stmt.setString(2, event_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * updateEventLocation - Updates the location of an event in the database. This event was already confirmed to
     * exist.
     *
     * @param event_id The id of the event.
     * @param new_location The new locaiton the event is being held at.
     *
     * @return boolean - true on sucess, else false
     */
    public Boolean updateEventLocation(String event_id, String new_location) {
        PreparedStatement stmt = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement( "update Events set Location = ? where Event_id = ?");
            stmt.setString(1, new_location);
            stmt.setString(2, event_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * updateEventDate - Updates the date an event is taking place in the database. This event was already confirmed
     * to exist.
     *
     * @param event_id The id of the event.
     * @param new_date The new date the event is happening on.
     *
     * @return boolean - true on success, else false.
     */
    public Boolean updateEventDate(String event_id, String new_date) {
        PreparedStatement stmt = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement( "update Events set Date_time = ? where Event_id = ?");
            stmt.setString(1, new_date);
            stmt.setString(2, event_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * deleteEvent - deletes an event from the database. This event was already confirmed to exist.
     * Deleting an event automatically deletes all its related entries in the event_membership table also.
     *
     * @param event_id The id of the event.
     *
     * @return boolean - true on success, else false.
     */
    public boolean deleteEvent(String event_id) {

        PreparedStatement stmt = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement( "delete from Events where Event_id = ?");
            stmt.setString(1, event_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * updateAdmins - Change the admin status of a user for a group.
     *
     * @param group_id The id of the group.
     * @param user_id The id of the user.
     * @param make_admin Make the user an: 1=admin, 0=regular user.
     *
     * @return boolean - true on success, else false.
     */
    public boolean updateAdmins(String group_id, String user_id, Integer make_admin) {
        PreparedStatement stmt = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement( "update Groop_memberships set Is_admin = ? where " +
                                                "User_id = ? and Groop_id= ?");
            stmt.setInt(1, make_admin);
            stmt.setString(2, user_id);
            stmt.setString(3, group_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /* todo: add comments */
    public List<Event> getEvents(String id) {
        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select Event_name, Event_id from Events where Groop_id = ?");
            stmt.setInt(1, Integer.valueOf(id));
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
        List<Event> events = new ArrayList<>();

        try {
            while (result_set.next()) {
                events.add(new EventBuilder()
                        .id(Integer.valueOf(result_set.getString("Event_id")))
                        .name(result_set.getString("Event_name"))
                        .description("")
                        .date("")
                        .location("")
                        .build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return events;
    }


    public List<Group> getGroups() {
        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select * from Groops");
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
        List<Group> groups= new ArrayList<>();

        try {
            while (result_set.next()) {
                groups.add(new GroupBuilder()
                        .id(Integer.valueOf(result_set.getString("Groop_id")))
                        .name(result_set.getString("Name"))
                        .description(result_set.getString("Description"))
                        .build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return groups;
    }

}