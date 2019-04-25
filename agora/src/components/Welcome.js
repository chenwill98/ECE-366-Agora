import React, { Component } from 'react';
import { Card } from "react-bootstrap";
import "../styles/Welcome.css";
import CenterView from '../components/CenterView.js';
import WelcomeNav from './WelcomeNav.js';
import Jumbotron from 'react-bootstrap/Jumbotron'
import Button from 'react-bootstrap/Button'
import ListGroup from 'react-bootstrap/ListGroup'
import Badge from 'react-bootstrap/Badge'
import Alert from 'react-bootstrap/Alert'
import SinglebObjectView from '../components/SingleObjectView.js';

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
		

		{/***********************
		<Jumbotron> 
			<h1> Welcome to Agora! </h1>
			<p> Experience the latest in bleeding-edge event organization technology and meet up with your
                            friends today!
			</p>
			<p> <Button variant="primary" href="/signup"> {"Let's get started."} </Button>
		        </p>
		</Jumbotron>
ADD ALL OF THIS BACK IN*******/}
{/***************************************************/}
                    <SinglebObjectView> 
			<Jumbotron>
				<h1>Event</h1>
				<Button href={"/group/" }> Go to group.</Button> 				
				<Alert variant="primary" style={{width : '30rem'}}><b>Description:</b>Doing stuff</Alert>
				
				<Alert variant="primary" style={{width : '30rem'}}><b>Location:</b> {"Place we're gonna go"} </Alert>
				<Alert variant="primary" style={{width : '30rem'}}><b>Time:</b> {"Time were gonna meet"} </Alert>
			</Jumbotron>	

			<CenterView>
			<Card style={{width : '50rem'}}>
			<Card.Header as="h2">
			 Attendees
			 </Card.Header>
			<ListGroup variant="flush"> 
                                <ListGroup.Item> Person One</ListGroup.Item>
                                <ListGroup.Item> Person One</ListGroup.Item>
                                <ListGroup.Item> Person One</ListGroup.Item>
                                <ListGroup.Item> Person One</ListGroup.Item>
                                <ListGroup.Item> Person One</ListGroup.Item>
			  </ListGroup>	
			</Card>
			</CenterView>
                    </SinglebObjectView> 
            </div>
        )
    };
}
