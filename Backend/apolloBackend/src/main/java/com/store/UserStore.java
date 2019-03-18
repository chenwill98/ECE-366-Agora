package com.store;

import com.model.User;
import com.typesafe.config.Config;

import java.sql.*;


/**
 * UserStore - the user endpoint that interacts with the mysql User table.
 */
public class UserStore {

    /* fields */
    private final Connection connection;


    /* methods */
    /**
     * UserStore - The constructor of UserStore. This function sets up the
     * mysql connection based on the inputted configuration and throws a
     * runtime exception on error.
     *
     * @param config A Config class that includes the mysql database location.
     */
    public UserStore(final Config config) {

        // register the database


        // try connecting to the database
        try {
            this.connection = DriverManager.getConnection(config.getString("mysql.jdbc"), "guy", "");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * getUser - Gets a user object from the database. For now simply a proof-of-concept
     * by sending a query to the database, getting the result back from the database, and
     * printing it out.
     *
     * @param usr - The username of the user to retrieve from the db.
     *
     * @return A User object of the inputted username.
     */
    public User getUser(final String usr) {
        PreparedStatement find_user = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
                String concat ="select * from user where username ='" + usr + "'";
            find_user = connection.prepareStatement(concat);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            result_set = find_user.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //check the ResultSet
        User test_user = null;
        try {
            while (result_set.next()) {
                test_user = new User(result_set.getString("username"), result_set.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return test_user;
    }
}
