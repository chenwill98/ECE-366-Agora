import React, { Component } from 'react';
import { Card } from "react-bootstrap";
import "../styles/Welcome.css";
import CenterView from '../components/CenterView.js';
import Navigation from '../components/Navigation.js';

export default class PageNotFound extends Component {
    render() {
        return (
            <div className='mt-5'>
                <Navigation/>
                <CenterView>
                    <Card border="primary" style={{ width: '40rem'}}>
                        <Card.Body>
                            <Card.Title>404 Error</Card.Title>
                            <Card.Text>
                                Sorry, this page doesn't exist!
                            </Card.Text>

                        </Card.Body>
                    </Card>
                </CenterView>
            </div>
        )
    };
}