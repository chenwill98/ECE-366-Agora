import React, { Component } from "react";
import axios from "axios";
import Navigation from '../components/Navigation.js';
import SingleObjectView from '../components/SingleObjectView.js';
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import {Backend_Route} from "../BackendRoute.js";

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
            user_id: "",
            user_isAdmin: false,

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }


    //fetches all data when the component mounts
    componentDidMount () {

        // get group info
        axios.get( `${this.state.ip}:${this.state.port}/group/${this.state.group_id}`)
            .then(res => {
                this.setState( {
                    group_description: res.data.description,
                    group_name: res.data.name
                });
            })
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg: error.message
                });
                console.log("Error requesting group info: " + error.message);
            });

        // get the group's users
        axios.get(`${this.state.ip}:${this.state.port}/group/${this.state.group_id}/get-users`)
            .then ( res => {
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

        // get the group's events
        axios.get(`${this.state.ip}:${this.state.port}/group/${this.state.group_id}/get-events`)
            .then ( res => {
                console.log(res.data);
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

        // find if user is an admin, if so give him certain buttons
        if (this.state.user_id !== '') {

            axios.get( `${this.state.ip}:${this.state.port}/group/${this.state.group_id}/is-admin/${this.state.user_id}`)
                .then( res => {
                    this.setState( {
                        user_isAdmin: true
                    });
                })
                .catch( error => {
                    this.setState({
                        error: true,
                        error_msg: error.message
                    });
                    console.log("Error requesting is-admin: " + error.message);
                })
        }

        if (!this.state.intervalSet) {
            let interval = setInterval(this.getData, 1000);
            this.setState({intervalSet: interval})
        }
    }

    // updates the group_users array to also include the user's emails
    getContactInfo() {
        // get the group's events
        axios.post(`${this.state.ip}:${this.state.port}/group/${this.state.group_id}/view-contacts`,
                {},
            { headers: { 'Authorization': 'USER_TOKEN=' + localStorage.getItem('cookie')} })
            .then ( res => {
                console.log(res.data);
                this.setState( {
                    group_events: res.data
                });
            })
            .catch( error => {
                this.setState({
                    error: true,
                    error_msg: error.message
                });
                console.log("Error requesting view-contacts: " + error.message);
            });
    }

    //kills the process
    componentWillUnmount() {
        if (this.state.intervalIsSet) {
            clearInterval(this.state.intervalIsSet);
            this.setState({ intervalIsSet: null });
        }
    }


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
                        <p>Description: {this.state.group_description}</p>

                        {this.state.user_isAdmin && <Button variant="raised" href="/createEvent">Create Event</Button>}
                        {this.state.user_isAdmin && <Button variant="raised" onClick={() => this.getContactInfo()}>Get Contact Info</Button>}

                        <Card>
                            <h3>Users:</h3>
                        {this.state.group_users.map((user, i) =>
                            <Card key={i} user={user}>
                                <Card.Body><Card.Title>{user.first_name} {user.last_name} {user.email}</Card.Title></Card.Body>
                            </Card>
                        )}
                        </Card>

                        <Card>
                            <h3>Events:</h3>
                            {this.state.group_events.map((event, i) =>
                                <Card key={i} user={event}>
                                    <Card.Body><Card.Link href={"/event/" + event.id}>{event.name}</Card.Link></Card.Body>
                                </Card>
                            )}
                        </Card>
                    </SingleObjectView>
                </div>
            );
        }
    }
}


export default GroupPage;