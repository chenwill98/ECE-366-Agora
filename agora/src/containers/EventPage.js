import React, { Component } from "react";
import axios from "axios";
import SinglebObjectView from '../components/SingleObjectView.js';
import Navigation from '../components/Navigation.js';
import Card from "react-bootstrap/Card";
import {Backend_Route} from "../BackendRoute.js";
import ListGroup from 'react-bootstrap/ListGroup'


class EventPage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: Backend_Route.ip,
            port: Backend_Route.port,

            // events related states
            event_id: this.props.match.params.event_id,
            event_description: "description",
            event_name: "Name",
            event_gid: "456",
            event_g_name: "",
            event_location: "Here",
            event_date: "03/18/1994",
            event_users: [],

            // if in a user session
            user_id: "",
            user_isAdmin: false,

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }


    //fetches all data when the component mounts (called right after constructor)
    componentDidMount () {
        // get the event info
        axios.get( `${this.state.ip}:${this.state.port}/event/${this.state.event_id}`)
            .then(res => {
                this.setState( {
                    event_description: res.data.description,
                    event_name: res.data.name,
                    event_gid: res.data.gid,
                    event_location: res.data.location,
                    event_date: res.data.date
                });

                // get the Group name of the event's parent group.
                console.log(this.state.event_gid);
                axios.get( `${this.state.ip}:${this.state.port}/group/${this.state.event_gid}`)
                    .then(res => {
                        this.setState( {
                            event_g_name: res.data.name
                        });
                    })
                    .catch(error => {
                        this.setState({
                            error: true,
                            error_msg: error.message
                        });
                        console.log("Error requesting event's parent group info: " + error.message);
                    });
            })
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg:  "Error requesting the details of an event: " + error.message
                });
                console.log("Error requesting event: " + error.message);
            });

        // get the event's users
        axios.get(`${this.state.ip}:${this.state.port}/event/${this.state.event_id}/get-users`)
            .then( res => {
                this.setState( {
                    event_users: res.data
                })
            })

        if (!this.state.intervalSet) {
            let interval = setInterval(this.getData, 1000);
            this.setState({intervalSet: interval})
        }
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
                    <SinglebObjectView>
                        <p> {this.state.error_msg}</p>
                    </SinglebObjectView>
                </div>
                    );
        }
        else {
            return (
                <div className='mt-5'>
                    <Navigation/>

                    <SinglebObjectView>
                        <h1>Name: {this.state.event_name}</h1>
                        <p>Description: {this.state.event_description}</p>

                        <Card>
                            <Card.Body>
                                <Card.Link href={"/group/" + this.state.event_gid}>{this.state.event_g_name}</Card.Link>
                            </Card.Body>
                        </Card>

                        <p>Location: {this.state.event_location}</p>
                        <p>Date: {this.state.event_date}</p>



			  <ListGroup> 
			    {this.state.event_users.map((user, i) =>
                                <ListGroup.Item key={i} user={user}>
                                    {user.first_name} {user.last_name} {user.email}
                                </ListGroup.Item>
                            )}    

			  </ListGroup>
                        <Card>
                            <h3>Users attending:</h3>
			    <ListGroup> 
			      {this.state.event_users.map((user, i) =>
                                  <ListGroup.Item key={i} user={user}>
                                      {user.first_name} {user.last_name} {user.email}
                                  </ListGroup.Item>
                              )}    

			    </ListGroup>	
                            {this.state.event_users.map((user, i) =>
                                <Card key={i} user={user}>
                                    <Card.Body><Card.Title>{user.first_name} {user.last_name} {user.email}</Card.Title></Card.Body>
                                </Card>
                            )}
                        </Card>
                    </SinglebObjectView>

                </div>
            );
        }
    }
}

export default EventPage;
