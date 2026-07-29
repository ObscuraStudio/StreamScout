import { useTrendingStreams } from '../hooks/useTrendingStreams'

function TrendingStreamsPanel() {
  const { status, streams } = useTrendingStreams()

  let content
  if (status === 'loading') {
    content = <p className="library-message">Loading…</p>
  } else if (status === 'error') {
    content = <p className="library-message">Couldn't load trending streams.</p>
  } else if (streams.length === 0) {
    content = <p className="library-message">No trending streams found.</p>
  } else {
    content = (
      <ul className="discovery-list">
        {streams.map((stream) => (
          <li key={stream.streamerLogin} className="discovery-item">
            <a
              href={`https://twitch.tv/${stream.streamerLogin}`}
              target="_blank"
              rel="noreferrer"
              className="discovery-item-link"
            >
              <img src={stream.thumbnailUrl} alt="" className="discovery-item-image" />
              <span className="discovery-item-name">{stream.streamerName}</span>
              <span className="discovery-item-meta">{stream.viewerCount.toLocaleString()}</span>
            </a>
          </li>
        ))}
      </ul>
    )
  }

  return (
    <aside className="discovery-panel">
      <h2 className="section-heading">Trending on Twitch</h2>
      {content}
    </aside>
  )
}

export default TrendingStreamsPanel
