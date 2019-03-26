package com.model;


import io.norberg.automatter.AutoMatter;

/**
 * User - A class for the user datatype.
 */
@AutoMatter
public interface User {
    int uid();
    String First_Name();
    String Last_Name();
    String PassHash();
    String Email();
}
