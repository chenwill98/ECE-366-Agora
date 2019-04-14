import React, { Component } from 'react';
import axios from "axios";
import { Form, Button, Card } from "react-bootstrap";
import CenterView from '../components/CenterView.js';
import Navigation from '../components/Navigation.js';

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
            user_email: "",
            user_password: "",

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }

    //fetches all data when the component mounts
    componentDidMount() {
        // this.getData();
        console.log(localStorage.getItem('cookie'));
        if (!this.state.intervalSet) {
            let interval = setInterval(this.getData, 1000);
            this.setState({intervalSet: interval});
        }
    }

    //kills the process
    componentWillUnmount() {
        if (this.state.intervalSet) {
            clearInterval(this.state.intervalSet);
            this.setState({ intervalSet: null });
        }
    }

    //fetches data
    // getData = () => {
    //     fetch("http://199.98.27.114:8080/somestuff")
    //         .then(data => data.json())
    //         .then(res => this.setState({ data: res.data }));
    // };

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

    SignUp = () => {
        axios.post("http://199.98.27.114:8080/create", {
            email: this.state.user_email,
            pass: this.state.user_password
        })
            .then(res => {
                localStorage.setItem('cookie', res.data);
            })
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg:  "Error requesting the details of an event: " + error.message
                });
                console.log("Error requesting event: " + error.message);
            });
    };

    render() {
        return (
            <div className='mt-5'>
                <Navigation/>
                <CenterView>
                    <Card border="primary" style={{ width: '40rem'}}>
                        <Card.Header as="h5">Organize events and connect with others on Agora today!</Card.Header>
                        <Card.Body>
                            <Card.Title>Sign Up</Card.Title>
                            <Card.Text>
                                <Form onSubmit={this.handleSubmit}>

                                    <Form.Group controlId="email">
                                        <Form.Label>Email address</Form.Label>
                                        <Form.Control type="email"
                                                      placeholder="Enter email"
                                                      onChange={this.handleChange}/>
                                        {this.state.error ? console.log("hello") : '' }
                                    </Form.Group>
                                    <Form.Group controlId="password">
                                        <Form.Label>Password</Form.Label>
                                        <Form.Control type="password"
                                                      placeholder="Password"
                                                      onChange={this.handleChange}/>
                                        <Form.Text className="text-muted">
                                            We'll never share your password with anyone else besides Zuckerberg
                                        </Form.Text>
                                    </Form.Group>
                                    <Form.Group controlId="formBasicCheckbox">
                                        <Form.Check type="checkbox" label="I agree to the terms and conditions" />
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
            </div>);
    }
}
export default SignUp;