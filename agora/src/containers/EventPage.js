import React, { Component } from "react";
import axios from "axios";
import SinglebObjectView from '../components/SingleObjectView.js';
import Navigation from '../components/Navigation.js';
import {Card, CardColumns} from "react-bootstrap";
import {Backend_Route} from "../BackendRoute.js";
import Button from "react-bootstrap/Button";
import Footer from "../components/Footer";
import Cookies from "universal-cookie";
import * as emailjs from "emailjs-com";


const cookies = new Cookies();

let init = {
    method: "Get",
    credentials: "include"
};


class EventPage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: Backend_Route.ip,
            port: Backend_Route.port,

            // events related states
            event_id: this.props.match.params.event_id,
            event_description: "description",
            event_name: "Name",
            event_gid: "456",
            event_g_name: "",
            event_location: "Here",
            event_date: "03/18/1994",
            event_users: [],

            // if in a user session
            user_id: localStorage.getItem('userID'),
            user_attendance: -2,

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }


    //fetches all data when the component mounts (called right after constructor)
    componentDidMount () {
        /**
         * GET THE EVENT INFO
         */
        axios.get( `${this.state.ip}:${this.state.port}/event/${this.state.event_id}`)
        .then(res => {
            this.setState( {
                event_description: res.data.description,
                event_name: res.data.name,
                event_gid: res.data.gid,
                event_location: res.data.location,
                event_date: res.data.date
            });

            /**
             * GET THE GROUP NAME OF THE GROUP WHO OWNS THE EVENT
             */
            axios.get( `${this.state.ip}:${this.state.port}/group/${this.state.event_gid}`)
            .then(res => {
                this.setState( {
                    event_g_name: res.data.name
                });
                console.log("Success getting the event's group owner.");
            })
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg: error.message
                });
                console.log("Error requesting event's parent group info: " + error.message);
            });
        })
        .catch(error => {
            this.setState({
                error: true,
                error_msg:  "Error requesting the details of an event: " + error.message
            });
            console.log("Error requesting event: " + error.message);
        });

        /**
         * CHECK YOUR ATTENDANCE STATUS FOR THE EVENT
         */
        fetch( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}/event/${this.state.event_id}/is-attending`, init)
        .then(res => {
            res.json().then(data => ({
                    data: data,  /***  Integer with value of: 1, 2, or 3. 1:attending \ 2:maybe \ 3:not attending. **/
                    status: res.status
                })
            ).then(res => {
                if (res.status === 200) {
                    this.setState({
                        user_attendance: res.data
                    });
                    console.log("Got the user attendance: " + this.state.user_attendance);
                }
            });
        });

        this.getEventUsers();

        if (!this.state.intervalSet) {
            let interval = setInterval(this.getData, 1000);
            this.setState({intervalSet: interval})
        }
    }


    //kills the process
    componentWillUnmount() {
        if (this.state.intervalIsSet) {
            clearInterval(this.state.intervalIsSet);
            this.setState({ intervalIsSet: null });
        }
    }

    /**
     * GET THE EVENT'S USERS
     */
    getEventUsers() {
        axios.get(`${this.state.ip}:${this.state.port}/event/${this.state.event_id}/get-users`)
        .then( res => {
            this.setState( {
                event_users: res.data
            });
            console.log("Success getting the event's users.");
        });
    }


    /**
     * joinEvent - Updates a user's attendance to an event to attending.
     */
    joinEvent() {
        fetch(`${this.state.ip}:${this.state.port}/user/${this.state.user_id}/event/${this.state.event_id}/join`,
            {
                method: "Get",
                credentials: "include"
            }
        )
            .catch( error => {
                this.setState({
                    error: true,
                    error_msg: error.message
                });
                console.log("Error updating your event attendance: " + error.message);
            })
            .then(res => {
                if (res.status === 200) {
                    this.setState( {
                        user_attendance: 1
                    });
                    console.log("Successfully attending event.");
                    this.getEventUsers();
                }
                else {
                    this.setState({
                        error: true,
                        error_msg: "Response: " + res.status
                    });
                    console.log("Error joining group, status:" + res.status);
                }
            });
    }


    /**
     * leaveEvent- Changes the attendance of an event to not attending.
     */
    leaveEvent() {
        fetch(`${this.state.ip}:${this.state.port}/user/${this.state.user_id}/event/${this.state.event_id}/leave`,
            {
                method: "Get",
                credentials: "include"
            }
        )
            .catch( error => {
                this.setState({
                    error: true,
                    error_msg: error.message
                });
                console.log("Error updating your event attendance: " + error.message);
            })
            .then(res => {
                if (res.status === 200) {
                    this.setState( {
                        user_attendance: 3
                    });
                    console.log("Successfully updated attendance of event to not attending.");
                    this.getEventUsers();
                }
                else {
                    this.setState({
                        error: true,
                        error_msg: "Error updating your event attendance status: " + res.status
                    })
                    console.log("Error updating event status event.");
                }
            });
    }

    massEmail = () => {
        let template_params = {
            "reply_to": this.state.user_email,
            "message_html": "We don't actually have the appropriate routes to retrieve a user's password lmao"
        };

        let service_id = "agora_service";
        let template_id = "template_hlWoe6JV";
        let user_id = "user_L6P4JRoGpemcRWO1WNmcG";
        emailjs.send(service_id, template_id, template_params, user_id)
            .then(function(response) {
                console.log('SUCCESS!', response.status, response.text);
            }, function(error) {
                console.log('FAILED...', error);
            });
    }

    /**
     * RENDERING
     */
        render() {
        if (this.state.error) {
            return (
                <div className='mt-5'>
                    <Navigation/>
                    <SinglebObjectView>
                        <p> {this.state.error_msg}</p>
                    </SinglebObjectView>
                </div>
            );
        }
        else {
            return (
                <div className='mt-5'>
                    <Navigation/>

                    <SinglebObjectView>
                        <h1>Name: {this.state.event_name}</h1>
                        <h5>Description: {this.state.event_description}</h5>
                        <h5>Location: {this.state.event_location}</h5>
                        <h5>Date: {this.state.event_date.substr(0, 16)}</h5>
                        {cookies.get("USER_TOKEN") && this.state.user_id  && this.state.user_attendance === 3 && <Button variant="primary" onClick={() => this.joinEvent()}>Attend Event</Button>}
                        {this.state.user_id  && this.state.user_attendance === 1 && <Button variant="primary" onClick={() => this.leaveEvent()}>Not attending after all?</Button>}
                        <hr/>
                        <Card>
                            <Card.Body>
                                <h4> <Card.Link href={"/group/" + this.state.event_gid}>{this.state.event_g_name}</Card.Link></h4>
                            </Card.Body>
                        </Card>
                        <hr/>
                        <Card>&nbsp;
                            <h3>&nbsp;&nbsp;&nbsp;Users attending:</h3>
                            <CardColumns>
                                    {this.state.event_users.map((user, i) =>
                                        <Card key={i} user={user}>
                                            <h5><Card.Body>{user.first_name} {user.last_name} {user.email}</Card.Body></h5>
                                        </Card>
                                    )}
                            </CardColumns>
                        </Card>
                        <Footer/>
                    </SinglebObjectView>
                </div>
            );
        }
    }
}


export default EventPage;
