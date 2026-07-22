import { Link, useParams } from 'react-router-dom'
import GameCard from './GameCard'
import GameDetailStreams from './GameDetailStreams'
import AchievementSummary from './AchievementSummary'
import { useFavourites } from '../hooks/useFavourites'
import { useLibrary } from '../hooks/useLibrary'

function GameDetailPage() {
  const { appId } = useParams<{ appId: string }>()
  const numericAppId = Number(appId)
  const { favourites, status: favStatus, toggle } = useFavourites(true)
  const { games, status: libStatus } = useLibrary(true)

  if (favStatus === 'loading' || libStatus === 'loading') {
    return <p className="library-message">Loading…</p>
  }

  const favourite = favourites.find((f) => f.appId === numericAppId)
  const libraryGame = games.find((g) => g.appId === numericAppId)
  // Prefer the library entry when available — it carries playtimeHours, which
  // the favourite snapshot deliberately doesn't (see Favourite type).
  const game = libraryGame ?? favourite

  if (!game) {
    return (
      <div className="game-detail">
        <Link to="/" className="back-to-library">
          Back to Library
        </Link>
        <p className="library-message">Game not found.</p>
      </div>
    )
  }

  return (
    <div className="game-detail">
      <Link to="/" className="back-to-library">
        Back to Library
      </Link>

      <div className="game-detail-header">
        <GameCard
          game={game}
          isFavourite={favourite !== undefined}
          onToggleFavourite={() =>
            toggle({ appId: game.appId, name: game.name, imageUrl: game.imageUrl })
          }
          large={true}
        />
      </div>

      <section className="stats-placeholder">
        <h2 className="section-heading">Stats</h2>
        {libraryGame !== undefined && (
          <p className="library-message">{libraryGame.playtimeHours} hours played</p>
        )}
        <AchievementSummary appId={game.appId} />
      </section>

      <section>
        <h2 className="section-heading">Live Streams</h2>
        <GameDetailStreams gameName={game.name} />
      </section>
    </div>
  )
}

export default GameDetailPage
