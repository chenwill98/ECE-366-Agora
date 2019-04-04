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
        PreparedStatement stmt = null;
        ResultSet result_set = null;

        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("select * from user where username = ?");
            stmt.setString(1, usr);
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
            stmt = connection.prepareStatement("select email, passhash, firstname, lastname from users where uid = ?");
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
                        .email(result_set.getString("email"))
                        .pass_hash(result_set.getString("passhash"))
                        .first_name(result_set.getString("firstname"))
                        .last_name(result_set.getString("lastname"))
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
            stmt = connection.prepareStatement("select uid, email, passhash, firstname, lastname from users where email = ?");
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
                        .uid(result_set.getInt("uid"))
                        .email(result_set.getString("email"))
                        .pass_hash(result_set.getString("passhash"))
                        .first_name(result_set.getString("firstname"))
                        .last_name(result_set.getString("lastname"))
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
            stmt = connection.prepareStatement("update users set passhash = (?) where uid = (?)");
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
            stmt = connection.prepareStatement("select uid from users where email = ?");
            stmt.setString(1, new_user.email());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // execute the sql to confirm no such user already exists
        try {
            result_set = stmt.executeQuery();
            if (!result_set.next())
                return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }


        // prepare the sql statement
        try {
            stmt = connection.prepareStatement("insert into users (email, passhash, firstname, lastname) values (?, ?, ?, ?)");
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
            stmt = connection.prepareStatement("insert into `groups` (Name, Description) values (?, ?)");
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

        // get the group id of the group we just created
        try {
            stmt = connection.prepareStatement("select gid from `groups` where Name = ?");
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
            stmt = connection.prepareStatement("insert into group_memberships (users_uid, groups_gid, is_admin) values (?, ?, ?)");
            stmt.setString(1, user_id);
            stmt.setString(2, result_set.getString("gid"));
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

    public boolean userLeaveGroup(String userid, String groupname) {

        PreparedStatement stmt = null;
        ResultSet  result_set;

        // get the group id of the group
        try {
            stmt = connection.prepareStatement("select gid from `groups` where Name = ?");
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
            stmt = connection.prepareStatement("delete from group_memberships where users_uid = ? AND groups_gid =  ?");
            stmt.setString(1, userid);
            stmt.setString(2, result_set.getString("gid"));
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
     * @param event_name The name of the event the user is joining.
     * @param is_attending a 1, 2, or 3. 1:attending \ 2:maybe \ 3:not attending.
     *
     * @return boolean - true on success, else false.
     */
    public boolean userJoinEvent(String user_id, String event_name, int is_attending) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet  result_set;

        // get the event id of the event name
        try {
            stmt = connection.prepareStatement("select eventid from events where event_name = ?");
            stmt.setString(1, event_name);
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

        // connect user and event
        try {
            result_set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String event_id = result_set.getString("eventid");

        // check that user isn't already rsvp-ed to the event.
        try {
            stmt = connection.prepareStatement( "select users_uid, events_eventid, is_attending from event_attendance" +
                                                " where users_uid = ? and events_eventid = ?");
            stmt.setString(1, user_id);
            stmt.setString(2, event_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            result_set = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        if (result_set.next()) {
            // update rsvp
            try {
                stmt = connection.prepareStatement( "update event_attendance set is_attending = 1 " +
                                                    "where users_uid = ? AND events_eventid=  ?");
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
        else {  // create new relationship.
            try {
                stmt = connection.prepareStatement( "insert into event_attendance (users_uid, events_eventid, is_attending)" +
                        " values (?, ?, ?)");
                stmt.setString(1, user_id);
                stmt.setString(2, result_set.getString("eventid"));
                stmt.setInt(3, is_attending);
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
    }


    /**
     * userLeaveEvent - disconnects between a user and an event. Doesn't delete the connection between the two,
     * rather just updates the status of the user as NOT attending.
     *
     * @param user_id The user in question.
     * @param event_name The event name in question.
     *
     * @return boolean - true on success, else false.
     */
    public boolean userLeaveEvent(String user_id, String event_name) {

        PreparedStatement stmt = null;
        ResultSet  result_set;

        // get the event id of the event
        try {
            stmt = connection.prepareStatement("select eventid from events where event_name = ?");
            stmt.setString(1, event_name);
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

        // disconnect between user and event
        try {
            result_set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            stmt = connection.prepareStatement( "update event_attendance set is_attending = 3 " +
                                                "where users_uid = ? AND events_eventid=  ?");
            stmt.setString(1, user_id);
            stmt.setString(2, result_set.getString("eventid"));
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
            stmt = connection.prepareStatement( "select G.gid, G.Name, G.description from group_memberships GM " +
                                                "inner join users U on U.uid = GM.users_uid " +
                                                "inner join `groups` G on G.gid = GM.groups_gid" +
                                                " where U.uid = ?");
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
                        .gid(result_set.getInt("gid"))
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
            stmt = connection.prepareStatement( "select E.eventid, E.event_name, E.desc, E.Location, E.Date_Time" +
                                                "from event_attendance EA" +
                                                "inner join users U on U.uid = EA.users_uid " +
                                                "inner join events E on E.eventid = EA.events_eventid" +
                                                " where U.uid = ?");
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
                        .id(result_set.getInt("eventid"))
                        .name(result_set.getString("event_name"))
                        .description(result_set.getString("desc"))
                        .location(result_set.getString("Location"))
                        .date(result_set.getString("Date_Time"))
                        .build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }
}
