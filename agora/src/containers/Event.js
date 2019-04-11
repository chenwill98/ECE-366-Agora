import React, { Component } from "react";
import axios from "axios";
import { Form, Button, Card } from "react-bootstrap";
import CenterView from '../components/CenterView.js';
import Navigation from '../components/Navigation.js';

class Event extends Component {
    constructor(props) {
        super(props);

        this.state = {
            event_id;

            data: [],
            email: "",
            password: "",
            id: 0,
            intervalSet: false,
            error: false
        };
    }


    //fetches all data when the component mounts
    componentDidMount () {
        const { handle } = this.props.match.params

        fetch(`/event/${event_id}`)
            .then((id) => {
                this.setState(() => ({ id }))
            })

        if (!this.state.intervalSet) {
            let interval = setInterval(this.getData, 1000);
            this.setState({intervalSet: interval})
        }
    }





    //kills the process
    componentWillUnmount() {
        if (this.state.intervalIsSet) {
            clearInterval(this.state.intervalIsSet);
            this.setState({ intervalIsSet: null });
        }
    }


}