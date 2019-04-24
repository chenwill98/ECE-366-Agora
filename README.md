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

### Front end Setup

Our front end uses React to build the UI, and we use Yarn to manage our dependencies.

To start the app in development mode, we first need to install all the dependencies using the command:
```
yarn install
```
After installing the dependencies, we can start the app in development mode using the command:
```
yarn start
```
The app should start, and to access it simply go to `http://localhost:3000` if it doesn't automatically direct you there.

### Back end Setup

Our back end consists of restful API services along with a database.

#### Services

We use [Spotify Apollo](https://github.com/spotify/apollo) to set up our API services.
We use maven to manage our dependencies, and so to open our project and get all needed dependencies open the file [/APPLICATION/FOLDER/Backend/apolloBackend/pom.xml](https://github.com/chenwill98/ECE-366-Agora/blob/master/Backend/apolloBackend/pom.xml).

To build our HTTP Server as a *jar* file simply call `mnv package`. The *jar* file will appear in */APPLICATION/FOLDER/apollo-backend.jar*.

The default port that our HTTP server is listening on is 8080. This can be changed in two ways. One is in the file [/APPLICATION/FOLDER/Backend/apolloBackend/src/main/resources/apolloBackend.conf](https://github.com/chenwill98/ECE-366-Agora/blob/master/Backend/apolloBackend/src/main/resources/apolloBackend.conf), changing the default port. The second way is to choose the port every time you run the server, adding the argument `HTTP_PORT=9000` into the maven command.

To run a test in which one cleans up the previously generated *target* directory, do `mvn clean package`.

#### MySQL Server

We use MySQL as our database. Currently in our stage in development, we only run our database locally. To run our database locally, after MySQL is installed one can create a database with the correct tables and fields using the command:
```
mysql database_name < /APPLICATION/FOLDER/Backend/MySQL/Database_Creation.SQL
```
where *database_name* will be the name of the created database and */APPLICATION/FOLDER/Backend/MySQL/Database_Creation.SQL* is the file containing the database schemas.

Note that to run the database locally you need to update config file located at [/APPLICATION/FOLDER/Backend/apolloBackend/src/main/resources/apolloBackend.conf](https://github.com/chenwill98/ECE-366-Agora/blob/master/Backend/apolloBackend/src/main/resources/apolloBackend.conf) with your username, password, and sql database location.

#### NginX

We use [NginX](https://www.nginx.com/) to serve our static as well as act as a proxy server to the BackEnd. Our application has NginX configured to listen on port 8000 and it acts as a reverse proxy to `localhost:8080` which is where the BackEnd server is listening. These configurations can be changed in the *nginx.conf* configuration file. This file resides in different locations depending on OS- on linux it can be found in */etc/nginx/nginx.conf*.

To run NginX locally, first install it (on Ubuntu one could do `sudo apt get nginx`). One could then configure NginX using the configuration file describe in the paragraph above. To run our application, first produce a */build* folder for the react app using `yarn run build`. 

Next, set up a server block in the configuration file to look like so:
```
server {
    listen 8000;
    server_name localhost;
    root /PATH/TO/BUILD/DIRECTORY/build;
    index index.html;
    location / {
        try_files $uri /index.html;
    }
}
```
For more information on how to set up NginX, we recommend looking at the official [beginner's guide](http://nginx.org/en/docs/beginners_guide.html). A great (and brief) explination on the specific configuraiton required for react apps can be found [here](https://stackoverflow.com/questions/43555282/react-js-application-showing-404-not-found-in-nginx-server).

