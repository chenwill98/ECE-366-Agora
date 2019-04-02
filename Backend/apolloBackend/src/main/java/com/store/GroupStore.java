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
            this.connection = DriverManager.getConnection(config.getString("mysql.jdbc"), "guy", "");
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
            stmt = connection.prepareStatement("select Name, Description from `groups` where Name = ?");
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
     * getUsers - Gets the list of users who are members of a group.
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
            stmt = connection.prepareStatement( "select U.uid, U.firstname, U.lastname from group_memberships GM " +
                                                "inner join users U on U.uid = GM.users_uid " +
                                                "inner join `groups` G on G.gid = GM.groups_gid" +
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
                        .pass_hash("")
                        .email("")
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
     * @param gid The group id of the group that created the event.
     * @param new_event The new event that is being added to the db.
     *
     * @return boolean - true on success, else false.
     */
    public boolean createEvent(String gid, Event new_event) {

        PreparedStatement stmt = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement( "insert into events (event_name, `desc`, groups_gid, Location, Date_Time)" +
                                                "values (?, ?, ?, ?, ?)");
            stmt.setString(1, new_event.name());
            stmt.setString(2, new_event.description());
            stmt.setInt(3, new_event.gid());
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
            stmt = connection.prepareStatement( "select is_admin from group_memberships where" +
                                                "users_uid = ? and groups_gid =?");
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
            return result_set.getBoolean("is_admin");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}