package com.model;


import io.norberg.automatter.AutoMatter;

/**
 * Event - A class for the event datatype.
 */
@AutoMatter
public interface Event{
    int id();
    String name();
    String description();
    int gid();
    String location();
    String date();
}
