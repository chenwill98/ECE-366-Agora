import React, { Component } from "react";
import { Form, Button, Card } from "react-bootstrap";
import CenterView from '../components/CenterView.js';
import Navigation from '../components/Navigation.js';

class Login extends Component {
    constructor(props) {
        super(props);

        this.state = {
            email: "",
            password: ""
        };
    }

    handleChange = event => {
        this.setState({
            [event.target.id]: event.target.value
        });
    }

    handleSubmit = event => {
        event.preventDefault();
    }

    Login = () => {
        console.log('this.state,', this.state);
    }

    render() {
        return (
            <div className='mt-5'>
                <Navigation/>
                <CenterView>
                    <Card border="primary" style={{ width: '40rem'}}>
                        <Card.Body>
                            <Card.Title>Login Form</Card.Title>
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