package com.model;


import io.norberg.automatter.AutoMatter;

/**
 * Group - A class for the group datatype.
 */
@AutoMatter
public interface Group {
    int gid();
    String name();
    String description();
}
