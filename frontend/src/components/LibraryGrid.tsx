import GameCard from './GameCard'
import { useLibrary } from '../hooks/useLibrary'

function LibraryGrid({ enabled }: { enabled: boolean }) {
  const { games, status } = useLibrary(enabled)

  if (status === 'loading') {
    return <p className="library-message">Loading your library…</p>
  }

  if (status === 'error') {
    return <p className="library-message">Couldn't load your library — try again.</p>
  }

  if (games.length === 0) {
    return (
      <p className="library-message">
        No games found — make sure your Steam profile and game details are set to Public.
      </p>
    )
  }

  return (
    <div className="library-grid">
      {games.map((game) => (
        <GameCard key={game.appId} game={game} />
      ))}
    </div>
  )
}

export default LibraryGrid
