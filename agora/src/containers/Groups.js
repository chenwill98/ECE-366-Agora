import React, { Component } from 'react';
import { Card } from "react-bootstrap";
import axios from "axios";
import Navigation from "../components/Navigation.js";
import SearchBar from "../components/SearchBar";
import CenterView from '../components/CenterView.js';
import {Backend_Route} from "../BackendRoute.js";
import Cookies from "universal-cookie";

const cookies = new Cookies();

let init = {
    method: "Get",
    credentials: "include"
};


export default class Groups extends Component {
    constructor(props) {
        super(props);

        this.state = {
            // backend related states
            ip: Backend_Route.ip,
            port: Backend_Route.port,

            // user related states
            user_id: localStorage.getItem('userID'),
            user_groups: [],
            total_groups: [],

            // error related states
            intervalSet: false,
            error: false,
            error_msg: ""
        };
    }

    //fetches all data when the component mounts
    componentDidMount() {
        this.getData();
        // if (!this.state.intervalSet) {
        //     let interval = setInterval(this.getData, 1000);
        //     this.setState({intervalSet: interval});
        // }
    }

    // kills the process
    componentWillUnmount() {
        // if (this.state.intervalSet) {
        //     clearInterval(this.state.intervalSet);
        //     // this.setState({ intervalSet: null });
        // }
    }

    getData = () => {
        if (this.state.user_id && cookies.get("USER_TOKEN")) {
            //fetches all of the user's groups
            fetch( `${this.state.ip}:${this.state.port}/user/${this.state.user_id}/groups`, init)
            .catch(error => {
                this.setState({
                    error: true,
                    error_msg:  "Error requesting list of user groups: " + error.message
                });
                console.log("Error requesting user groups: " + error.message);
            })
            .then(res => {
                res.json().then(data => ({
                        data: data,
                        status: res.status
                    })
                )
                    .then(res => {
                        if (res.data !== '') {
                            console.log("Successfully got user groups.");
                            this.setState( {
                                user_groups: res.data
                            });
                        }
                    })
            });
        }

        //fetches all of the groups available for browsing
        axios.get( `${this.state.ip}:${this.state.port}/group/get-groups`)
        .then(res => {
            this.setState( {
                total_groups: res.data
            });
            console.log("Successfully got all groups.");
        })
        .catch(error => {
            this.setState({
                error: true,
                error_msg:  "Error requesting list of all groups: " + error.message
            });
            console.log("Error requesting all groups: " + error.message);
        });
    };

