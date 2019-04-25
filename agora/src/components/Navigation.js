import React, { Component } from 'react';
import { Navbar, Nav, NavDropdown } from "react-bootstrap";
import Cookies from "universal-cookie";
import { withRouter } from 'react-router-dom';

const cookies = new Cookies();




class Navigation extends Component {
    logOut = () => {
        cookies.remove('USER_TOKEN', { path: '/' });
        localStorage.setItem('userID', '');
        this.props.history.push('/login');
    }

    render() {
        return (
            <Navbar fixed="bottom" bg="primary" variant="dark" expand="lg">
                <Navbar.Brand href="/home">Agora</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="mr-auto">
                        <Nav.Link href="/home">Home</Nav.Link>
                        <Nav.Link href="/groups">Groups</Nav.Link>
                        <Nav.Link href="/events">Events</Nav.Link>
                        <NavDropdown drop="up" title="Profile" id="basic-nav-dropdown">
                            <NavDropdown.Item href="/account">My Account</NavDropdown.Item>
                            <NavDropdown.Item href="/settings">Settings</NavDropdown.Item>
                            <NavDropdown.Divider />
                            <NavDropdown.Item onClick={() => this.logOut()}>Sign Out</NavDropdown.Item>
                        </NavDropdown>
                    </Nav>
                </Navbar.Collapse>
            </Navbar>
        )
    };
}
export default withRouter(Navigation) // at the end of component