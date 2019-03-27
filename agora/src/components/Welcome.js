import React, { Component } from 'react';
import { Row, Col, Container} from "react-bootstrap";
import Navigation from "./Navigation.js";

export default class Welcome extends Component {
    render() {
        return (
            <div>
                <Navigation/>
                Welcome placeholder
            </div>
        )
    };
}