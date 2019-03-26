import React, { Component } from 'react';
import { Row, Col, Container} from "react-bootstrap";

export default class CenterView extends Component {
    render() {
        return (
            <Container>
                <Row className="justify-content-md-center">
                    <Col md="auto">{this.props.children}</Col>
                </Row>
            </Container>
        )
    };
}