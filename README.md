# Agora

This is an event sharing platform built for Cooper Union's ECE-366: Software Engineering.

### Team Members:
* Benjamin Kaplan
* Will Chen
* Rayhan Syed
* Guy Bar Yosef
* Josh Go

## Setup

### Block Diagram
![Block Diagram Picture](./diagrams/AgoraBlockDiagram.png)

### BackEnd Setup

Our BackEnd consists of restful API services along with a database.

#### Services

We use [Spotify Apollo](https://github.com/spotify/apollo) to set up our API services.
We use maven to manage our dependencies, and so to open our project and get all needed dependencies open the file [/APPLICATION/FOLDER/Backend/apolloBackend/pom.xml](https://github.com/chenwill98/ECE-366-Agora/blob/master/Backend/apolloBackend/pom.xml).

To build our HTTP Server as a *jar* file simply call `mnv package`. The *jar* file will appear in */APPLICATION/FOLDER/apollo-backend.jar*.

The default port that our HTTP server is listening on is 8080. This can be changed in two ways. One is in the file [/APPLICATION/FOLDER/Backend/apolloBackend/src/main/resources/apolloBackend.conf](https://github.com/chenwill98/ECE-366-Agora/blob/master/Backend/apolloBackend/src/main/resources/apolloBackend.conf), changing the default port. The second way is to choose the port every time you run the server, adding the argument `HTTP_PORT=9000` into the maven command.


#### MySQL Server

We use MySQL as our database. Currently in our stage in development, we only run our database locally. To run our database locally, after MySQL is installed one can create a database with the correct tables and fields using the command:
```
mysql database_name < /APPLICATION/FOLDER/Backend/MySQL/Database_Creation.SQL
```
where *database_name* will be the name of the created database and */APPLICATION/FOLDER/Backend/MySQL/Database_Creation.SQL* is the file containing the database schemas.

Note that to run the database locally you need to update config file located at [/APPLICATION/FOLDER/Backend/apolloBackend/src/main/resources/apolloBackend.conf](https://github.com/chenwill98/ECE-366-Agora/blob/master/Backend/apolloBackend/src/main/resources/apolloBackend.conf) with your username, password, and sql database location.


