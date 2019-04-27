import React, { Component } from 'react';
import { Card } from "react-bootstrap";
import "../styles/Welcome.css";
import { Redirect } from 'react-router-dom'
import CenterView from '../components/CenterView.js';
import WelcomeNav from './WelcomeNav.js';
import Cookies from "universal-cookie";

const cookies = new Cookies();

export default class Welcome extends Component {
    render() {
        if (cookies.get("USER_TOKEN")) {
            return(
                <Redirect to="/home"/>
            );
        }
        else {
            return (
                <div className='mt-5'>
                    <WelcomeNav/>
                    <CenterView>
                        <Card border="primary" style={{ width: '40rem'}}>
                            <Card.Body>
                                <Card.Title>Welcome to Agora!</Card.Title>
                                <Card.Text>
                                    Experience the latest in bleeding-edge event organization technology and meet up with your
                                    friends today!
                                </Card.Text>

                            </Card.Body>
                        </Card>
                    </CenterView>
                </div>
            )
        }
    };
}