import {loginUrl} from '../api/auth'

function LoginButton() {
    return (
        <a href={loginUrl()} className="login-button">
            Login with your Steam Account
        </a>
    )
}

export default LoginButton
