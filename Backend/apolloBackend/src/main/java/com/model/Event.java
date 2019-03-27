package com.model;


import io.norberg.automatter.AutoMatter;

/**
 * Event - A class for the event datatype.
 */
@AutoMatter
public interface Event{
    int Eventid();
    String Name();
    String Desc();
    int gid();
    String Location();
    String Time();
}
