import React, { Component } from 'react';
import { Navbar, Nav, NavDropdown } from "react-bootstrap";


export default class Navigation extends Component {
    render() {
        return (
            <Navbar fixed="bottom" bg="primary" variant="dark" expand="lg">
                <Navbar.Brand href="#home">Agora</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="mr-auto">
                        <Nav.Link href="#home">Home</Nav.Link>
                        <Nav.Link href="#groups">Groups</Nav.Link>
                        <Nav.Link href="#events">Events</Nav.Link>
                        <NavDropdown drop="up" title="Profile" id="basic-nav-dropdown">
                            <NavDropdown.Item href="#action/3.1">My Account</NavDropdown.Item>
                            <NavDropdown.Item href="#action/3.2">Settings</NavDropdown.Item>
                            <NavDropdown.Divider />
                            <NavDropdown.Item href="#action/3.3">Sign Out</NavDropdown.Item>
                        </NavDropdown>
                    </Nav>
                </Navbar.Collapse>
            </Navbar>
        )
    };
}