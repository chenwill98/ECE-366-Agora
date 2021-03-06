import React, { Component } from 'react';
import {Col} from "react-bootstrap";
import Jumbotron from "react-bootstrap/Jumbotron";

export default class SingleObjectView extends Component {
    render() {
        return (
            <Jumbotron className="justify-content-md-center">
                <Col>{this.props.children}</Col>
            </Jumbotron>
        )
    };
}
