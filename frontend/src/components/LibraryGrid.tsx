import { useNavigate } from 'react-router-dom'
import GameCard from './GameCard'
import { useLibrary } from '../hooks/useLibrary'
import { useFavourites } from '../hooks/useFavourites'

function LibraryGrid({ enabled }: { enabled: boolean }) {
  const navigate = useNavigate()
  const { games, status } = useLibrary(enabled)
  const { favourites, favouriteIds, status: favStatus, toggle } = useFavourites(enabled)

  let favouritesContent
  if (favStatus === 'error') {
    favouritesContent = <p className="library-message">Couldn't load favourites.</p>
  } else if (favourites.length === 0) {
    favouritesContent = (
      <p className="library-message">No favourites yet — tap the star on a game to add one.</p>
    )
  } else {
    // Favourite snapshots don't carry playtime (it's live data, not frozen at
    // favourite-time) — fill it in from the library entry when we have one.
    const libraryGamesByAppId = new Map(games.map((game) => [game.appId, game]))
    favouritesContent = (
      <div className="library-grid">
        {favourites.map((fav) => (
          <GameCard
            key={fav.appId}
            game={{ ...fav, playtimeHours: libraryGamesByAppId.get(fav.appId)?.playtimeHours }}
            isFavourite={true}
            onToggleFavourite={() => toggle(fav)}
            onClick={() => navigate(`/games/${fav.appId}`)}
          />
        ))}
      </div>
    )
  }

  let libraryContent
  if (status === 'loading') {
    libraryContent = <p className="library-message">Loading your library…</p>
  } else if (status === 'error') {
    libraryContent = <p className="library-message">Couldn't load your library — try again.</p>
  } else if (games.length === 0) {
    libraryContent = (
      <p className="library-message">
        No games found — make sure your Steam profile and game details are set to Public.
      </p>
    )
  } else {
    libraryContent = (
      <div className="library-grid">
        {games.map((game) => (
          <GameCard
            key={game.appId}
            game={game}
            isFavourite={favouriteIds.has(game.appId)}
            onToggleFavourite={() =>
              toggle({ appId: game.appId, name: game.name, imageUrl: game.imageUrl })
            }
            onClick={() => navigate(`/games/${game.appId}`)}
          />
        ))}
      </div>
    )
  }

  return (
    <>
      <section className="favourites-section">
        <h2 className="section-heading">Favourites</h2>
        {favouritesContent}
      </section>
      <section className="library-section">
        <h2 className="section-heading">Library</h2>
        {libraryContent}
      </section>
    </>
  )
}

export default LibraryGrid
