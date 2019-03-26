import React from 'react';
import ReactDOM from 'react-dom';
// import $ from 'jquery';
// import Popper from 'popper.js';
import 'bootstrap/dist/css/bootstrap.min.css';
import './styles/index.css';
import { Route, Link, BrowserRouter as Router } from 'react-router-dom'
import App from './App';
import SignUp from './containers/SignUp.js';
import Login from './containers/Login.js';

const routing = (
    <Router>
        <div>
            <Route path="/helloworld" component={App} />
            <Route path="/signup" component={SignUp} />
            <Route path="/login" component={Login} />
        </div>
    </Router>
)
ReactDOM.render(routing, document.getElementById('root'));

