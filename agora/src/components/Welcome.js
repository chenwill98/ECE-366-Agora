import React, { Component } from 'react';
import { Card } from "react-bootstrap";
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
                                Experience the latest in bleeding-edge event organizing technology with our
                                never-before-seen tech stack (no really, literally no one in their right mind
                                would use the unholy combination of react, Nginx, Apollo, and mySQL over straight up
                                LAMP or MERN).
                            </Card.Text>

                        </Card.Body>
                    </Card>
                </CenterView>
            </div>
        )
    };
}