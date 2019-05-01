import React, { Component } from 'react';
import { Navbar, Nav } from "react-bootstrap";
import Cookies from "universal-cookie";
import "../styles/Navigation.css";
import { withRouter } from 'react-router-dom';

const cookies = new Cookies();




class Navigation extends Component {
    logOut = () => {
        cookies.remove('USER_TOKEN', { path: '/' });
        localStorage.setItem('userID', null);
        this.props.history.push('/login');
    };

    render() {
        return (
            <Navbar fixed="bottom" bg="primary" variant="dark" expand="lg">
                <Navbar.Brand href="/">
                    <img
                        src={require("../images/Logo2.PNG")}
                        width="100"
                        height="20"
                        alt={"Logo2"}
                    />
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="mr-auto">
                        {cookies.get("USER_TOKEN") &&
                        <Nav.Link href="/home">
                            <i className="fas fa-home"></i>
                            &nbsp; Home
                        </Nav.Link>
                        }
                        <Nav.Link href="/groups">
                            <i className="fas fa-users"></i>
                            &nbsp; Groups
                        </Nav.Link>
                        <Nav.Link href="/events">
                            <i className="far fa-calendar-alt"></i>
                            &nbsp; Events
                        </Nav.Link>
                        {!cookies.get("USER_TOKEN") &&
                        <Nav.Link href="/signup">
                            <i className="fas fa-user-plus"></i>
                            &nbsp; Sign Up
                        </Nav.Link>
                        }
                        { !cookies.get("USER_TOKEN") &&
                            <Nav.Link href="/login">
                            <i className="fas fa-sign-in-alt"></i>
                            &nbsp; Login
                            </Nav.Link>
                        }
                    </Nav>
                    {cookies.get("USER_TOKEN") &&
                    <Nav className="ml-auto">
                        <Nav.Link href="/home">
                            <i className="fas fa-user-circle"></i>
                            &nbsp; My Account
                        </Nav.Link>
                        <Nav.Link onClick={() => this.logOut()}>
                            <i className="fas fa-sign-out-alt"></i>
                            &nbsp; Sign Out
                        </Nav.Link>
                    </Nav>
                    }
                </Navbar.Collapse>
            </Navbar>
        )
    };
}
export default withRouter(Navigation) // at the end of component