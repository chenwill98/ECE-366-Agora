import React, { Component } from 'react';
import { Card, CardColumns, Jumbotron } from "react-bootstrap";
import axios from "axios";
import Navigation from "../components/Navigation.js";
import SearchBar from "../components/SearchBar";
import CenterView from '../components/CenterView.js';
import {Backend_Route} from "../BackendRoute.js";
import Cookies from "universal-cookie";
import Footer from "../components/Footer";

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
        this.getData();
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
                total_groups: res.data
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
                <div className='p-5'>
                    <Navigation/>
                    {/*<SearchBar/>*/}
                    <main>
                        <Jumbotron>
                            <div className="text-sm-left mb-3 text-center text-md-left mb-sm-0 col-12 col-sm-4">
                                {/*<span className="text-uppercase page-subtitle">Dashboard</span>*/}
                                <h3>Your Groups</h3>
                            </div>
                            <hr/>
                            <CardColumns>
                                {this.state.user_groups.map((groups, i) =>
                                    <Card key={i} group={groups}>
                                        <Card.Body>
                                            {groups.name}
                                        </Card.Body>
                                    </Card>)
                                }
                            </CardColumns>
                        </Jumbotron>
                        <Jumbotron>
                            <div className="text-sm-left mb-3 text-center text-md-left mb-sm-0 col-12 col-sm-4">
                                {/*<span className="text-uppercase page-subtitle">Dashboard</span>*/}
                                <h3>All Groups</h3>
                            </div>
                            <hr/>
                            <CardColumns>
                                {this.state.total_groups.map((groups, i) =>
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
                        </Jumbotron>
                    </main>
                    <Footer/>
                </div>
            );
        }
    };
}