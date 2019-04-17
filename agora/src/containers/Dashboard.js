import React, { Component } from "react";
import DashboardTabs from '../components/DashboardTabs';
import DashboardList from '../components/DashboardList';
import axios from 'axios'
import "../styles/App.css";
import Button from "react-bootstrap/Button";
import { Redirect } from 'react-router-dom'
import {Backend_Route} from "../BackendRoute.js";
require('../styles/Dashboard.css');


function formatName(first, last) {
    return first + ' ' + last;
}


class Dashboard extends Component{
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: Backend_Route.ip,
            port: Backend_Route.port,

            // user related states
            user_id: localStorage.getItem('userID'), //
            user_cookie: localStorage.getItem('cookie'), //
            user_last_name: "",
            user_first_name: "",
            user_email: "",
            user_groups: [], //
            user_events: [], //

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }
    componentDidMount () {

        var config = {
            headers: {Authorization: `aaaa`}
        };

        axios.get( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}/get-user`, config)
        .then(res => {
            this.setState( {
                user_first_name: res.data.first_name,
                user_last_name: res.data.last_name,
                user_email: res.data.email
            });
        })
        .catch(error => {
            this.setState({
                error: true,
                error_msg:  "Error requesting the details of an user: " + error.message
            });
            console.log("Error requesting user: " + error.message);
        });

        if (!this.state.intervalSet) {
            //let interval = setInterval(this.getData, 1000);
            //this.setState({intervalSet: interval})
        }
    }

    ///////////////////////

    getData = () => {
        //fetches all of the user's groups
        axios.get( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}/groups`,
            { headers: {Origin: `aaaa`}
            }
            )
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

        //fetches all of the user's events
        axios.get( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}/events`,
            { headers: {Origin: `aaaa`} } )
            .then(res => {
                this.setState( {
                    user_events: res.data.events,
                });
            })
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg:  "Error requesting list of user events: " + error.message
                });
                console.log("Error requesting user events: " + error.message);
            });
    };

    //////////////////////////////////

    //kills the process
    componentWillUnmount() {
        if (this.state.intervalIsSet) {
            clearInterval(this.state.intervalIsSet);
            this.setState({ intervalIsSet: null });
        }
    }

    ////////////////////////////
    render() {
        if (this.state.user_cookie == null)
            return <Redirect to="/login"/>;
        else
            return (
                <div>
                    <h1>Name {formatName(this.state.user_name, this.state.user_surname)}! </h1>

                    <Button variant="primary">Change Password</Button>

                    <DashboardTabs>
                        <div label="Groups">
                            <DashboardList contacts={this.state.user_groups} />
                        </div>
                        <div label="Events">
                            <DashboardList contacts={this.state.user_events} />
                        </div>
                    </DashboardTabs>
                </div>
            );
    }
}

export default Dashboard;