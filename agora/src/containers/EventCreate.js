import React, { Component } from 'react';
import { Form, Button, Card, Alert, Col } from "react-bootstrap";
import CenterView from '../components/CenterView.js';
import Navigation from '../components/Navigation.js';
import { Redirect } from 'react-router-dom'
import {Backend_Route} from "../BackendRoute";
import Cookies from "universal-cookie";


const cookies = new Cookies();


class EventCreate extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: Backend_Route.ip,
            port: Backend_Route.port,

            group_owner_id: localStorage.getItem('groupID'),

            event_id: "",
            event_description: "",
            event_name: "",
            event_gid: "",
            event_location: "",
            event_date: "",
            event_time: "",
            event_success: false,

            user_session: false,

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }
    //determines if there is currently a user session by whether a cookie exists or not
    componentDidMount () {
    }


    //kills the process
    componentWillUnmount() {
        if (this.state.intervalSet) {
            clearInterval(this.state.intervalSet);
            this.setState({ intervalSet: null });
        }
    }


    //sets the values of the inputs as values in this.state
    handleChange = event => {
        this.setState({
            [event.target.id]: event.target.value
        });
    };

    //prevents the submission from refreshing
    handleSubmit = event => {
        event.preventDefault();
    };

    //sends a request to create a group that doesn't already exist
    EventCreate = () => {

        fetch(`${this.state.ip}:${this.state.port}/group/${this.state.group_owner_id}/create-event`,
            {
                    method: "Post",
                    credentials: "include",
                    body: JSON.stringify({
                        'name': this.state.event_name,
                        'description': this.state.event_description,
                        'date': this.state.event_date + ' ' + this.state.event_time + ':00',
                        'location': this.state.event_location
                    })
                }
        )
        .catch(error => {
            this.setState({
                error: true,
                error_msg: "Error creating the event: " + error.message
            });
            console.log("Error creating event: " + error.message);
        })
        .then(res => {
            if (res.status === 200) {
                if (!this.state.error) {
                    this.setState({
                        event_success: true
                    });
                }
                console.log("Successfully created event.");
            }
            else {
                this.setState({
                    error: true,
                    error_msg: "Response: " + res.status
                });
                console.log("Error creating event.");
            }
        });
    };



    render() {
        // confirm that the user session exists, otherwise redirect to login.
        if (!cookies.get("USER_TOKEN")) {
            return <Redirect to="/login"/>;
        }
        else if (this.state.event_success) {
            return (
                <Redirect to="/home"/>
            );
        } else if (this.state.error) {
                    return (
                        <div className='mt-5'>
                            <Navigation/>
                            <CenterView>
                                <p>Error: {this.state.error_msg}</p>
                            </CenterView>
                        </div>
                    );
        } else {
            return (
                <div className='mt-5'>
                    <Navigation/>
                        <CenterView>
                            <Card border="primary" style={{ width: '40rem'}}>
                            <Card.Header as="h5">Event Creation</Card.Header>
                            <Card.Body>
                                <Card.Text>
                                    {this.state.error ?
                                        <Alert dismissible variant="danger"
                                               onClick={() => this.setState({error:false})}>
                                            {this.state.error_msg}
                                        </Alert>
                                        : ''}
                                    <Form onSubmit={this.handleSubmit}>
                                        <Form.Group controlId="event_name">
                                            <Form.Label>Event Name</Form.Label>
                                            <Form.Control type="event_name"
                                                          placeholder="Name"
                                                          onChange={this.handleChange}/>
                                        </Form.Group>

                                        <Form.Group controlId="event_description">
                                            <Form.Label>Event Description</Form.Label>
                                            <Form.Control type="event_description"
                                                          placeholder="Description"
                                                          as="textarea"
                                                          onChange={this.handleChange}/>
                                        </Form.Group>

                                        <Form.Group controlId="event_location">
                                            <Form.Label>Event Location</Form.Label>
                                            <Form.Control type="event_location"
                                                          placeholder="Location"
                                                          onChange={this.handleChange}/>
                                        </Form.Group>
                                        <Form.Row>
                                            <Form.Group as={Col} controlId="event_date">
                                                <Form.Label>Date</Form.Label>
                                                <Form.Control type="event_date"
                                                              placeholder="MM/DD/YYY"
                                                              onChange={this.handleChange}/>
                                            </Form.Group>
                                            <Form.Group as={Col} controlId="event_time">
                                                <Form.Label>Time</Form.Label>
                                                <Form.Control type="event_time"
                                                              placeholder="00:00 AM"
                                                              onChange={this.handleChange}/>
                                            </Form.Group>
                                        </Form.Row>
                                        <Button variant="primary" type="submit" onClick={() => this.EventCreate()}>
                                            Create Event
                                        </Button>
                                    </Form>
                                </Card.Text>
                            </Card.Body>
                        </Card>
                    </CenterView>
                </div>
            );
        }
    }
}

export default EventCreate;