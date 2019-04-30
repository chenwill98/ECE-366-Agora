import React, { Component } from "react";
import "../styles/App.css";
import Button from "react-bootstrap/Button";
import { Redirect } from 'react-router-dom'
import {Backend_Route} from "../BackendRoute.js";
import Navigation from "../components/Navigation.js";
import Cookies from "universal-cookie";
import Tabs from 'react-bootstrap/Tabs';
import Tab from 'react-bootstrap/Tabs';
import Sonnet from 'react-bootstrap/Tabs';
import "../styles/DashboardList.css";

const cookies = new Cookies();

let init = {
    method: "Get",
    credentials: "include"
};

function ContactGroup(props) {
    return (
        <div className="contact">
            <a href={"/group/" + props.ID}><span>{props.name}</span></a>
        </div>
    );
}
function ContactEvent(props) {
    return (
        <div className="contact">
            <a href={"/Event/" + props.ID}><span>{props.name}</span></a>
        </div>
    );
}

function GroupList(props) {

    if (props == null || props.contacts == null ) {
        return;
    }
    else {

        return (
            <div>
                {props.contacts.map(c => <ContactGroup key={c.id} name={c.name} ID={c.id}/>)}
            </div>
        );
    }
}
function EventList(props) {

    if (props == null || props.contacts == null ) {
        return;
    }
    else {

        return (
            <div>
                {props.contacts.map(c => <ContactEvent key={c.id} name={c.name}/>)}
            </div>
        );
    }
}
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
            user_id: localStorage.getItem('userID'),
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


    gotoChangePassword = () => {
        this.props.history.push('/changepassword');
    };

    ////////////////////////////
    render() {
        // confirm that the user session exists, otherwise redirect to login.
        if (!cookies.get("USER_TOKEN")) {
            return <Redirect to="/login"/>;
        }
        else {
            return (
                <div>
                    <Navigation/>
                    <h1>Name: {formatName(this.state.user_first_name, this.state.user_last_name)} </h1>
                    <hr />
                    <Button variant="primary">Change Password</Button>
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    <Button variant="primary" href="groupCreate" > &nbsp;&nbsp;&nbsp;&nbsp;Make Group&nbsp;&nbsp;&nbsp;&nbsp;</Button>
                    <hr />
                    <Tabs defaultActiveKey="groups" transition={false} id="tabs">
                        <Tab eventKey="groups" title="Groups">
                            <Sonnet /> <GroupList contacts={this.state.user_groups} />
                        </Tab>
                        <Tab eventKey="events" title="Events">
                            <Sonnet /> <EventList contacts={this.state.user_events} />
                        </Tab>
                    </Tabs>;
                </div>
            );
        }
    }
}

export default Dashboard;
