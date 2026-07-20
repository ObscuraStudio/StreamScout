import { BrowserRouter, Route, Routes } from 'react-router-dom'
import './App.css'
import AuthButton from './components/AuthButton'
import LoginButton from './components/LoginButton'
import LibraryGrid from './components/LibraryGrid'
import GameDetailPage from './components/GameDetailPage'
import { useCurrentUser } from './hooks/useCurrentUser'

function App() {
  const { user, loading, setUser } = useCurrentUser()

  return (
    <BrowserRouter>
      <header className="app-header">
        <img src="/StreamScoutLogo.png" alt="StreamScout" className="app-logo" />
        {!loading && user && <AuthButton user={user} setUser={setUser} />}
      </header>
      <main className="app-main">
        {!loading && !user && <LoginButton />}
        {!loading && user && (
          <Routes>
            <Route path="/" element={<LibraryGrid enabled={true} />} />
            <Route path="/games/:appId" element={<GameDetailPage />} />
          </Routes>
        )}
      </main>
    </BrowserRouter>
  )
}

export default App
