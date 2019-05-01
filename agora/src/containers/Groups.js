import React, { Component } from 'react';
import { Card, CardColumns } from "react-bootstrap";
import axios from "axios";
import Navigation from "../components/Navigation.js";
import CenterView from '../components/CenterView.js';
import Footer from "../components/Footer";
import {Backend_Route} from "../BackendRoute.js";
import Cookies from "universal-cookie";

const cookies = new Cookies();

let init = {
    method: "Get",
    credentials: "include"
};


export default class Groups extends Component {
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
            other_groups: [],

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }

    //fetches all data when the component mounts
    componentDidMount() {
        this.getData();
        this.removeDup(this.state.other_groups, this.state.user_groups);
        // if (!this.state.intervalSet) {
        //     let interval = setInterval(this.getData, 1000);
        //     this.setState({intervalSet: interval});
        // }
    }

    // kills the process
    componentWillUnmount() {
        // if (this.state.intervalSet) {
        //     clearInterval(this.state.intervalSet);
        //     // this.setState({ intervalSet: null });
        // }
    }

    getData = () => {
        if (this.state.user_id && cookies.get("USER_TOKEN")) {
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
                )
                    .then(res => {
                        if (res.data !== '') {
                            console.log("Successfully got user groups.");
                            this.setState( {
                                user_groups: res.data
                            });
                        }
                    })
            });
        }

        //fetches all of the groups available for browsing
        axios.get( `${this.state.ip}:${this.state.port}/group/get-groups`)
        .then(res => {
            this.setState( {
                other_groups: res.data
            });
            console.log("Successfully got all groups.");
        })
        .catch(error => {
            this.setState({
                error: true,
                error_msg:  "Error requesting list of all groups: " + error.message
            });
            console.log("Error requesting all groups: " + error.message);
        });

        //fetches user data to display
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
    };

    // removes the duplicate objects so other_groups doesn't contain redundant groups to the user_groups
    removeDup = (array, subset) => {
        this.setState({other_groups: array.filter(obj => !subset.includes(obj))});
    };

    render() {
        if (this.state.error) {
            return (
                <div className='p-5'>
                    <Navigation/>
                    <CenterView>
                        <Card border="primary" style={{width: '40rem'}}>
                            <Card.Body>
                                <Card.Title>Error</Card.Title>
                                <Card.Text>
                                    Oops, {this.state.error_msg} :/
                                </Card.Text>
                            </Card.Body>
                        </Card>
                    </CenterView>
                </div>
            );
        } else {
            return (
                <div>
                    <Navigation/>
                    <nav className="navbar bg-white sticky-top flex-md-nowrap p-1">
                        <a className="navbar-brand text-center col-sm-3 col-md-2 mr-0" href="/account">{this.state.user_first_name} {this.state.user_last_name}</a>
                        <input className="form-control form-control-dark w-100" type="text" placeholder="Search"/>
                    </nav>
                    <div className="container-fluid">
                        <div className="row">
                            {cookies.get("USER_TOKEN") &&
                                <nav className="col-md-2 d-none d-md-block bg-light sidebar">
                                    <div className="sidebar-sticky">
                                        <ul className="nav flex-column">
                                            <li className="nav-item">
                                                <a className="nav-link active" href="/groupcreate">
                                                    <i className="fas fa-plus-circle"></i>
                                                    &nbsp; Create Group
                                                </a>
                                            </li>
                                        </ul>
                                    </div>
                                </nav>
                            }

                            <main role="main" className="col-md-9 ml-sm-auto col-lg-10 pt-3 px-4">
                                {cookies.get("USER_TOKEN") && <Card>
                                    <Card.Body>
                                        <div className="text-sm-left mb-3 text-center text-md-left mb-sm-0 col-12 col-sm-4">
                                            <h3>Your Groups</h3>
                                        </div>
                                        <hr/>
                                        <CardColumns>
                                            {this.state.user_groups.map((groups, i) =>
                                                <Card key={i} group={groups}>
                                                    <Card.Header as="h5">
                                                        <Card.Link href={"/group/" + groups.id}>
                                                            {groups.name}
                                                        </Card.Link>
                                                    </Card.Header>
                                                    <Card.Body>
                                                        {groups.description}
                                                    </Card.Body>
                                                </Card>
                                            )}
                                        </CardColumns>
                                    </Card.Body>
                                </Card>
                                }
                                <hr/>
                                <Card>
                                    <Card.Body>
                                        <div className="text-sm-left mb-3 text-center text-md-left mb-sm-0 col-12 col-sm-4">
                                            <h3>All Groups</h3>
                                        </div>
                                        <hr/>
                                        <CardColumns>
                                            {this.state.other_groups.map((groups, i) =>
                                                <Card key={i} group={groups}>
                                                    <Card.Header as="h5">
                                                        <Card.Link href={"/group/" + groups.id}>
                                                            {groups.name}
                                                        </Card.Link>
                                                    </Card.Header>
                                                    <Card.Body>
                                                        {groups.description}
                                                    </Card.Body>
                                                </Card>
                                            )}
                                        </CardColumns>
                                    </Card.Body>
                                </Card>
                            </main>
                        </div>
                        <div className='pl-5'>
                            <Footer/>
                        </div>
                    </div>
                </div>
            );
        }
    };
}