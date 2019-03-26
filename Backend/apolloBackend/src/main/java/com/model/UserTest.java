package com.model;


import io.norberg.automatter.AutoMatter;

/**
 * UserTest - A class for testing a test-user class & functionality.
 */
@AutoMatter
public interface UserTest {

    String Username();
    String PassHash();
}
