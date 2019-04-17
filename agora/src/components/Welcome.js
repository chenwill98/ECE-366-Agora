import React, { Component } from 'react';
import { Card, Jumbotron } from "react-bootstrap";
import "../styles/Welcome.css";
import CenterView from '../components/CenterView.js';
import WelcomeNav from './WelcomeNav.js';

export default class Welcome extends Component {
    render() {
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
    };
}