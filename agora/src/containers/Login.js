import React, { Component } from "react";
import axios from "axios";
import { Form, Button, Card } from "react-bootstrap";
import CenterView from '../components/CenterView.js';
import Navigation from '../components/Navigation.js';

class Login extends Component {
    constructor(props) {
        super(props);

        this.state = {
            data: [],
            email: "",
            password: "",
            id: 0,
            cookie: "",
            intervalSet: false,
            error: false
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
        return axios.post("http://localhost:8080/login",
            JSON.stringify({email: this.state.email, pass: this.state.password})
        ).then(response => {
            return response.data;
        })
    };

    // checkResponse = (response) => {
    //     if (response.status == 403) {
    //
    //     } else {
    //
    //     }
    // }

    render() {
        return (
            <div className='mt-5'>
                <Navigation/>
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
                                    <Button variant="primary" type="submit" onClick={() =>
                                        this.Login().then(data => {
                                            localStorage.setItem('cookie', data);
                                        })
                                    }>
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