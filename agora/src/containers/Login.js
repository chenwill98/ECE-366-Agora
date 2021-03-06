import React, { Component } from "react";
import axios from "axios";
import { Form, Button, Card, Alert, Image, Jumbotron, InputGroup } from "react-bootstrap";
import "../styles/Login.css";
import { Redirect } from 'react-router-dom'
import CenterView from '../components/CenterView.js';
import Footer from "../components/Footer.js";
import {Backend_Route} from "../BackendRoute.js";
import Cookies from "universal-cookie";
import * as emailjs from 'emailjs-com';


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

            // form related states
            forgot:false,

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
                    error_msg:  "Error logging in. Check that your username and password are correct."
                });
                console.log("Error requesting cookie: " + error.message);
            });
        }
    };

    email = () => {
        let template_params = {
            "reply_to": this.state.user_email,
            "message_html": "We don't actually have the appropriate routes to retrieve a user's password lmao"
        };

        let service_id = "agora_service";
        let template_id = "template_hlWoe6JV";
        let user_id = "user_L6P4JRoGpemcRWO1WNmcG";
        emailjs.send(service_id, template_id, template_params, user_id)
            .then(function(response) {
                console.log('SUCCESS!', response.status, response.text);
            }, function(error) {
                console.log('FAILED...', error);
            });
    };

    render() {
        if (cookies.get("USER_TOKEN")) {
            return(
                <Redirect to="/home"/>
            )
        } else {
            return (
                <div className='p-5'>
                    <CenterView>
                        <a href="/">
                            <Image src={require("../images/Logo.png")} style={{width: '20rem'}} rounded fluid/>
                        </a>
                    </CenterView>
                    <hr />
                    <Jumbotron fluid>
                        <CenterView>
                            {this.state.email ?
                                <Card border="primary" style={{width: '40rem'}}>
                                    <Card.Header className="text-center" as="h5">Forgot my password</Card.Header>
                                    <Card.Body>
                                        <Card.Text>
                                            <Form onSubmit={this.handleSubmit}>
                                                <Form.Group controlId="user_email">
                                                    <Form.Label>Email address</Form.Label>
                                                    <InputGroup>
                                                        <InputGroup.Prepend>
                                                            <InputGroup.Text id="inputGroupPrepend">@</InputGroup.Text>
                                                        </InputGroup.Prepend>
                                                        <Form.Control type="email"
                                                                      placeholder="Enter email"
                                                                      aria-describedby="inputGroupPrepend"
                                                                      onChange={this.handleChange}/>
                                                    </InputGroup>
                                                    {this.state.error ?
                                                        <Alert dismissible variant="danger"
                                                               onClick={() => this.setState({error: false})}>
                                                            {this.state.error_msg}
                                                        </Alert>
                                                        : ''}
                                                    <Form.Text className="text-muted">
                                                        We'll send your password to your email
                                                    </Form.Text>
                                                </Form.Group>
                                                <Button variant="primary" type="submit" onClick={() => this.email()} block>
                                                    Retrieve my password!
                                                </Button>
                                            </Form>
                                        </Card.Text>
                                        <Card.Link href="/login">I remember it now!</Card.Link>
                                    </Card.Body>
                                </Card>
                                :
                                <Card border="primary" style={{width: '40rem'}}>
                                    <Card.Header className="text-center" as="h5">Login</Card.Header>
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
                                                    <InputGroup>
                                                        <InputGroup.Prepend>
                                                            <InputGroup.Text id="inputGroupPrepend">@</InputGroup.Text>
                                                        </InputGroup.Prepend>
                                                        <Form.Control type="email"
                                                                      placeholder="Enter email"
                                                                      aria-describedby="inputGroupPrepend"
                                                                      onChange={this.handleChange}/>
                                                    </InputGroup>
                                                </Form.Group>
                                                <Form.Group controlId="user_password">
                                                    <Form.Label>Password</Form.Label>
                                                    <Form.Control type="password"
                                                                  placeholder="Password"
                                                                  onChange={this.handleChange}/>
                                                </Form.Group>
                                                <Button variant="primary" type="submit" onClick={() => this.Login()} block>
                                                    Login
                                                </Button>
                                            </Form>
                                        </Card.Text>
                                        <Card.Link href="/signup">Don't have an account?</Card.Link>
                                        <Card.Link className="pull-right" href="#" onClick={() => this.setState({email: true})}>Forgot your password?</Card.Link>
                                    </Card.Body>
                                </Card>}
                        </CenterView>
                    </Jumbotron>
                    <Footer/>
                </div>
            );
        }
    }
}

export default Login;