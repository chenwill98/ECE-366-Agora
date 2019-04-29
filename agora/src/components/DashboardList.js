import React from "react";
import "../styles/DashboardList.css";

function Contact(props) {
    return (
        <div className="contact">
            <a href={"/group/" + props.id}><span>{props.name}</span></a>
        </div>
    );
}

function DashboardList(props) {

    if (props == null || props.contacts == null ) {
        return;
    }
    else {

        return (
            <div>
                {props.contacts.map(c => <Contact key={c.id} name={c.name}/>)}
            </div>
        );
    }
}

export default DashboardList;