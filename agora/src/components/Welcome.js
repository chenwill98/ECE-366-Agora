import React, { Component } from 'react';
import { Card } from "react-bootstrap";
import "../styles/Welcome.css";
import CenterView from '../components/CenterView.js';
import WelcomeNav from './WelcomeNav.js';
import Jumbotron from 'react-bootstrap/Jumbotron'
import Button from 'react-bootstrap/Button'

export default class Welcome extends Component {
    render() {
        return (
            <div className='mt-5'>
                <WelcomeNav/>
                {/*<CenterView>
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
		*/}
		<Jumbotron> 
			<h1> Welcome to Agora! </h1>
			<p> Experience the latest in bleeding-edge event organization technology and meet up with your
                            friends today!
			</p>
			<p> <Button variant="primary" href="/signup"> {"Let's get started."} </Button>
		        </p>
		</Jumbotron>		
	
            </div>
        )
    };
}
