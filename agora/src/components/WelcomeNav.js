import React, { Component } from 'react';
import { Navbar, Nav } from "react-bootstrap";
// import Logo from '../images/AgoraLogo.svg';


export default class Navigation extends Component {
    render() {
        return (
            <Navbar fixed="bottom" bg="primary" variant="dark" expand="lg">
                {/*<Navbar.Brand href="/">*/}
                    {/*<img*/}
                        {/*src="../images/AgoraLogo.svg"*/}
                        {/*width="30"*/}
                        {/*height="30"*/}
                    {/*/>*/}
                {/*</Navbar.Brand>*/}
                <Navbar.Brand href="/">Agora</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="mr-auto">
                        <Nav.Link href="/signup">Sign Up</Nav.Link>
                        <Nav.Link href="/login">Login</Nav.Link>
                    </Nav>
                </Navbar.Collapse>
            </Navbar>
        )
    };
}