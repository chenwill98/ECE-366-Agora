import React, { Component } from 'react';
import { Card, CardColumns, Jumbotron } from "react-bootstrap";
import axios from "axios";
import Navigation from "../components/Navigation.js";
import CenterView from '../components/CenterView.js';
import {Backend_Route} from "../BackendRoute.js";
import Footer from "../components/Footer";


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
            other_events: [],

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }

    //fetches all data when the component mounts
    componentDidMount() {
        this.getData();
        this.removeDup(this.state.other_events, this.state.user_events);
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
                                user_events: res.data
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
                other_events: res.data
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

    // removes the duplicate objects so other_events doesn't contain redundant events to the user_events
    removeDup = (array, subset) => {
        this.setState({array: array.filter(obj => !subset.includes(obj))});
    };

    /// render file ///
    render() {
        if (this.state.error) {
            return (
                <div className='p-5'>
                    <Navigation/>
                    <CenterView>
                        <Card border="primary" style={{width: '40rem'}}>
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
        } else {
            return (
                <div>
                    <Navigation/>

                    <main className='p-5'>
                        <Jumbotron>
                            <div className="text-sm-left mb-3 text-center text-md-left mb-sm-0 col-12 col-sm-4">
                                {/*<span className="text-uppercase page-subtitle">Dashboard</span>*/}
                                <h3>Your Events</h3>
                            </div>
                            <hr/>
                            <CardColumns>
                                {this.state.user_events.map((events, i) =>
                                    <Card key={i} event={events}>
                                        <Card.Header as="h5">
                                            <Card.Link href={"/event/" + events.id}>
                                                {events.name}
                                            </Card.Link>
                                        </Card.Header>
                                        <Card.Body>
                                            {events.description}
                                        </Card.Body>
                                    </Card>
                                )}
                            </CardColumns>
                        </Jumbotron>
                        <Jumbotron>
                            <div className="text-sm-left mb-3 text-center text-md-left mb-sm-0 col-12 col-sm-4">
                                {/*<span className="text-uppercase page-subtitle">Dashboard</span>*/}
                                <h3>All Events</h3>
                            </div>
                            <hr/>
                            <CardColumns>
                                {this.state.other_events.map((events, i) =>
                                    <Card key={i} event={events}>
                                        <Card.Header as="h5">
                                            <Card.Link href={"/event/" + events.id}>
                                                {events.name}
                                            </Card.Link>
                                        </Card.Header>
                                        <Card.Body>
                                            {events.description}
                                        </Card.Body>
                                    </Card>
                                )}
                            </CardColumns>
                        </Jumbotron>
                    </main>
                    <Footer/>
                </div>
            );
        }
    };
}