import React from 'react';
import ReactDOM from 'react-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Route, Switch, BrowserRouter as Router } from 'react-router-dom'
import SignUp from './containers/SignUp.js';
import Login from './containers/Login.js';
import Welcome from './components/Welcome.js';
import Dashboard from './containers/Dashboard.js';
import Groups from './containers/Groups.js';
import Events from './containers/Events.js';
import EventPage from './containers/EventPage.js';
import GroupPage from './containers/GroupPage.js';
import GroupCreate from './containers/GroupCreate.js';
import EventCreate from './containers/EventCreate.js';
import PageNotFound from './containers/PageNotFound';

const routing = (
    <Router>
        <div>
            <Switch>
                <Route exact path="/" component={Welcome} />
                <Route path="/home" exact component={Dashboard} />
                <Route path="/events" exact component={Events} />
                <Route path="/groups" exact component={Groups} />
                <Route path="/signup" exact component={SignUp} />
                <Route path="/login" exact component={Login} />
                <Route path="/group/:group_id" exact component={GroupPage} />
                <Route path="/event/:event_id" exact component={EventPage} />
                <Route path="/groupcreate" exact component={GroupCreate} />
                <Route path="/eventcreate" exact component={EventCreate} />
                <Route component={PageNotFound} />
            </Switch>
        </div>
    </Router>
);
ReactDOM.render(routing, document.getElementById('root'));

