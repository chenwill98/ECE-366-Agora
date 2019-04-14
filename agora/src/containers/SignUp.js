import React, { Component } from 'react';
import axios from "axios";
import { Form, Button, Card, Col, Alert } from "react-bootstrap";
import { Redirect } from 'react-router-dom'
import CenterView from '../components/CenterView.js';

class SignUp extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: "http://localhost",
            port: "8080",

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
            error_msg: ""
        };
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
            axios.post(`${this.state.ip}:${this.state.port}/user/create`, {
                email: this.state.user_email,
                first_name: this.state.user_name,
                last_name: this.state.user_surname,
                pass: this.state.user_password
            })
                .catch(error => {
                    this.setState({
                        error: true,
                        error_msg: "Error creating the user: " + error.message
                    });
                    console.log("Error posting user: " + error.message);
                });
            //if everything is fine, then redirect to login page
            if (!this.state.error) { //why does this show up as false???
                this.setState({user_success: true});
            }
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
        } else {
            return (
                <div className='mt-5'>
                    <CenterView>
                        <Card border="primary" style={{ width: '40rem'}}>
                            <Card.Header as="h5">Organize events and connect with others on Agora today!</Card.Header>
                            <Card.Body>
                                <Card.Title>Sign Up</Card.Title>
                                <Card.Text>
                                    {this.state.error ?
                                        <Alert dismissible variant="danger"
                                               onClick={() => this.setState({error:false})}>
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
                            </Card.Body>
                        </Card>
                    </CenterView>
                </div>
            );
        }
    }
}
export default SignUp;