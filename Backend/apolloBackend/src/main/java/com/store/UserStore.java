package com.store;

import com.model.User;
import com.typesafe.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


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

        // try connecting to the database
        try {
            this.connection = DriverManager.getConnection(config.getString("mysql.jdbc"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * getUser - Gets a user object from the database.
     *
     * @param usr - The username of the user to retrieve from the db.
     *
     * @return A User object of the inputted username.
     */
    public User getUser(final String usr) {

        // TODO: The three steps below. For now there is a test dummy below.
        // prepare the sql statement
        try {
            PreparedStatement find_user = connection.prepareStatement("select * from user where id =" + usr);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        //check the ResultSet


        // temporary dummy return User
        User test_user = new User("test-user", "password123");
        return test_user;
    }
}
