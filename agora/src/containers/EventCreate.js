import React, { Component } from 'react';
import axios from "axios";
import { Form, Button, Card, Alert, Col } from "react-bootstrap";
import CenterView from '../components/CenterView.js';
import Navigation from '../components/Navigation.js';
import { Redirect } from 'react-router-dom'

class EventCreate extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: "http://localhost",
            port: "8080",

            // events related states
            event_id: "",
            event_description: "",
            event_name: "",
            event_gid: "",
            event_location: "",
            event_date: "",
            event_time: "",

            user_session: false,

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }
    //determines if there is currently a user session by whether a cookie exists or not
    componentDidMount () {
        if (localStorage.getItem('cookie')) {
            this.setState({user_session: true});
        }
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
        axios.post(`${this.state.ip}:${this.state.port}/eventCreate`, {
            event_name: this.state.name,
            event_description: this.state.description,
            event_location: this.state.location,
            event_date: this.state.date,
            event_time: this.state.time
        })
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg: "Error creating the group: " + error.message
                });
                console.log("Error posting group: " + error.message);
            });
        //if everything is fine, then redirect to group page
        if (!this.state.error) { //why does this show up as false???
            this.setState({group_success: true});
        }
    };
    render() {
        if (this.state.group_success) {
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
                                            <Form.Control type="name"
                                                          placeholder="Name"
                                                          onChange={this.handleChange}/>
                                        </Form.Group>

                                        <Form.Group controlId="event_description">
                                            <Form.Label>Event Description</Form.Label>
                                            <Form.Control type="description"
                                                          placeholder="Description"
                                                          onChange={this.handleChange}/>
                                        </Form.Group>

                                        <Form.Group controlId="event_location">
                                            <Form.Label>Event Location</Form.Label>
                                            <Form.Control type="location"
                                                          placeholder="Location"
                                                          onChange={this.handleChange}/>
                                        </Form.Group>
                                        <Form.Row>
                                            <Form.Group as={Col} controlId="event_date">
                                                <Form.Label>Date</Form.Label>
                                                <Form.Control type="date"
                                                              placeholder="MM/DD/YYY"
                                                              onChange={this.handleChange}/>
                                            </Form.Group>
                                            <Form.Group as={Col} controlId="event_time">
                                                <Form.Label>Time</Form.Label>
                                                <Form.Control type="time"
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