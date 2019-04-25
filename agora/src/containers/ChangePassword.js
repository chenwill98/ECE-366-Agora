import React, { Component } from 'react';
import axios from "axios";
import { Form, Button, Card, Col, Alert } from "react-bootstrap";

import CenterView from '../components/CenterView.js';
import Navigation from '../components/Navigation.js';
import { Redirect } from 'react-router-dom'
import {Backend_Route} from "../BackendRoute.js";

class ChangePassword extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: Backend_Route.ip,
            port: Backend_Route.port,

            // user related states
            user_session: false,

            old_pass: "",
            new_pass: "",
            pass_nomatch: false,
            change_success: false,

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

    //sends a request to create a user if it matches criteria and doesn't already exist
    ChangePassword= () => {
        let data = JSON.stringify({
            'pass_hash': this.state.new_pass
        });
        axios.post(`${this.state.ip}:${this.state.port}/user/changePassword`, data)
        .then(res => { //if everything is fine, then redirect to home page
            if (res.status === 200) {
                this.setState({
                    change_success: true
                });
            }
            else {
                this.setState( {
                    pass_nomatch: true
                });
            }
        })
        .catch(error => {
            console.log("Status: " + error.status);
            if (error.response.status === 400) {
                this.setState( {
                    pass_nomatch: true
                });
            }
            else {
                this.setState({
                    error: true,
                    error_msg: "Error changing password: " + error.message
                });
                console.log("Error posting password: " + error.message);
            }
        })
    };

    render() {
        if (this.state.change_success) {
            return (
                <Redirect to="/home"/>
            );
        } else if (this.state.error === false) {
                return (
                    <div className='mt-5'>
                        <Navigation/>
                        <CenterView>
                            <Card border="primary" style={{width: '40rem'}}>
                                <Card.Body>
                                    <Card.Title>Change Password</Card.Title>
                                    <Card.Text>
                                        {this.state.error ?
                                            <Alert dismissible variant="danger"
                                                   onClick={() => this.setState({error: false})}>
                                                {this.state.error_msg}
                                            </Alert>
                                            : ''}
                                        <Form onSubmit={this.handleSubmit}>
                                            <Form.Group controlId="old_pass">
                                                <Form.Label>Old Password</Form.Label>
                                                <Form.Control type="opass"
                                                              placeholder="Enter Old Password"
                                                              onChange={this.handleChange}/>
                                            </Form.Group>

                                            <Form.Group controlId="new_pass">
                                                <Form.Label>Password</Form.Label>
                                                <Form.Control type="password"
                                                              placeholder="Enter New Password"
                                                              onChange={this.handleChange}/>
                                                <Form.Text className="text-muted">
                                                    1234 is not a good password.
                                                </Form.Text>
                                            </Form.Group>
                                            <Button variant="primary" type="submit" onClick={() => this.ChangePassword()}>
                                                Apply Changes
                                            </Button>
                                        </Form>
                                    </Card.Text>
                                    <Card.Text>
                                        {this.state.pass_nomatch ?
                                            <Alert variant="danger">
                                                Incorrect old password!
                                            </Alert>
                                        : ''}
                                    </Card.Text>
                                </Card.Body>

                            </Card>
                        </CenterView>
                    </div>
                );
        }
        else { // error!
            return (
                <div className='mt-5'>
                    <Navigation/>
                    <div className='mt-5'>
                        <CenterView>
                        <p>Error: {this.state.error_msg}</p>
                        </CenterView>
                    </div>
                </div>
            );
        }
    }
}
export default ChangePassword;