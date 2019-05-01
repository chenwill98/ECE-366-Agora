import React, { Component } from 'react';
import { Form, Button, Card, Alert } from "react-bootstrap";
import CenterView from '../components/CenterView.js';
import Navigation from '../components/Navigation.js';
import { Redirect } from 'react-router-dom'
import {Backend_Route} from "../BackendRoute";
import Cookies from "universal-cookie";


const cookies = new Cookies();

class GroupCreate extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: Backend_Route.ip,
            port: Backend_Route.port,

            // group related states
            group_id: "",
            group_description: "",
            group_name: "",
            group_success: false,

            user_id: localStorage.getItem('userID'),
            user_isAdmin: true,
            user_session: false,

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
    GroupCreate = () => {
        fetch(`${this.state.ip}:${this.state.port}/user/${this.state.user_id}/create-group`,
        {
                method: "Post",
                credentials: "include",
                body: JSON.stringify({
                    'name': this.state.group_name,
                    'description': this.state.group_description
                })
            }
        )
        .catch(error => {
            this.setState({
                error: true,
                error_msg: "Error creating the group: " + error.message
            });
            console.log("Error posting group: " + error.message);
        })
        .then(res => {
            if (res.status === 200) {
                if (!this.state.error) {
                    this.setState({
                        group_success: true
                    });
                }
                console.log("Successfully created group.");
            }
            else {
                this.setState({
                    error: true,
                    error_msg: res.statusText
                })
                console.log("Error creating group.");
            }
        });
    };



    render() {
        // confirm that the user session exists, otherwise redirect to login.
        if (!cookies.get("USER_TOKEN")) {
            return <Redirect to="/login"/>;
        }
        else if (this.state.group_success) {
            return (
                <Redirect to="/home"/>
            );
        } else if (this.state.error) {
                    return (
                        <div className='mt-5'>
                            <Navigation/>
                            <CenterView>
                                <p>Error: {this.state.error_msg}</p>
                            </CenterView>
                        </div>
                    );
        } else {
            return (
                <div className='mt-5'>
                    <Navigation/>
                        <CenterView>
                            <Card border="primary" style={{ width: '40rem'}}>
                            <Card.Header as="h5">Group Creation</Card.Header>
                            <Card.Body>
                                <Card.Text>
                                    {this.state.error ?
                                        <Alert dismissible variant="danger"
                                               onClick={() => this.setState({error:false})}>
                                            {this.state.error_msg}
                                        </Alert>
                                        : ''}
                                    <Form onSubmit={this.handleSubmit}>
                                        <Form.Group controlId="group_name">
                                            <Form.Label>Group Name</Form.Label>
                                            <Form.Control type="name"
                                                          placeholder="Name"
                                                          onChange={this.handleChange}/>
                                        </Form.Group>

                                        <Form.Group controlId="group_description">
                                            <Form.Label as="textarea">Group Description</Form.Label>
                                            <Form.Control type="description"
                                                          placeholder="Description"
                                                          as="textarea"
                                                          onChange={this.handleChange}/>
                                        </Form.Group>
                                        <Button variant="primary" type="submit" onClick={() => this.GroupCreate()}>
                                            Create Group
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



}
    export default GroupCreate;