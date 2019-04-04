# Agora

This is an event sharing platform built for Cooper Union's ECE-366: Software Engineering.

## Team Members:
* Benjamin Kaplan
* Will Chen
* Rayhan Syed
* Guy Bar Yosef
* Josh Go


### Backend Setup

#### HTTP Server

We use [Spotify Apollo](https://github.com/spotify/apollo) to set up our API service.
We use maven to manage our dependencies, and as such one could open our project and get all our dependencies from [/APPLICATION/FOLDER/Backend/apolloBackend/pom.xml](https://github.com/chenwill98/ECE-366-Agora/blob/master/Backend/apolloBackend/pom.xml).

To build our HTTP Server as a *jar* file: `mnv package`. The *jar* file will appear as */APPLICATION/FOLDER/apollo-backend.jar*.

The default port that our HTTP server is listening on is 8080. This can be changed in the file [/APPLICATION/FOLDER/Backend/apolloBackend/src/main/resources/apolloBackend.conf](https://github.com/chenwill98/ECE-366-Agora/blob/master/Backend/apolloBackend/src/main/resources/apolloBackend.conf), or one can add the argument `HTTP_PORT=9000` in the maven command to have the server listen to port, in this case, 9000.


#### MySQL Server

We use MySQL for our storage. To run this server locally, after MySQL is installed one can create a database with the correct tables and fields using the command:
```
mysql database_name < /APPLICATION/FOLDER/Backend/MySQL/Database_Creation.SQL
```
where *database_name* will be the name of the database.

Note that one will have to update config file located at [/APPLICATION/FOLDER/Backend/apolloBackend/src/main/resources/apolloBackend.conf](https://github.com/chenwill98/ECE-366-Agora/blob/master/Backend/apolloBackend/src/main/resources/apolloBackend.conf) for their own username, password, and sql database location.
