import React, { Component } from 'react';
import { Carousel, Image, Row, Col } from "react-bootstrap";
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
                <div className='p-5'>
                    <WelcomeNav/>
                    <CenterView>
                        <a href="/">
                            <Image src={require("../images/Logo.png")}
                                   style={{width: '20rem'}}
                                   rounded
                                   fluid/>
                        </a>
                    </CenterView>
                    <hr width="50%"/>
                    <CenterView>
                        <Carousel>
                            <Carousel.Item>
                                <img
                                    className="d-block w-100 h-100"
                                    src={require("../images/CH.jpg")}
                                    alt="Third slide"
                                />
                                <Carousel.Caption>
                                    <p>Now available in Chicago!</p>
                                </Carousel.Caption>
                            </Carousel.Item>
                            <Carousel.Item>
                                <img
                                    className="d-block w-100 h-100"
                                    src={require("../images/NYC.jpg")}
                                    alt="Third slide"
                                />
                                <Carousel.Caption>
                                    <p>Meet up with friends in NYC!</p>
                                </Carousel.Caption>
                            </Carousel.Item>
                            <Carousel.Item>
                                <img
                                    className="d-block w-100 h-100"
                                    src={require("../images/SF.jpg")}
                                    alt="Third slide"
                                />
                                <Carousel.Caption>
                                    <p>San Francisco is perfect for hanging out in!</p>
                                </Carousel.Caption>
                            </Carousel.Item>
                        </Carousel>
                    </CenterView>
                    <hr width="50%"/>
                </div>
            )
        }
    };
}