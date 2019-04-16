import React, { Component } from "react";
import DashboardTabs from '../components/DashboardTabs';
import DashboardList from '../components/DashboardList';
import axios from 'axios'
import "../styles/App.css";
import Button from "react-bootstrap/Button";
require('../styles/Dashboard.css');


function formatName(first, last) {
    return first + ' ' + last;
}


class Dashboard extends Component{
    constructor(props) {
        super(props);
        this.state = {
            // backend related states
            ip: "http://localhost",
            port: "8080",

            // user related states
            user_id: "", //
            user_cookie: "", //
            user_name: "",
            user_surname: "",
            user_email: "",
            user_password: "",
            user_groups: [], //
            user_events: [], //

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }
    componentDidMount () {
        // get the user info
        axios.get( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}`, { headers:{
                Cookie: `USER_TOKEN=${user_cookie}`
            }})
            .then(res => {
            this.setState( {
                user_name: res.data.name,
                user_surname: res.data.surname,
                user_email: res.data.email,
                user_password: res.data.password
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
            let interval = setInterval(this.getData, 1000);
            this.setState({intervalSet: interval})
        }
    }
    ///////////////////////
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
//fetches all of the user's events
    axios.get( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}/events`, { headers:{
    Cookie: `USER_TOKEN=${user_cookie}`
    }})
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
    return (
    <div>
    <h1>Name {formatName(user_name, user_surname)}! </h1>
    <Button variant="primary">Change Password</Button>
    <DashboardTabs>
    <div label="Groups">
    <DashboardList contacts={user_groups} />
    </div>
    <div label="Events">
    <DashboardList contacts={user_events} />
    </div>
    </DashboardTabs>
    </div>
    );
    }
    }

    export default Dashboard;