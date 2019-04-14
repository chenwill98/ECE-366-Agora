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

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }

    //fetches all data when the component mounts
    componentDidMount() {
        this.setState({user_cookie: localStorage.getItem('cookie')})
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


    }

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
                <div>
                    <Navigation/>

                </div>
            );
        }
    };
}