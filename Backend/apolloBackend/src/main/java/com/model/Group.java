package com.model;


import io.norberg.automatter.AutoMatter;

/**
 * Group - A class for the group datatype.
 */
@AutoMatter
public interface Group {
    int id();
    String name();
    String description();
}
