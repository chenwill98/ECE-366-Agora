import React, { Component } from 'react';
import {Card, Image} from "react-bootstrap";
import "../styles/Welcome.css";
import CenterView from '../components/CenterView.js';
import Footer from "../components/Footer";

export default class PageNotFound extends Component {
    render() {
        return (
            <div className='p-5'>
                <CenterView>
                    <a href="/home">
                        <Image src={require("../images/Logo.png")}
                               style={{width: '20rem'}}
                               rounded
                               fluid/>
                    </a>
                </CenterView>
                <hr width="50%"/>
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
                <Footer/>
            </div>
        )
    };
}
