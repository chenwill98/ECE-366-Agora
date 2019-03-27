import React, { Component } from 'react';
import axios from "axios";
import { Form, Button, Card, Alert } from "react-bootstrap";
import CenterView from '../components/CenterView.js';
import Navigation from '../components/Navigation.js';

class SignUp extends Component {
    constructor(props) {
        super(props);

        this.state = {
            data: [],
            email: "",
            password: "",
            id: 0,
            intervalSet: false,
            error: false
        };
    }

    //fetches all data when the component mounts
    componentDidMount() {
        this.getData();
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
    getData = () => {
        fetch("http://localhost:8080/somestuff")
            .then(data => data.json())
            .then(res => this.setState({ data: res.data }));
    };

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
        if (this.state.email == "error") {
            this.setState({error: true})
        }
        axios.post("http://localhost:8080/user/create", {
            email: this.state.email,
            pass: this.state.password
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
                                        {this.state.error ?
                                            <Alert variant="danger">
                                                This username is invalid somehow, idk
                                            </Alert>
                                            : '' }
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