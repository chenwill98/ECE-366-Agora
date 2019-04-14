import React, { Component } from "react";
import axios from "axios";
import { Form, Button, Card, Alert } from "react-bootstrap";
import { Redirect } from 'react-router-dom'
import CenterView from '../components/CenterView.js';

class Login extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: "http://localhost",
            port: "8080",

            // user related states
            user_id: "",
            user_cookie: "",
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
        if (localStorage.getItem('cookie')) {
            this.setState({user_session: true});
        }
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
            axios.post(`${this.state.ip}:${this.state.port}/login`, {
                email: this.state.user_email,
                pass: this.state.user_password
            })
                .then(res => {
                    localStorage.setItem('cookie', res.data);
                })
                .catch(error => { //not catching error with connecting? only catches errors returned by backend?
                    this.setState({
                        error: true,
                        error_msg:  "Error logging in user: " + error.message
                    });
                    console.log("Error requesting cookie: " + error.message);
                });
            //if everything is fine, then redirect to user home
            if (!this.state.error) {
                this.setState({user_session: true});
            }
        }
    };

    render() {
        if (this.state.user_session) {
            return(
                <Redirect to="/home"/>
            )
        } else {
            return (
                <div className='mt-5'>
                    <CenterView>
                        <Card border="primary" style={{width: '40rem'}}>
                            <Card.Body>
                                <Card.Title>Login</Card.Title>
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