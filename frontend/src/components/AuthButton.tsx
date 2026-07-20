import { logout, type User } from '../api/auth'

function AuthButton({
  user,
  setUser,
}: {
  user: User
  setUser: (user: User | null) => void
}) {
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
