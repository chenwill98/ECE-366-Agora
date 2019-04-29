import React, { Component } from 'react';
import { Card, CardColumns } from "react-bootstrap";
import axios from "axios";
import Navigation from "../components/Navigation.js";
import CenterView from '../components/CenterView.js';
import {Backend_Route} from "../BackendRoute.js";
import Footer from "../components/Footer";
import Cookies from "universal-cookie";

const cookies = new Cookies();

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
            user_last_name: "",
            user_first_name: "",
            user_email: "",
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
        if (this.state.user_id && cookies.get("USER_TOKEN")) {
            //fetches all of the user's events
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
        //fetches all of the events available for browsing
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
        //fetches user data to display
        fetch( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}/get-user`, init)
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg:  "Error requesting the details of an user: " + error.message
                });
                console.log("Error requesting user: " + error.message);
            })
            .then(res => {
                res.json().then(data => ({
                        data: data,
                        status: res.status
                    })
                ).then(res => {
                    this.setState( {
                        user_first_name: res.data.first_name,
                        user_last_name: res.data.last_name,
                        user_email: res.data.email
                    });
                })
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
                    <nav className="navbar bg-white sticky-top flex-md-nowrap p-1">
                        <a className="navbar-brand text-center col-sm-3 col-md-2 mr-0" href="/account">{this.state.user_first_name} {this.state.user_last_name}</a>
                        <input className="form-control form-control-dark w-100" type="text" placeholder="Search"/>
                    </nav>
                    <div className="container-fluid">
                        <div className="row">
                            <nav className="col-md-2 d-none d-md-block bg-light sidebar">
                                <div className="sidebar-sticky">
                                    <ul className="nav flex-column">
                                        <li className="nav-item">
                                            <a className="nav-link active" href="/groupcreate">
                                                <i className="fas fa-plus-circle"></i>
                                                &nbsp; Create Group
                                            </a>
                                        </li>
                                    </ul>
                                </div>
                            </nav>
                            <main role="main" className="col-md-9 ml-sm-auto col-lg-10 pt-3 px-4">
                                <Card>
                                    <Card.Body>
                                    <div className="text-sm-left mb-3 text-center text-md-left mb-sm-0 col-12 col-sm-4">
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
                                    </Card.Body>
                                </Card>
                                <hr/>
                                <Card>
                                    <Card.Body>
                                        <div className="text-sm-left mb-3 text-center text-md-left mb-sm-0 col-12 col-sm-4">
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
                                    </Card.Body>
                                </Card>
                            </main>
                        </div>
                        <div className='pl-5'>
                            <Footer/>
                        </div>
                    </div>
                </div>
            );
        }
    };
}