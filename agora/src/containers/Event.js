import React, { Component } from "react";
import axios from "axios";
import SinglebObjectView from '../components/SingleObjectView.js';
import Navigation from '../components/Navigation.js';

class Event extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: "http://localhost",
            port: "8080",

            // events related states
            event_id: this.props.match.params.event_id,
            event_description: "description",
            event_name: "Name",
            event_gid: "456",
            event_location: "Here",
            event_date: "03/18/1994",

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }


    //fetches all data when the component mounts (called right after constructor)
    componentDidMount () {
        axios.get( `${this.state.ip}:${this.state.port}/event/${this.state.event_id}`)
            .then(res => {
                this.setState( {
                    event_description: res.data.description,
                    event_name: res.data.name,
                    event_gid: res.data.gid,
                    event_location: res.data.location,
                    event_date: res.data.date
                });
            })
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg: error.message
                });
                console.log("Error on request: " + error.message);
            });

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


    render() {
        if (this.state.error) {
            return (
                <div className='mt-5'>
                    <Navigation/>
                    <SinglebObjectView>
                        <p> {this.state.error_msg}</p>
                    </SinglebObjectView>
                </div>
                    );
        }
        else {
            return (
                <div className='mt-5'>
                    <Navigation/>

                    <SinglebObjectView>
                        <h1>
                            {this.state.event_name}
                        </h1>
                        <p>
                            {this.state.event_id}
                        </p>
                        <p>
                            {this.state.event_description}
                        </p>
                        <p>
                            {this.state.event_location}
                        </p>
                        <p>
                            {this.state.event_date}
                        </p>
                    </SinglebObjectView>
                </div>
            );
        }
    }
}

export default Event;