import React, { Component } from 'react';
import { Navbar, Nav } from "react-bootstrap";
// import Logo from '../images/AgoraLogo.svg';


export default class Navigation extends Component {
    render() {
        return (
            <Navbar fixed="bottom" bg="primary" variant="dark" expand="lg">
                <Navbar.Brand href="/">
                    <img
                        src={require("../images/Brand2.png")}
                        width="30"
                        height="30"
                        alt={"Brand2"}
                    />
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="mr-auto">
                        <Nav.Link href="/signup">
                            <i className="fas fa-user-plus"></i>
                            &nbsp; Sign Up
                        </Nav.Link>
                        <Nav.Link href="/login">
                            <i className="fas fa-sign-in-alt"></i>
                            &nbsp; Login
                        </Nav.Link>
                    </Nav>
                </Navbar.Collapse>
            </Navbar>
        )
    };
}