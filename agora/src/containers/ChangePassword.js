import React, { Component } from 'react';
import { Form, Button, Card, Alert } from "react-bootstrap";
import CenterView from '../components/CenterView.js';
import Navigation from '../components/Navigation.js';
import {Backend_Route} from "../BackendRoute";

class ChangePassword extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: Backend_Route.ip,
            port: Backend_Route.port,

            user_id: localStorage.getItem('userID'),
            new_pass: '',
            new_pass2: '',
            old_pass: '',

            // error related states
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

    //sends a request to create a group that doesn't already exist
    changePassword = () => {
        if (this.state.new_pass !== this.state.new_pass2) {
            this.setState( {
                error: true,
                error_msg: "New Passwords do not match!"
            })
            return;
        }

        /**
         * CHANGE PASSWORD
         */
        fetch(`${this.state.ip}:${this.state.port}/user/${this.state.user_id}/change-password`,
        {
                method: "Post",
                credentials: "include",
                body: JSON.stringify({
                    'oldpass': this.state.old_pass,
                    'newpass': this.state.new_pass
                })
            }
        )
        .catch(error => {
            this.setState({
                error: true,
                error_msg: "Your old password does not match!"
            });
            console.log("Error changing password: " + error.message);
        })
        .then(res => {
            if (res.status === 200) {
                console.log("Successfully changed password.");
                this.props.history.push('/home');

            }
            else {
                this.setState({
                    error: true,
                    error_msg: "Error changing your password."
                })
                console.log("Error changing password.");
            }
        });
    };



    render() {
        return (
            <div className='mt-5'>
                <Navigation/>
                    <CenterView>
                        <Card border="primary" style={{ width: '40rem'}}>
                        <Card.Header as="h5">Change Password</Card.Header>
                        <Card.Body>
                            <Card.Text>
                                {this.state.error ?
                                    <Alert dismissible variant="danger"
                                           onClick={() => this.setState({error:false})}>
                                        {this.state.error_msg}
                                    </Alert>
                                    : ''}
                                <Form onSubmit={this.handleSubmit}>
                                    <Form.Group controlId="old_pass">
                                        <Form.Label>Old Password</Form.Label>
                                        <Form.Control type="old_pass"
                                                      placeholder="..."
                                                      onChange={this.handleChange}/>
                                    </Form.Group>

                                    <Form.Group controlId="new_pass">
                                        <Form.Label>New Password</Form.Label>
                                        <Form.Control type="new_pass"
                                                      placeholder="..."
                                                      onChange={this.handleChange}/>
                                    </Form.Group>

                                    <Form.Group controlId="new_pass2">
                                        <Form.Label>Confirm New Password</Form.Label>
                                        <Form.Control type="new_pass2"
                                                      placeholder="..."
                                                      onChange={this.handleChange}/>
                                    </Form.Group>


                                    <Button variant="primary" type="submit" onClick={() => this.changePassword()}>
                                        Change Password
                                    </Button>
                                </Form>
                            </Card.Text>
                        </Card.Body>
                    </Card>
                </CenterView>
            </div>
        );
    }
}
export default ChangePassword;