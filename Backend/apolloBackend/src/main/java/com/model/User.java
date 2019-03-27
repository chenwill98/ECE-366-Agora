package com.model;


import io.norberg.automatter.AutoMatter;

/**
 * User - A class for the user datatype.
 */
@AutoMatter
public interface User {
    int uid();
    String first_name();
    String last_name();
    String pass_hash();
    String email();
}
