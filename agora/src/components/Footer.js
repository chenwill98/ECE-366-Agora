import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';



class Footer extends Component {
    render() {
        return (
            <div>
                <hr width="50%"/>
                <footer>
                    <p className="float-right"><a href="#">Back to top</a></p>
                    <p>© 2019 Agora, Inc. · <a href="#">Privacy</a> · <a href="#">Terms</a></p>
                </footer>
            </div>
        )
    };
}
export default withRouter(Footer) // at the end of component