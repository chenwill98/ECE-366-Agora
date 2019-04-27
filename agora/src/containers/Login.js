import React, { Component } from "react";
import axios from "axios";
import { Form, Button, Card, Alert } from "react-bootstrap";
import "../styles/Login.css";
import { Redirect } from 'react-router-dom'
import WelcomeNav from '../components/WelcomeNav.js';
import CenterView from '../components/CenterView.js';
import {Backend_Route} from "../BackendRoute.js";
import Cookies from "universal-cookie";


const cookies = new Cookies();

class Login extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: Backend_Route.ip,
            port: Backend_Route.port,

            // user related states
            user_id: "",
            user_email: "",
            user_password: "",
            user_session: false,

            // error related states
            success: false,
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
        if (this.state.intervalIsSet) {
            clearInterval(this.state.intervalIsSet);
            this.setState({ intervalIsSet: null });
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

    //posts posts the input email, password, and then returns the response
    Login = () => {
        //only tries to log in if the email matches certain criteria
        if (this.state.user_email.includes('@')) {
            let data = JSON.stringify({
                'email': this.state.user_email,
                'pass_hash': this.state.user_password
            });
            axios.post(`${this.state.ip}:${this.state.port}/login`, data)
            .then(res => {
                if (res.status === 200) {

                    // set cookie and local storage from response
                    cookies.set('USER_TOKEN', res.data[0], { path: '/' });
                    localStorage.setItem('userID', res.data[1]);
                    this.setState( {
                        success: true
                    });
                }
                else {
                    this.setState({
                        error: true,
                        error_msg:  "Error logging in user. Status code: " + res.status
                    });
                    console.log("Error requesting cookie: " + res.status);
                }
            })
            .catch(error => { //not catching error with connecting? only catches errors returned by backend?
                this.setState({
                    error: true,
                    error_msg:  "Error logging in user: " + error.message
                });
                console.log("Error requesting cookie: " + error.message);
            });
        }
    };

    render() {
        if (cookies.get("USER_TOKEN")) {
            return(
                <Redirect to="/home"/>
            )
        } else {
            return (
                <div className='mt-5'>
                    <WelcomeNav/>
                    <CenterView>
                        <Card border="primary" style={{width: '40rem'}}>
                            <Card.Header as="h5">Login</Card.Header>
                            <Card.Body>
                                <Card.Text>
                                    <Form onSubmit={this.handleSubmit}>
                                        {this.state.error ?
                                            <Alert dismissible variant="danger"
                                                   onClick={() => this.setState({error: false})}>
                                                {this.state.error_msg}
                                            </Alert>
                                            : ''}
                                        <Form.Group controlId="user_email">
                                            <Form.Label>Email address</Form.Label>
                                            <Form.Control type="email"
                                                          placeholder="Enter email"
                                                          onChange={this.handleChange}/>
                                        </Form.Group>
                                        <Form.Group controlId="user_password">
                                            <Form.Label>Password</Form.Label>
                                            <Form.Control type="password"
                                                          placeholder="Password"
                                                          onChange={this.handleChange}/>
                                        </Form.Group>
                                        <Button variant="primary" type="submit" onClick={() => this.Login()}>
                                            Login
                                        </Button>
                                    </Form>
                                </Card.Text>
                                <Card.Link href="/signup">Don't have an account?</Card.Link>
                                <Card.Link href="#">Forgot your password?</Card.Link>
                            </Card.Body>
                        </Card>
                    </CenterView>
                </div>
            );
        }
    }
}

export default Login;