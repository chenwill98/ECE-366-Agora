package com.store;

import com.model.Group;
import com.model.GroupBuilder;
import com.typesafe.config.Config;

import java.sql.*;

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
}