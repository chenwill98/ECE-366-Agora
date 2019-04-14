import React, { Component } from "react";
import axios from "axios";
import { Form, Button, Card } from "react-bootstrap";
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

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }

    //fetches all data when the component mounts
    componentDidMount() {
        // this.getData();
        if (!this.state.intervalSet) {
            let interval = setInterval(this.getData, 1000);
            this.setState({intervalSet: interval})
        }
    }

    //kills the process
    componentWillUnmount() {
        if (this.state.intervalIsSet) {
            clearInterval(this.state.intervalIsSet);
            this.setState({ intervalIsSet: null });
        }
    }

    //method for fetching data
    // getData = () => {
    //     fetch("http://localhost:8080/somestuff")
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

    //posts posts the input email, password, and then returns the response
    Login = () => {
        axios.post("http://199.98.27.114:8080/login", {
            email: this.state.user_email,
            pass: this.state.user_password
        })
            .then(res => {
                localStorage.setItem('cookie', res.data);
            })
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg:  "Error logging in user: " + error.message
                });
                console.log("Error requesting cookie: " + error.message);
            });
    };

    render() {
        return (
            <div className='mt-5'>
                <CenterView>
                    <Card border="primary" style={{ width: '40rem'}}>
                        <Card.Body>
                            <Card.Title>Login</Card.Title>
                            <Card.Text>
                                <Form onSubmit={this.handleSubmit}>
                                    <Form.Group controlId="email">
                                        <Form.Label>Email address</Form.Label>
                                        <Form.Control type="email"
                                                      placeholder="Enter email"
                                                      onChange={this.handleChange}/>

                                    </Form.Group>
                                    <Form.Group controlId="password">
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
            </div>);
    }
}
export default Login;