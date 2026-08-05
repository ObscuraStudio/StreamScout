import {loginUrl} from '../api/auth'

function LoginButton() {
    return (
        <>
            <a href={loginUrl()} className="login-button">
                Login with your Steam Account
            </a>
            <p className="login-synopsis">
                StreamScout connects your Steam library with Twitch — see who's live for the
                games you own, discover what's coming soon and trending, and jump straight into
                a stream.
            </p>
        </>
    )
}

export default LoginButton
