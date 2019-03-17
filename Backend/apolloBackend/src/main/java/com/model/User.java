package com.model;


/**
 * User - A class for the user datatype.
 */
public class User {


    /* Fields */
    private String username;
    private String pass_hash;


    /* Methods */

    /**
     * User - A constructor for users. Just initializes initial values.
     *
     * @param usr - the username
     * @param pw  - the password hash
     */
    public User(String usr, String pw) {
        this.username = usr;
        this.pass_hash = pw;
    }


    /**
     * printUser - A function used for testing that simply prints the fields of the user.
     */
    public void printUser() {
        System.out.println(String.format("%s - %s\n", this.username, this.pass_hash));
    }
}
