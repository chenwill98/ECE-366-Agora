import React, { Component } from 'react';
import { Card } from "react-bootstrap";
import axios from "axios";
import Navigation from "../components/Navigation.js";
import CenterView from '../components/CenterView.js';
import {Backend_Route} from "../BackendRoute.js";


let init = {
    method: "Get",
    credentials: "include"
};


export default class Events extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: Backend_Route.ip,
            port: Backend_Route.port,

            // user related states
            user_id: localStorage.getItem('userID'),
            user_cookie: "",
            user_events: [],
            total_events: [],

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }

    //fetches all data when the component mounts
    componentDidMount() {
        this.getData();
        // if (!this.state.intervalSet) {
        //     let interval = setInterval(this.getData, 1000);
        //     this.setState({intervalSet: interval});
        // }
    }

    // kills the process
    componentWillUnmount() {
        // if (this.state.intervalSet) {
        //     clearInterval(this.state.intervalSet);
        //     this.setState({ intervalSet: null });
        // }
    }

    getData = () => {

        if (this.state.user_id) {
            //fetches all of the user's groups
            fetch( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}/events`, init)
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg:  "Error requesting list of user events: " + error.message
                });
                console.log("Error requesting user events: " + error.message);
            })
            .then(res => {
                res.json().then(data => ({
                        data: data,
                        status: res.status
                    })
                )
                    .then(res => {
                        if (res.data !== '') {
                            this.setState( {
                                user_events: res.data.events
                            });
                            console.log("Successfully got user's events.");
                        }
                    })
            });
        }


        //fetches all of the groups available for browsing
        axios.get( `${this.state.ip}:${this.state.port}/event/get-events`)
        .then(res => {
            this.setState( {
                total_events: res.data.events
            });
            console.log("Successfully got all events.");
        })
        .catch(error => {
            this.setState({
                error: true,
                error_msg:  "Error requesting list of all events: " + error.message
            });
            console.log("Error requesting all events: " + error.message);
        });
    };

    /// render file ///
    render() {
        if (this.state.error)
            return (
                <div className='mt-5'>
                    <Navigation/>
                    <CenterView>
                        <Card border="primary" style={{ width: '40rem'}}>
                            <Card.Body>
                                <Card.Title>Error</Card.Title>
                                <Card.Text>
                                    Oops,{this.state.error_msg} :/
                                </Card.Text>
                            </Card.Body>
                        </Card>
                    </CenterView>
                </div>
            );
        else {
            return (
                <div>
                    <Navigation/>
                    <CenterView>
                        <Card>
                            <Card.Header as="h5">Your groups</Card.Header>
                        </Card>
                        {this.state.user_events && this.state.user_events.map((events, i) =>
                            <Card key={i} event={events}>

                            </Card>
                        )}
                        <Card>
                            <Card.Header as="h5">All groups</Card.Header>
                        </Card>
                        {this.state.total_events && this.state.total_events.map((events, i) =>
                            <Card key={i} event={events}>

                            </Card>
                        )}
                    </CenterView>
                </div>
            );
        }
    };
}