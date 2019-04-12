import React, { Component } from "react";
import axios from "axios";
import SinglebObjectView from '../components/SingleObjectView.js';
import Navigation from '../components/Navigation.js';
import Card from "react-bootstrap/Card";

class EventPage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: "http://localhost",
            port: "8080",

            // events related states
            event_id: this.props.match.params.event_id,
            event_description: "description",
            event_name: "Name",
            event_gid: "456",
            event_location: "Here",
            event_date: "03/18/1994",
            event_users: [],

            // if in a user session
            user_id: "",
            user_cookie: "",
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
            })
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg: error.message
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
            .catch( error => {
                this.setState({
                    error: true,
                    error_msg: error.message
                });
                console.log("Error requesting event-users: " + error.message);
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
                        <p>Location: {this.state.event_location}</p>
                        <p>Date: {this.state.event_date}</p>

                        <Card>
                            <h3>Users attending:</h3>
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