import './App.css'
import AuthButton from './components/AuthButton'
import LibraryGrid from './components/LibraryGrid'
import { useCurrentUser } from './hooks/useCurrentUser'

function App() {
  const { user, loading, setUser } = useCurrentUser()

  return (
    <>
      <header className="app-header">
        <h1>StreamScout</h1>
        {!loading && <AuthButton user={user} setUser={setUser} />}
      </header>
      <main className="app-main">
        {!loading && user && <LibraryGrid enabled={true} />}
      </main>
    </>
  )
}

export default App
