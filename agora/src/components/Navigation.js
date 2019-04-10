import React, { Component } from 'react';
import axios from "axios";
import { Navbar, Nav, NavDropdown } from "react-bootstrap";


export default class Navigation extends Component {
    constructor(props) {
        super(props);

        this.state = {
            cookie: "",
            session: false,
            error: false
        };
    }

    //fetches the locally cached cookie and compares it to the DB's cookie
    // componentDidMount() {
    //     this.setState({cookie:localStorage.getItem('cookie')});
    //     axios.get("http://localhost:8080/login", {
    //         headers:{
    //             Cookie: this.state.cookie
    //         }
    //     }).then(response => {
    //         if (response.data == "Error") { //some error
    //             this.setState({session:false})
    //         } else {
    //             this.setState({session:true})
    //         }
    //     })
    // }

    deleteCookie = () => {
        localStorage.removeItem('cookie');
        this.setState({cookie: ""});
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
                            <NavDropdown.Item onClick={() =>
                                    this.deleteCookie()
                            }>Sign Out</NavDropdown.Item>
                        </NavDropdown>
                    </Nav>
                </Navbar.Collapse>
            </Navbar>
        )
    };
}