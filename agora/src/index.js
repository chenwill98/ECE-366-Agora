import React from 'react';
import ReactDOM from 'react-dom';
// import $ from 'jquery';
// import Popper from 'popper.js';
import 'bootstrap/dist/css/bootstrap.min.css';
//import './styles/index.css';
import { Route, Link, BrowserRouter as Router } from 'react-router-dom'
import App from './App';
import SignUp from './containers/SignUp.js';
import Login from './containers/Login.js';
import Welcome from './components/Welcome.js';
import Home from './containers/Home.js';
import Groups from './containers/Groups.js';
import Events from './containers/Events.js';

const routing = (
    <Router>
        <div>
            <Route path="/helloworld" component={App} />
            <Route path="/welcome" component={Welcome} />
            <Route path="/home" component={Home} />
            <Route path="/events" component={Events} />
            <Route path="/groups" component={Groups} />
            <Route path="/signup" component={SignUp} />
            <Route path="/login" component={Login} />
        </div>
    </Router>
)
ReactDOM.render(routing, document.getElementById('root'));

