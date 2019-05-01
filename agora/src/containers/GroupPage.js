import React, { Component } from "react";
import axios from "axios";
import Navigation from '../components/Navigation.js';
import SingleObjectView from '../components/SingleObjectView.js';
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import {Backend_Route} from "../BackendRoute.js";
import Footer from "../components/Footer";

let init_get = {
    method: "Get",
    credentials: "include"
};


class GroupPage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: Backend_Route.ip,
            port: Backend_Route.port,

            // group related states
            group_id: this.props.match.params.group_id,
            group_description: "Description here",
            group_name: "Name here",
            group_users: [],
            group_events: [],

            // if in a user session
            user_id: localStorage.getItem('userID'),
            user_isAdmin: false,
            user_belongs: false,

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }


    //fetches all data when the component mounts
    componentDidMount () {

        /**
         * GET THE GROUP'S INFO
         */
        axios.get( `${this.state.ip}:${this.state.port}/group/${this.state.group_id}`)
            .then(res => {
                this.setState( {
                    group_description: res.data.description,
                    group_name: res.data.name
                });
                localStorage.setItem('groupID', this.state.group_id);
            })
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg: error.message
                });
                console.log("Error requesting group info: " + error.message);
            });


        this.getGroupUsers();

        /**
         * GET THE GROUP'S EVENTS
         */
        axios.get(`${this.state.ip}:${this.state.port}/group/${this.state.group_id}/get-events`)
            .then ( res => {
                console.log("Successfully got events.");
                this.setState( {
                    group_events: res.data
                });
            })
            .catch( error => {
                this.setState({
                    error: true,
                    error_msg: error.message
                });
                console.log("Error requesting get-events: " + error.message);
            });


        /**
         * CHECK IF USER IS AN ADMIN TO THE GROUP
         */
        if (this.state.user_id !== null) {
            fetch( `${this.state.ip}:${this.state.port}/group/${this.state.group_id}/is-admin/${this.state.user_id}`, init_get)
            .catch( error => {
                this.setState({
                    error: true,
                    error_msg: error.message
                });
                console.log("Error requesting is-admin: " + error.message);
            })
            .then(res => {
                res.json().then(data => ({
                        data: data,
                        status: res.status
                    })
                )
                    .then(res => {
                        if (res.status === 200) {
                            if (res.data === true) {
                                this.setState({
                                    user_isAdmin: true,
                                    user_belongs: true,
                                });
                                console.log("Successfully know whether admin:" + this.state.user_isAdmin);
                            }

                        } else {
                            this.setState({
                                error: true,
                                error_msg: "Error with message: " + res.status
                            });
                            console.log("Error requesting is-admin: " + res.status);
                        }
                    });
            });
            /**
             * CHECK IF USER BELONGS TO THE GROUP
             */
            if (this.state.user_belongs === false) {
                fetch(`${this.state.ip}:${this.state.port}/user/${this.state.user_id}/group/${this.state.group_id}`, init_get)
                .catch( error => {
                    this.setState({
                        error: true,
                        error_msg: error.message
                    });
                    console.log("Error requesting if user belongs in group: " + error.message);
                })
                .then(res => {
                    res.json().then(data => ({
                            data: data,
                            status: res.status
                    })).then(res => {
                        if (res.data === true) {
                            console.log("User belongs in group");
                            this.setState( {
                                user_belongs: true,
                            });
                        }
                    })
                });
            }
        }

        if (!this.state.intervalSet) {
            let interval = setInterval(this.getData, 1000);
            this.setState({intervalSet: interval})
        }
    }


    /**
     * getContactInfo - updates the group_users array to also include the user's emails.
     */
    getContactInfo() {
        // get the group's events
        fetch(`${this.state.ip}:${this.state.port}/group/${this.state.group_id}/view-contacts`, init_get)
        .catch( error => {
            this.setState({
                error: true,
                error_msg: error.message
            });
            console.log("Error requesting view-contacts: " + error.message);
        })
        .then(res => {
            console.log("status: " + res.status);

            res.json().then(data => ({
                    data: data,
                    status: res.status
                })
            ).then(res => {
                if (res.data !== '') {
                    this.setState( {
                        group_users: res.data
                    });
                }
            })
        });
    }


    /**
     * joinGroup - Makes a user join a group.
     */
    joinGroup() {
        fetch(`${this.state.ip}:${this.state.port}/user/${this.state.user_id}/join-group`,
        {
                method: "Post",
                credentials: "include",
                body: JSON.stringify({'groupname': this.state.group_name})
            }
        )
        .catch( error => {
            this.setState({
                error: true,
                error_msg: error.message
            });
            console.log("Error joining group: " + error.message);
        })
        .then(res => {
            if (res.status === 200) {
                this.setState( {
                    user_belongs: true
                });
                console.log("Successfully joined group.");
                this.getGroupUsers();
            }
            else {
                this.setState({
                    error: true,
                    error_msg: "Response: " + res.status
                });
                console.log("Error joining group, status:" + res.status);
            }
        });
    }


    /**
     * leaveGroup - Deletes the connection between a user and a group.
     */
    leaveGroup() {
        fetch(`${this.state.ip}:${this.state.port}/user/${this.state.user_id}/leave-group`,
            {
                method: "Post",
                credentials: "include",
                body: JSON.stringify({'groupname': this.state.group_name})
            }
        )
        .catch( error => {
            this.setState({
                error: true,
                error_msg: error.message
            });
            console.log("Error joining group: " + error.message);
        })
        .then(res => {
            if (res.status === 200) {
                this.setState( {
                    user_belongs: false
                });
                console.log("Successfully left group.");
                this.getGroupUsers();
            }
            else {
                this.setState({
                    error: true,
                    error_msg: "Response: " + res.status
                })
                console.log("Error leaving group.");
            }
        });
    }


    /**
     * GET THE GROUP'S USERS
     */
    getGroupUsers() {
        axios.get(`${this.state.ip}:${this.state.port}/group/${this.state.group_id}/get-users`)
            .then ( res => {
                console.log("Successfully got users.");
                this.setState( {
                    group_users: res.data
                });
            })
            .catch( error => {
                this.setState({
                    error: true,
                    error_msg: error.message
                });
                console.log("Error requesting get-users: " + error.message);
            });
    }


    /**
     * makeAdmin - Makes a user an admin.
     */
    makeAdmin(user2admin_id) {
        fetch(`${this.state.ip}:${this.state.port}/group/${this.state.group_id}/update-admins`,
            {
                method: "Post",
                credentials: "include",
                body: JSON.stringify({'user_id': user2admin_id, 'make_admin': 1})
            }
        )
        .catch( error => {
            this.setState({
                error: true,
                error_msg: error.message
            });
            console.log("Error joining group: " + error.message);
        })
        .then(res => {
            if (res.status === 200) {
                console.log("Successfully made someone an admin.");
            }
            else {
                console.log("Failed to make someone an admin.");
            }
        });
    }


    //kills the process
    componentWillUnmount() {
        if (this.state.intervalIsSet) {
            clearInterval(this.state.intervalIsSet);
            this.setState({ intervalIsSet: null });
        }
    }


    //// render function ////
   render() {
        if (this.state.error) {
            return (
                <div className='mt-5'>
                    <Navigation/>
                    <SingleObjectView>
                        <p>Error: {this.state.error_msg}</p>
                    </SingleObjectView>
                </div>
            );
        }
        else {
            return (
                <div className='mt-5'>
                    <Navigation/>
                    <SingleObjectView>
                        <h1>Name: {this.state.group_name}</h1>
                        <h5>Description: {this.state.group_description}</h5>

                        {this.state.user_id  && this.state.user_isAdmin && <Button variant="primary" href="/eventCreate">Create Event</Button>} &nbsp;&nbsp;
                        {this.state.user_id  && this.state.user_isAdmin && <Button variant="primary" onClick={() => this.getContactInfo()}>Get Contact Info</Button>}&nbsp;&nbsp;
                        {this.state.user_id  && !this.state.user_belongs && <Button variant="primary" onClick={() => this.joinGroup()}>Join Group</Button>}&nbsp;&nbsp;
                        {this.state.user_id  && this.state.user_belongs && <Button variant="primary" onClick={() => this.leaveGroup()}>Leave Group</Button>}
                        <hr/>
                        <Card>&nbsp;
                            <h3>&nbsp;&nbsp;&nbsp;Users:</h3>
                        {this.state.group_users.map((user, i) =>
                            <Card key={i} user={user}>
                                <Card.Body>
                                    <h4>{user.first_name}  {user.last_name} {user.email}</h4>
                                    {user.uid != this.state.user_id && this.state.user_isAdmin && <Button variant="raised" onClick={() => this.makeAdmin(user.uid)}>Make Admin</Button>}
                                </Card.Body>
                            </Card>

                        )}
                        </Card>
                        <hr/>
                        <Card>&nbsp;
                            <h3>&nbsp;&nbsp;&nbsp;Events:</h3>
                            {this.state.group_events.map((event, i) =>
                                <Card key={i} user={event}>
                                    <Card.Body>
                                        <h4><Card.Link href={"/event/" + event.id}>{event.name}</Card.Link></h4>
                                    </Card.Body>
                                </Card>
                            )}
                        </Card>
                        <Footer/>
                    </SingleObjectView>
                </div>
            );
        }
    }
}

export default GroupPage;
