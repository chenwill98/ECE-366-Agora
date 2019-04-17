import React from "react";
import "../styles/DashboardList.css";

import PropTypes from "prop-types";

function Contact(props) {
    return (
        <div className="contact">
            <span>{props.name}</span>
        </div>
    );
}

Contact.propTypes = {
    name: PropTypes.string.isRequired
};

function DashboardList(props) {
    return (
        <div>
            {props.contacts.map(c => <Contact key={c.id} name={c.name} />)}
        </div>
    );
}

export default DashboardList;