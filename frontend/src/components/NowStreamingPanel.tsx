import { Link } from 'react-router-dom'
import { useLibraryLiveStreams } from '../hooks/useLibraryLiveStreams'
import type { Game } from '../api/library'

function NowStreamingPanel({ games }: Readonly<{ games: Game[] }>) {
  const { status, entries } = useLibraryLiveStreams(games)

  let content
  if (status === 'loading') {
    content = <p className="library-message">Loading…</p>
  } else if (entries.length === 0) {
    content = (
      <p className="library-message">None of your top games are being streamed right now.</p>
    )
  } else {
    content = (
      <ul className="discovery-list">
        {entries.map((entry) => (
          <li key={entry.appId} className="discovery-item">
            <Link to={`/games/${entry.appId}`} className="discovery-item-link">
              <img src={entry.stream.thumbnailUrl} alt="" className="discovery-item-image" />
              <span className="discovery-item-name">{entry.gameName}</span>
              <span className="discovery-item-meta">{entry.stream.viewerCount.toLocaleString()}</span>
            </Link>
          </li>
        ))}
      </ul>
    )
  }

  return (
    <aside className="discovery-panel">
      <h2 className="section-heading">Now Streaming</h2>
      {content}
    </aside>
  )
}

export default NowStreamingPanel
