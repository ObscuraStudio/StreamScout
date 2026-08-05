import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import GameCard from './GameCard'
import ComingSoonPanel from './ComingSoonPanel'
import WishlistRankingPanel from './WishlistRankingPanel'
import NowStreamingPanel from './NowStreamingPanel'
import TrendingStreamsPanel from './TrendingStreamsPanel'
import { useLibrary } from '../hooks/useLibrary'
import { useFavourites } from '../hooks/useFavourites'
import { useIsMobile } from '../hooks/useIsMobile'
import type { Game } from '../api/library'

type SortOrder = 'lastPlayed' | 'mostPlaytime' | 'az' | 'za'
type MobileTab = 'home' | 'library' | 'twitch'

function compareGames(a: Game, b: Game, sortOrder: SortOrder): number {
  switch (sortOrder) {
    case 'mostPlaytime':
      return b.playtimeHours - a.playtimeHours
    case 'az':
      return a.name.localeCompare(b.name)
    case 'za':
      return b.name.localeCompare(a.name)
    case 'lastPlayed': {
      if (a.lastPlayedEpochSeconds === 0 && b.lastPlayedEpochSeconds === 0) {
        return b.playtimeHours - a.playtimeHours
      }
      if (a.lastPlayedEpochSeconds === 0) {
        return 1
      }
      if (b.lastPlayedEpochSeconds === 0) {
        return -1
      }
      return b.lastPlayedEpochSeconds - a.lastPlayedEpochSeconds
    }
  }
}

function LibraryGrid({ enabled }: { enabled: boolean }) {
  const navigate = useNavigate()
  const isMobile = useIsMobile()
  const { games, status } = useLibrary(enabled)
  const { favourites, favouriteIds, status: favStatus, toggle } = useFavourites(enabled)
  const [query, setQuery] = useState('')
  const [sortOrder, setSortOrder] = useState<SortOrder>('lastPlayed')
  const [activeTab, setActiveTab] = useState<MobileTab>('home')

  let favouritesContent
  if (favStatus === 'loading') {
    favouritesContent = <p className="library-message">Loading your favourites…</p>
  } else if (favStatus === 'error') {
    favouritesContent = <p className="library-message">Couldn't load favourites.</p>
  } else if (favourites.length === 0) {
    favouritesContent = (
      <p className="library-message">No favourites yet — tap the star on a game to add one.</p>
    )
  } else {
    favouritesContent = (
      <div className="library-grid">
        {favourites.map((fav) => (
          <GameCard
            key={fav.appId}
            game={fav}
            isFavourite={true}
            onToggleFavourite={() => toggle(fav)}
            onClick={() => navigate(`/games/${fav.appId}`)}
          />
        ))}
      </div>
    )
  }

  const filteredGames = games
    .filter((game) => game.name.toLowerCase().includes(query.toLowerCase()))
    .sort((a, b) => compareGames(a, b, sortOrder))

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
  } else if (filteredGames.length === 0) {
    libraryContent = <p className="library-message">No games match "{query}".</p>
  } else {
    libraryContent = (
      <div className="library-grid">
        {filteredGames.map((game) => (
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

  const librarySortControls = (
    <>
      <input
        type="text"
        className="library-search"
        placeholder="Search your library..."
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />
      <select
        className="library-sort"
        value={sortOrder}
        onChange={(e) => setSortOrder(e.target.value as SortOrder)}
      >
        <option value="lastPlayed">Last Played</option>
        <option value="mostPlaytime">Most Playtime</option>
        <option value="az">A-Z</option>
        <option value="za">Z-A</option>
      </select>
    </>
  )

  if (isMobile) {
    const favouriteGames = games.filter((game) => favouriteIds.has(game.appId))

    return (
      <div className="mobile-tabs-layout">
        <div className="mobile-tab-bar">
          <button
            className={`mobile-tab-button${activeTab === 'home' ? ' mobile-tab-button-active' : ''}`}
            onClick={() => setActiveTab('home')}
          >
            Home
          </button>
          <button
            className={`mobile-tab-button${activeTab === 'library' ? ' mobile-tab-button-active' : ''}`}
            onClick={() => setActiveTab('library')}
          >
            Library
          </button>
          <button
            className={`mobile-tab-button${activeTab === 'twitch' ? ' mobile-tab-button-active' : ''}`}
            onClick={() => setActiveTab('twitch')}
          >
            Twitch
          </button>
        </div>

        {activeTab === 'home' && (
          <div className="mobile-tab-panel">
            <section className="favourites-section">
              <h2 className="section-heading">Favourites</h2>
              {favouritesContent}
            </section>
            <ComingSoonPanel />
            <WishlistRankingPanel />
          </div>
        )}

        {activeTab === 'library' && (
          <div className="mobile-tab-panel">
            {librarySortControls}
            <section className="library-section">
              <h2 className="section-heading">Library</h2>
              {libraryContent}
            </section>
          </div>
        )}

        {activeTab === 'twitch' && (
          <div className="mobile-tab-panel">
            <NowStreamingPanel games={favouriteGames} />
            <TrendingStreamsPanel />
          </div>
        )}
      </div>
    )
  }

  return (
    <div className="library-page-layout">
      <div className="discovery-sidebar discovery-sidebar-left">
        <ComingSoonPanel />
        <WishlistRankingPanel />
      </div>
      <div className="library-page-center">
        <section className="favourites-section">
          <h2 className="section-heading">Favourites</h2>
          {favouritesContent}
        </section>
        {librarySortControls}
        <section className="library-section">
          <h2 className="section-heading">Library</h2>
          {libraryContent}
        </section>
      </div>
      <div className="discovery-sidebar discovery-sidebar-right">
        <NowStreamingPanel games={games} />
        <TrendingStreamsPanel />
      </div>
    </div>
  )
}

export default LibraryGrid
