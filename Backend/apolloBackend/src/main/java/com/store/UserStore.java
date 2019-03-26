package com.store;

import com.model.User;
import com.model.UserBuilder;
import com.model.UserTest;

import com.model.UserTestBuilder;
import com.typesafe.config.Config;

import java.sql.*;


/**
 * UserStore - the user endpoint that interacts with the mysql UserTest table.
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
            this.connection = DriverManager.getConnection(config.getString("mysql.jdbc"), "guy", "");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * getUserTest - Gets a user object from the database. For now simply a proof-of-concept
     * by sending a query to the database, getting the result back from the database, and
     * printing it out.
     *
     * @param usr - The username of the user to retrieve from the db.
     *
     * @return A UserTest object of the inputted username.
     */
    public UserTest getUserTest(final String usr) {
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
        UserTest test_user = null;
        try {
            while (result_set.next()) {
                test_user = new UserTestBuilder()
                        .Username(result_set.getString("username"))
                        .PassHash(result_set.getString("password"))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return test_user;
    }


    /**
     * getUser - Gets a user from the MySQL database.
     *
     * @param email A string of the email of a user, a unique identifier for each user.
     *
     * @return A User object that contains the email, password hash, first name, and last name of the user with the
     * inputted email.
     */
    public User getUser(String email) {

        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select email, passhash, firstname, lastname from users where email = ?");
            stmt.setString(1, email);
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
        User user = null;
        try {
            while (result_set.next()) {
                user = new UserBuilder()
                        .Email(result_set.getString("email"))
                        .PassHash(result_set.getString("passhash"))
                        .First_Name(result_set.getString("firstname"))
                        .Last_Name(result_set.getString("lastname"))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }


    /**
     * updatePass - Updates the password of a user. Of course, we are dealing with hashed passwords because we are
     * all about that security here in Agora. We aren't throwing no Facebook bullshit here.
     *
     * @param user_email The email of the user whos password will get updated.
     * @param new_pass The hash of the new password that will replace the old pass word of the inputted user.
     *
     * @return A boolean, true on success, else false.
     */
    public boolean updatePass(String user_email, String new_pass) {

        PreparedStatement stmt = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("update users set passhash = ? where email = ?");
            stmt.setString(1, new_pass);
            stmt.setString(2, user_email);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            return stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
