import React from 'react';
import ReactDOM from 'react-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Route, BrowserRouter as Router } from 'react-router-dom'
import SignUp from './containers/SignUp.js';
import Login from './containers/Login.js';
import Welcome from './components/Welcome.js';
import Home from './containers/Home.js';
import Groups from './containers/Groups.js';
import Events from './containers/Events.js';
import EventPage from './containers/EventPage.js';
import GroupPage from './containers/GroupPage.js';
import GroupCreate from './containers/GroupCreate.js';
import EventCreate from './containers/EventCreate.js';

const routing = (
    <Router>
        <div>
            <Route exact path="/" component={Welcome} />
            <Route path="/home" component={Home} />
            <Route path="/events" component={Events} />
            <Route path="/groups" component={Groups} />
            <Route path="/signup" component={SignUp} />
            <Route path="/login" component={Login} />
            <Route path="/group/:group_id" component={GroupPage} />
            <Route path="/event/:event_id" component={EventPage} />
            <Route path="/groupCreate" component ={GroupCreate} />
            <Route path="/eventCreate" component ={EventCreate} />

        </div>
    </Router>
)
ReactDOM.render(routing, document.getElementById('root'));

