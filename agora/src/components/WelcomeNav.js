import React, { Component } from 'react';
import { Navbar, Nav } from "react-bootstrap";
import "../styles/Navigation.css";


export default class Navigation extends Component {
    render() {
        return (
            <Navbar fixed="bottom" bg="primary" variant="dark" expand="lg">
                <Navbar.Brand href="/">
                    <img
                        src={require("../images/Brand2.png")}
                        width="20"
                        height="20"
                        alt={"Brand2"}
                    />
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="mr-auto">
                        <Nav.Link href="/groups">
                            <i className="fas fa-users"></i>
                            &nbsp; Groups
                        </Nav.Link>
                        <Nav.Link href="/events">
                            <i className="far fa-calendar-alt"></i>
                            &nbsp; Events
                        </Nav.Link>
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