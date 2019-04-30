import React, { Component } from "react";
import "../styles/App.css";
import Button from "react-bootstrap/Button";
import { Redirect } from 'react-router-dom'
import {Backend_Route} from "../BackendRoute.js";
import Navigation from "../components/Navigation.js";
import Cookies from "universal-cookie";
import Tabs from 'react-bootstrap/Tabs';
import Tab from 'react-bootstrap/Tabs';
import "../styles/DashboardList.css";
import Jumbotron from 'react-bootstrap/Jumbotron'
import { Card, CardColumns } from "react-bootstrap";
import Footer from "../components/Footer";

const cookies = new Cookies();

let init = {
    method: "Get",
    credentials: "include"
};

function ContactGroup(props) {
    return (

    <Card>
        <Card.Header as="h5">
        <Card.Title><a href={"/group/" + props.ID}><span>{props.name}</span></a></Card.Title>
        </Card.Header>
        <Card.Body>
            <Card.Text>
                {props.DESC}
            </Card.Text>
        </Card.Body>
    </Card>

    );
}
function ContactEvent(props) {
    return (
        <CardColumns>;
            <Card>
                <Card.Header as="h5">
                    <Card.Title><a href={"/event/" + props.ID}><span>{props.name}</span></a></Card.Title>
                </Card.Header>
                <Card.Body>
                    <Card.Text>
                        {props.DESC}
                    </Card.Text>
                </Card.Body>
            </Card>
        </CardColumns>
    );
}

function GroupList(props) {
    if (props == null || props.Groups == null ) {
        return;
    }
    else {
        return (
            <div>
                {props.Groups.map(c => <ContactGroup key={c.id} name={c.name} ID={c.id} DESC={c.description}/>)}
            </div>
        );
    }
}

function EventList(props) {
    if (props == null || props.Events == null ) {
        return;
    }
    else {

        return (
            <div>
                {props.Events.map(c => <ContactEvent key={c.id} name={c.name} ID={c.id}/>)}
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
                    <Jumbotron fluid>
                        <container>
                        <h1>Welcome back  {formatName(this.state.user_first_name, this.state.user_last_name)}!</h1>
                            <h4> <font color="gray"> Email:  {this.state.user_email} </font></h4>
                            <p>
                            <Button variant="primary" size="lg"href="ChangePassword">Change Password</Button>
                            &nbsp;&nbsp;&nbsp;&nbsp;
                            <Button variant="primary" size="lg"href="groupCreate" > &nbsp;&nbsp;&nbsp;&nbsp;Make Group&nbsp;&nbsp;&nbsp;&nbsp;</Button>
                        </p>
                        </container>
                    </Jumbotron>;
                    <Card>
                    <Tabs defaultActiveKey="groups" transition={false} id="tabs">
                        <Tab eventKey="groups" title="Groups">
                            <card>
                                <Card.Body>
                                <CardColumns>
                                    <GroupList Groups={this.state.user_groups} />
                                </CardColumns>
                                </Card.Body>
                            </card>
                        </Tab>
                        <Tab eventKey="events" title="Events">
                            <card>
                                <Card.Body>
                                <CardColumns>
                            <EventList Events={this.state.user_events} />
                            </CardColumns>
                                </Card.Body>
                            </card>
                        </Tab>
                    </Tabs>;
                    </Card>
                    <div>
                    <Footer/>
                    </div>
                </div>
            );
        }
    }
}

export default Dashboard;
