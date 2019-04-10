import React, { Component } from 'react';
import { Row, Col, Container} from "react-bootstrap";
import Navigation from "../components/Navigation.js";

export default class Home extends Component {
    constructor(props) {
        super(props);

        this.state = {
            data: [],
            email: "",
            password: "",
            id: 0,
            intervalSet: false,
            error: false
        };
    }

    componentDidMount() {
        // this.getData();
        console.log(localStorage.getItem('cookie'));
        if (!this.state.intervalSet) {
            let interval = setInterval(this.getData, 1000);
            this.setState({intervalSet: interval});
        }
    }

    render() {
        return (
            <div>
                <Navigation/>

            </div>
        )
    };
}