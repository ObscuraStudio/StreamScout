import { loginUrl, logout } from '../api/auth'
import { useCurrentUser } from '../hooks/useCurrentUser'

function AuthButton() {
  const { user, loading, setUser } = useCurrentUser()

  if (loading) {
    return null
  }

  if (!user) {
    return (
      <a href={loginUrl()} className="auth-button">
        Login with Steam
      </a>
    )
  }

  const handleLogout = () => {
    logout()
      .then(() => setUser(null))
      .catch((error: unknown) => console.error(error))
  }

  return (
    <div className="auth-user">
      {user.avatarUrl && <img src={user.avatarUrl} alt="" className="auth-avatar" />}
      <span>{user.displayName}</span>
      <button onClick={handleLogout}>Logout</button>
    </div>
  )
}

export default AuthButton
