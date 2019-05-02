package com.store;

import com.model.*;

import com.typesafe.config.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


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
            this.connection = DriverManager.getConnection(config.getString("mysql.jdbc"),
                    config.getString("mysql.username"), config.getString("mysql.password"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * getUserWithID - Gets a user from the MySQL database.
     *
     * @param user_id A string of the user ID of a user, a unique identifier for each user.
     *
     * @return A User object that contains the email, password hash, first name, and last name of the user with the
     * inputted email.
     */
    public User getUserWithID(String user_id) {

        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select Email, Passhash, First_name, Last_name from Users where User_id = ?");
            stmt.setString(1, user_id);
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
                        .uid(Integer.valueOf(user_id))
                        .email(result_set.getString("Email"))
                        .pass_hash(result_set.getString("Passhash"))
                        .first_name(result_set.getString("First_name"))
                        .last_name(result_set.getString("Last_name"))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }


    /**
     * getUserWithEmail - Gets a user from the MySQL database.
     *
     * @param email A string of the user ID of a user, a unique identifier for each user.
     *
     * @return A User object that contains the email, password hash, first name, and last name of the user with the
     * inputted email.
     */
    public User getUserWithEmail(String email) {

        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select User_id, Email, Passhash, First_name, Last_name from Users where Email = ?");
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
                        .uid(result_set.getInt("User_id"))
                        .email(result_set.getString("Email"))
                        .pass_hash(result_set.getString("Passhash"))
                        .first_name(result_set.getString("First_name"))
                        .last_name(result_set.getString("Last_name"))
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
     * @param user_id The email of the user whos password will get updated.
     * @param new_pass The hash of the new password that will replace the old pass word of the inputted user.
     *
     * @return A boolean, true on success, else false.
     */
    public boolean updatePass(String user_id, String new_pass) {

        PreparedStatement stmt = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("update Users set Passhash = (?) where User_id = (?)");
            stmt.setString(1, new_pass);
            stmt.setString(2, user_id);
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
     * createUser - Creates a new User in the database.
     *
     * @param new_user The user object to be inserted into the database.
     *
     * @return boolean- True on success and false on error.
     */
    public boolean createUser(User new_user) {

        PreparedStatement stmt = null;
        ResultSet result_set;

        // prepare stmt to confirm that no user with the given email doesn't already exist
        try {
            stmt = connection.prepareStatement("select User_id from Users where Email = ?");
            stmt.setString(1, new_user.email());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql to confirm no such user already exists
        try {
            result_set = stmt.executeQuery();

            if (result_set.next()) {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("insert into Users (Email, Passhash, First_name, Last_name) " +
                                                "values (?, ?, ?, ?)");
            stmt.setString(1, new_user.email());
            stmt.setString(2, new_user.pass_hash());
            stmt.setString(3, new_user.first_name());
            stmt.setString(4, new_user.last_name());
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
     * createGroup - Creates a new group and inserts it into the group database. Automatically makes the user who
     * created it a member and admin.
     *
     * @param user_id The user who created the group.
     * @param new_group The new group that gets added to the groups db.
     *
     * @return boolean - True of success, else false.
     */
    public boolean createGroup(String user_id, Group new_group) {

        PreparedStatement stmt = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("insert into Groops (Name, Description) values (?, ?)");
            stmt.setString(1, new_group.name());
            stmt.setString(2, new_group.description());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql
        try {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // add the creator as a member and admin of the group
        return userJoinGroup(user_id, new_group.name(), 1);
    }


    /**
     * userJoinGroup - Joins a user to a group.
     *
     * @param user_id The unique ID of the user who is joining the group.
     * @param group_name The name of the group that the user is joining.
     * @param is_admin int (0 or 1) where 0 means the user is NOT an amdin and 1 means the user IS an admin.
     *
     * @return boolean - true on success, else false.
     */
    public boolean userJoinGroup(String user_id, String group_name, int is_admin) {

        PreparedStatement stmt = null;
        ResultSet  result_set;

        // get the group id of the group goup name
        try {
            stmt = connection.prepareStatement("select Groop_id from Groops where Name = ?");
            stmt.setString(1, group_name);
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

        // add the user as a member of the group
        try {
            result_set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            stmt = connection.prepareStatement("replace into Groop_memberships (User_id, Groop_id, Is_admin) " +
                                                "values (?, ?, ?)");
            stmt.setString(1, user_id);
            stmt.setString(2, result_set.getString("Groop_id"));
            stmt.setInt(3, is_admin);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * userLeaveGroup - Changes the relationship of a user to a group to be not-belonging.
     *
     * @param userid The user id of the user.
     * @param groupname The groupname of the group.
     * @return boolean - true on success and false on error.
     */
    public boolean userLeaveGroup(String userid, String groupname) {

        PreparedStatement stmt = null;
        ResultSet  result_set;

        // get the group id of the group
        try {
            stmt = connection.prepareStatement("select Groop_id from Groops where Name = ?");
            stmt.setString(1, groupname);
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

        // disconnect between user and group
        try {
            result_set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            stmt = connection.prepareStatement("delete from Groop_memberships where User_id = ? AND Groop_id =  ?");
            stmt.setString(1, userid);
            stmt.setString(2, result_set.getString("Groop_id"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * userJoinEvent - Joins a user and an event together
     *
     * @param user_id The id of the user who is joining.
     * @param event_id The name of the event the user is joining.
     *
     * @return boolean - true on success, else false.
     */
    public boolean userJoinEvent(String user_id, String event_id) {
        PreparedStatement stmt = null;

        // update rsvp
        try {
            stmt = connection.prepareStatement( "replace into Event_attendance (Is_attending, User_id, Event_id) " +
                                            "values (1, ?, ?)");
            stmt.setString(1, user_id);
            stmt.setString(2, event_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * userLeaveEvent - disconnects between a user and an event. Doesn't delete the connection between the two,
     * rather just updates the status of the user as NOT attending.
     *
     * @param user_id The user in question.
     * @param event_id The event name in question.
     *
     * @return boolean - true on success, else false.
     */
    public boolean userLeaveEvent(String user_id, String event_id) {

        PreparedStatement stmt = null;


        try {
            stmt = connection.prepareStatement( "update Event_attendance set Is_attending = 3 " +
                                                "where User_id = ? AND Event_id =  ?");
            stmt.setString(1, user_id);
            stmt.setString(2, event_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * getGroups - Returns a list of the groups a user belongs to.
     *
     * @param user_id the id of the user.
     *
     * @return a list of groups the user belongs to.
     */
    public List<Group> getGroups(String user_id) {
        PreparedStatement stmt = null;
        ResultSet  result_set;

        try {
            stmt = connection.prepareStatement( "select G.Groop_id, G.Name, G.Description from Groop_memberships GM " +
                                                "inner join Users U on U.User_id = GM.User_id " +
                                                "inner join Groops G on G.Groop_id = GM.Groop_id" +
                                                " where U.User_id= ?");
            stmt.setString(1, user_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            result_set = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        List<Group> groups = new ArrayList<>();

        try {
            while (result_set.next()) {
                groups.add( new GroupBuilder()
                        .id(result_set.getInt("Groop_id"))
                        .name(result_set.getString("Name"))
                        .description(result_set.getString("Description"))
                        .build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }


    /**
     * getEvents - Returns a list of the events a user is associated with.
     *
     * @param user_id the id of the user.
     *
     * @return a list of events that the user is associated with.
     */
    public List<Event> getEvents(String user_id) {
        PreparedStatement stmt = null;
        ResultSet  result_set;

        try {
            stmt = connection.prepareStatement( "select E.Event_id, E.Event_name, E.Description, E.Location, " +
                    "E.DAte_time from Event_attendance EA " +
                    "inner join Users U on U.User_id = EA.User_id " +
                    "inner join Events E on E.Event_id = EA.Event_id " +
                    "where U.User_id = ? and EA.Is_attending = 1");
            stmt.setString(1, user_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            result_set = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        List<Event> events = new ArrayList<>();


        try {
            while (result_set.next()) {
                events.add( new EventBuilder()
                        .id(result_set.getInt("Event_id"))
                        .name(result_set.getString("Event_name"))
                        .description(result_set.getString("Description"))
                        .location(result_set.getString("Location"))
                        .date(result_set.getString("Date_time"))
                        .build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }


    /**
     * getUserEventAttendance - Get the attendance status of a user to an event.
     * @param user_id The user id.
     * @param event_id The event id.
     * @return Integer with value of: 1, 2, or 3. 1:attending \ 2:maybe \ 3:not attending.
     */
    public Integer getUserEventAttendance(String user_id, String event_id) {
        PreparedStatement stmt = null;
        ResultSet  result_set;

        try {
            stmt = connection.prepareStatement( "Select Is_attending from Event_attendance " +
                                                "where User_id = ? and Event_id = ?");
            stmt.setString(1, user_id);
            stmt.setString(2, event_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            result_set = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        String status = "NO";   /* we want default enum value to be "NO" */

        try {
            if (result_set.next()) {
                status = result_set.getString("Is_attending");
                if (status.equals("NO"))
                    return 3;
                else if (status.equals("YES"))
                    return 1;
                else
                    return 2;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 3;
    }
}
