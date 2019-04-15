import React, { Component } from 'react';
import { Card } from "react-bootstrap";
import axios from "axios";
import Navigation from "../components/Navigation.js";
import CenterView from '../components/CenterView.js';

export default class Groups extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: "http://localhost",
            port: "8080",

            // user related states
            user_id: "",
            user_cookie: "",
            user_groups: [],
            total_groups: [],

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }

    //fetches all data when the component mounts
    componentDidMount() {
        this.setState({user_cookie: localStorage.getItem('cookie')});
        this.getData(this.state.user_cookie);
        if (!this.state.intervalSet) {
            let interval = setInterval(this.getData, 1000);
            this.setState({intervalSet: interval});
        }
    }

    // kills the process
    componentWillUnmount() {
        if (this.state.intervalSet) {
            clearInterval(this.state.intervalSet);
            this.setState({ intervalSet: null });
        }
    }

    getData = (user_cookie) => {
        //fetches all of the user's groups
        axios.get( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}/groups`, { headers:{
                Cookie: `USER_TOKEN=${user_cookie}`
        }})
            .then(res => {
                this.setState( {
                    user_groups: res.data.groups,
                });
            })
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg:  "Error requesting list of user groups: " + error.message
                });
                console.log("Error requesting user groups: " + error.message);
            });

        //fetches all of the groups available for browsing
        axios.get( `${this.state.ip}:${this.state.port}/group/get-groups`)
            .then(res => {
                this.setState( {
                    total_groups: res.data.groups,
                });
            })
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg:  "Error requesting list of all groups: " + error.message
                });
                console.log("Error requesting all groups: " + error.message);
            });
    };

    render() {
        if (this.state.error)
            return (
                <div className='mt-5'>
                    <Navigation/>
                    <CenterView>
                        <Card border="primary" style={{ width: '40rem'}}>
                            <Card.Body>
                                <Card.Title>Error</Card.Title>
                                <Card.Text>
                                    Oops, error {this.state.error_msg} occurred :/
                                </Card.Text>
                            </Card.Body>
                        </Card>
                    </CenterView>
                </div>
            );
        else {
            return (
                <div className='mt-5'>
                    <Navigation/>
                    <CenterView>
                        <Card>
                            <Card.Header as="h5">Your groups</Card.Header>
                        </Card>
                        {this.state.user_groups.map((groups, i) =>
                            <Card key={i} group={groups}>

                            </Card>
                        )}
                        <Card>
                            <Card.Header as="h5">All groups</Card.Header>
                        </Card>
                        {this.state.total_groups.map((groups, i) =>
                            <Card key={i} group={groups}>

                            </Card>
                        )}
                    </CenterView>
                </div>
            );
        }
    };
}