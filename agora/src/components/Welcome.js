import React, { Component } from 'react';
import { Carousel, Image } from "react-bootstrap";
import "../styles/Welcome.css";
import { Redirect } from 'react-router-dom'
import CenterView from '../components/CenterView.js';
import WelcomeNav from './WelcomeNav.js';
import Footer from './Footer.js';
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
                                    <p>Welcome to Agora!</p>
                                </Carousel.Caption>
                            </Carousel.Item>
                            <Carousel.Item>
                                <img
                                    className="d-block w-100 h-100"
                                    src={require("../images/NYC.jpg")}
                                    alt="Third slide"
                                />
                                <Carousel.Caption>
                                    <p>The event platform for the masses!</p>
                                </Carousel.Caption>
                            </Carousel.Item>
                            <Carousel.Item>
                                <img
                                    className="d-block w-100 h-100"
                                    src={require("../images/SF.jpg")}
                                    alt="Third slide"
                                />
                                <Carousel.Caption>
                                    <p>Sign up to get started!</p>
                                </Carousel.Caption>
                            </Carousel.Item>
                        </Carousel>
                    </CenterView>
                    <hr width="50%"/>
                    <CenterView>
                        <div className="row">
                            <div className="col-lg-4">
                                <h2 className="text-center">Create groups</h2>
                                <p>With Agora, creating groups with friends has never been easier.</p>
                            </div>
                            <div className="col-lg-4">
                                <h2 className="text-center">Join events</h2>
                                <p>Join your friends' events or explore your interests by finding all available
                                events.</p>
                            </div>
                            <div className="col-lg-4">
                                <h2 className="text-center">Connect</h2>
                                <p>Agora makes it super easy to connect and meet with friends or make new ones!
                                    Sign up today!</p>
                            </div>
                        </div>
                    </CenterView>
                    <Footer/>
                </div>
            )
        }
    };
}