import React, { Component } from "react";
import DashboardTabs from '../components/DashboardTabs';
import DashboardList from '../components/DashboardList';
import "../styles/App.css";
import Button from "react-bootstrap/Button";
import { Redirect } from 'react-router-dom'
import {Backend_Route} from "../BackendRoute.js";
import Cookies from "universal-cookie";
import Navigation from "../components/Navigation.js";

const cookies = new Cookies();

let init = {
    method: "Get",
    credentials: "include"
};


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
            user_last_name: "",
            user_first_name: "",
            user_email: "",
            user_groups: [],
            user_events: [],

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }
    componentDidMount () {

        fetch( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}/get-user`, init)
        .catch(error => {
            this.setState({
                error: true,
                error_msg:  "Error requesting the details of an user: " + error.message
            });
            console.log("Error requesting user: " + error.message);
        })
        .then(res => {
            res.json().then(data => ({
                    data: data,
                    status: res.status
                })
            ).then(res => {
                this.setState( {
                    user_first_name: res.data.first_name,
                    user_last_name: res.data.last_name,
                    user_email: res.data.email
                });
            })
        });

        //fetches all of the user's groups
        fetch( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}/groups`, init)
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg:  "Error requesting list of user groups: " + error.message
                });
                console.log("Error requesting user groups: " + error.message);
            })
            .then(res => {
                res.json().then(data => ({
                        data: data,
                        status: res.status
                    })
                ).then(res => {
                    if (res.data !== '') {
                        this.setState( {
                            user_groups: res.data,
                        });
                    }
                })
            });

        //fetches all of the user's events
        fetch( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}/events`, init)
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg:  "Error requesting list of user events: " + error.message
                });
                console.log("Error requesting user events: " + error.message);
            })
            .then(res => {
                res.json().then(data => ({
                        data: data,
                        status: res.status
                    })
                ).then(res => {
                    console.log('eventsssss:' + res.data);
                    if (res.data !== '') {
                        this.setState( {
                            user_events: res.data,
                        });
                    }
                })
            });
    }

    //kills the process
    componentWillUnmount() {
        if (this.state.intervalIsSet) {
            clearInterval(this.state.intervalIsSet);
            this.setState({ intervalIsSet: null });
        }
    }

    ////////////////////////////
    render() {
        if (cookies.get("USER_TOKEN") == null)
            return <Redirect to="/login"/>;
        else {
            return (
                <div>
                    <Navigation/>

                    <h1>Name {formatName(this.state.user_first_name, this.state.user_last_name)}! </h1>

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
}

export default Dashboard;