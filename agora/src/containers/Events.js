import React, { Component } from 'react';
import { Card } from "react-bootstrap";
import Navigation from "../components/Navigation.js";
import CenterView from '../components/CenterView.js';

export default class Events extends Component {
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