import React, { Component } from 'react';
import axios from "axios";
import { Form, Button, Card, Col, Alert } from "react-bootstrap";
import "../styles/SignUp.css";
import { Redirect } from 'react-router-dom'
import WelcomeNav from '../components/WelcomeNav.js';
import CenterView from '../components/CenterView.js';
import {Backend_Route} from "../BackendRoute.js";
import Navigation from '../components/Navigation.js';

class SignUp extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: Backend_Route.ip,
            port: Backend_Route.port,

            // user related states
            user_id: "",
            user_cookie: "",
            user_name: "",
            user_surname: "",
            user_email: "",
            user_password: "",
            user_session: false,
            user_success: false,

            // error related states
            intervalSet: false,
            error: false,
            already_user: false,
            error_msg: ""
        };
    }

    //determines if there is currently a user session by whether a cookie exists or not
    componentDidMount () {
        // if (localStorage.getItem('cookie')) {
        //     this.setState({user_session: true});
        // }
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
    SignUp = () => {

        //only attempts to sign up if the email matches certain criteria
        if (this.state.user_email.includes('@')) {

            let data = JSON.stringify({
                'email': this.state.user_email,
                'first_name': this.state.user_name,
                'last_name': this.state.user_surname,
                'pass_hash': this.state.user_password
            });

            axios.post(`${this.state.ip}:${this.state.port}/user/create`, data)
            .then(res => { //if everything is fine, then redirect to login page
                if (res.status === 200) {
                    this.setState({
                        user_success: true
                    });
                }
                else {
                    this.setState( {
                        already_user: true
                    });
                }
            })
            .catch(error => {
                console.log("Status: " + error.status);
                if (error.response.status === 400) {
                    this.setState( {
                        already_user: true
                    });
                }
                else {
                    this.setState({
                        error: true,
                        error_msg: "Error creating the user: " + error.message
                    });
                    console.log("Error posting user: " + error.message);
                }
            })
        }
    };

    render() {
        if (this.state.user_success) {
            return (
                <Redirect to="/login"/>
            );
        } else if (this.state.user_session) {
            return(
                <Redirect to="/home"/>
            );
        } else if (this.state.error === false) {
                return (
                    <div className='mt-5'>
                        <WelcomeNav/>
                        <CenterView>
                            <Card border="primary" style={{width: '40rem'}}>
                                <Card.Header as="h5">Organize events and connect with others on Agora
                                    today!</Card.Header>
                                <Card.Body>
                                    <Card.Title>Sign Up</Card.Title>
                                    <Card.Text>
                                        {this.state.error ?
                                            <Alert dismissible variant="danger"
                                                   onClick={() => this.setState({error: false})}>
                                                {this.state.error_msg}
                                            </Alert>
                                            : ''}
                                        <Form onSubmit={this.handleSubmit}>
                                            <Form.Group controlId="user_email">
                                                <Form.Label>Email address</Form.Label>
                                                <Form.Control type="email"
                                                              placeholder="Enter email"
                                                              onChange={this.handleChange}/>
                                            </Form.Group>
                                            <Form.Row>
                                                <Form.Group as={Col} controlId="user_name">
                                                    <Form.Label>First Name</Form.Label>
                                                    <Form.Control type="first_name"
                                                                  placeholder="First Name"
                                                                  onChange={this.handleChange}/>
                                                </Form.Group>
                                                <Form.Group as={Col} controlId="user_surname">
                                                    <Form.Label>Last Name</Form.Label>
                                                    <Form.Control type="last_name"
                                                                  placeholder="Last Name"
                                                                  onChange={this.handleChange}/>
                                                </Form.Group>
                                            </Form.Row>
                                            <Form.Group controlId="user_password">
                                                <Form.Label>Password</Form.Label>
                                                <Form.Control type="password"
                                                              placeholder="Password"
                                                              onChange={this.handleChange}/>
                                                <Form.Text className="text-muted">
                                                    We'll never share your password with anyone else besides Zuckerberg
                                                </Form.Text>
                                            </Form.Group>
                                            <Button variant="primary" type="submit" onClick={() => this.SignUp()}>
                                                Sign Up
                                            </Button>
                                        </Form>
                                    </Card.Text>
                                    <Card.Link href="/login">Already have an account?</Card.Link>
                                    <Card.Text>
                                        {this.state.already_user ?
                                            <Alert variant="danger">
                                                A user with this email was already created!
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
export default SignUp;