    render() {
        if (this.state.error) {
            return (
                <div className='p-5'>
                    <Navigation/>
                    <CenterView>
                        <Card border="primary" style={{width: '40rem'}}>
                            <Card.Body>
                                <Card.Title>Error</Card.Title>
                                <Card.Text>
                                    Oops, {this.state.error_msg} :/
                                </Card.Text>
                            </Card.Body>
                        </Card>
                    </CenterView>
                </div>
            );
        } else {
            return (
                <div className='p-5'>
                    <Navigation/>
                    {/*<SearchBar/>*/}
                    <aside className="main-sidebar px-0 col-12 col-md-3 col-lg-2">
                        {/*<div className="main-navbar">*/}
                            {/*<nav*/}
                                {/*className="align-items-stretch bg-white flex-md-nowrap border-bottom p-0 navbar navbar-light">*/}
                                {/*<a href="#" className="w-100 mr-0 navbar-brand" style="line-height: 25px;">*/}
                                    {/*<div className="d-table m-auto"><img id="main-logo"*/}
                                                                         {/*className="d-inline-block align-top mr-1"*/}
                                                                         {/*src="./static/media/shards-dashboards-logo.60a85991.svg"*/}
                                                                         {/*alt="Shards Dashboard"*/}
                                                                         {/*style="max-width: 25px;"/><span*/}
                                        {/*className="d-none d-md-inline ml-1">Shards Dashboard</span>*/}
                                    {/*</div>*/}
                                {/*</a><a className="toggle-sidebar d-sm-inline d-md-none d-lg-none"><i*/}
                                {/*className="material-icons"></i></a></nav>*/}
                        {/*</div>*/}
                        {/*<form className="main-sidebar__search w-100 border-right d-sm-flex d-md-none d-lg-none"*/}
                              {/*style="display: flex; min-height: 45px;">*/}
                            {/*<div className="ml-3 input-group input-group-seamless">*/}
                                {/*<div className="input-group-prepend"><span className="input-group-text"><i*/}
                                    {/*className="material-icons">search</i></span><input*/}
                                    {/*placeholder="Search for something..." aria-label="Search"*/}
                                    {/*className="navbar-search form-control"></div>*/}
                            {/*</div>*/}
                        {/*</form>*/}
                        {/*<div className="nav-wrapper">*/}
                            {/*<ul className="nav--no-borders flex-column nav">*/}
                                {/*<li className="nav-item"><a className="nav-link active" aria-current="page"*/}
                                                            {/*href="/demo/shards-dashboard-lite-react/blog-overview">*/}
                                    {/*<div className="d-inline-block item-icon-wrapper"><i*/}
                                        {/*className="material-icons">edit</i></div>*/}
                                    {/*<span>Blog Dashboard</span></a></li>*/}
                                {/*<li className="nav-item"><a className="nav-link"*/}
                                                            {/*href="/demo/shards-dashboard-lite-react/blog-posts">*/}
                                    {/*<div className="d-inline-block item-icon-wrapper"><i*/}
                                        {/*className="material-icons">vertical_split</i></div>*/}
                                    {/*<span>Blog Posts</span></a></li>*/}
                                {/*<li className="nav-item"><a className="nav-link"*/}
                                                            {/*href="/demo/shards-dashboard-lite-react/add-new-post">*/}
                                    {/*<div className="d-inline-block item-icon-wrapper"><i*/}
                                        {/*className="material-icons">note_add</i></div>*/}
                                    {/*<span>Add New Post</span></a></li>*/}
                                {/*<li className="nav-item"><a className="nav-link"*/}
                                                            {/*href="/demo/shards-dashboard-lite-react/components-overview">*/}
                                    {/*<div className="d-inline-block item-icon-wrapper"><i*/}
                                        {/*className="material-icons">view_module</i></div>*/}
                                    {/*<span>Forms &amp; Components</span></a></li>*/}
                                {/*<li className="nav-item"><a className="nav-link"*/}
                                                            {/*href="/demo/shards-dashboard-lite-react/tables">*/}
                                    {/*<div className="d-inline-block item-icon-wrapper"><i*/}
                                        {/*className="material-icons">table_chart</i></div>*/}
                                    {/*<span>Tables</span></a></li>*/}
                                {/*<li className="nav-item"><a className="nav-link"*/}
                                                            {/*href="/demo/shards-dashboard-lite-react/user-profile-lite">*/}
                                    {/*<div className="d-inline-block item-icon-wrapper"><i*/}
                                        {/*className="material-icons">person</i></div>*/}
                                    {/*<span>User Profile</span></a></li>*/}
                                {/*<li className="nav-item"><a className="nav-link"*/}
                                                            {/*href="/demo/shards-dashboard-lite-react/errors">*/}
                                    {/*<div className="d-inline-block item-icon-wrapper"><i*/}
                                        {/*className="material-icons">error</i></div>*/}
                                    {/*<span>Errors</span></a></li>*/}
                            {/*</ul>*/}
                        {/*</div>*/}
                    </aside>
                    <main>
                        <CenterView>
                            <Card>
                                <Card.Header as="h5">Your groups</Card.Header>
                            </Card>
                            {this.state.user_groups.map((groups, i) =>
                                <Card key={i} group={groups}>
                                    <Card.Body>
                                        {groups.name}
                                    </Card.Body>
                                </Card>)
                            }
                            <Card>
                                <Card.Header as="h5">All groups</Card.Header>
                            </Card>
                            {this.state.total_groups.map((groups, i) =>
                                <Card key={i} group={groups}>
                                    <Card.Body>
                                        {groups.name}
                                    </Card.Body>
                                </Card>
                            )}
                        </CenterView>
                    </main>
                </div>
            );
        }
    };
}