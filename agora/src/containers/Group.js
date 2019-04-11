import React, { Component } from "react";
import axios from "axios";
import Navigation from '../components/Navigation.js';
import SinglebObjectView from '../components/SingleObjectView.js';

class Group extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: "http://localhost",
            port: "8080",

            // group related states
            group_id: this.props.match.params.group_id,
            group_description: "Description here",
            group_name: "Name here",

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }


    //fetches all data when the component mounts
    componentDidMount () {
        axios.get( `${this.state.ip}:${this.state.port}/group/${this.state.group_id}`)
            .then(res => {
                this.setState( {
                    group_description: res.data.description,
                    group_name: res.data.name
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
                            {this.state.group_name}
                        </h1>
                        <p>
                            {this.state.group_id}
                        </p>
                        <p>
                            {this.state.group_description}
                        </p>
                    </SinglebObjectView>
                </div>
            );
        }
    }

}

export default Group;