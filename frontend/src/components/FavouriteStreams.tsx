import StreamCard from './StreamCard'
import { useGameStreams } from '../hooks/useGameStreams'

function FavouriteStreams({ appId }: { appId: number }) {
  const { status, streams, expanded, toggle } = useGameStreams(appId)

  let content = null
  if (expanded) {
    if (status === 'loading') {
      content = <p className="streams-message">Loading streams…</p>
    } else if (status === 'error') {
      content = <p className="streams-message">Couldn't load streams — try again.</p>
    } else if (status === 'loaded' && streams.length === 0) {
      content = <p className="streams-message">No live streams found.</p>
    } else if (status === 'loaded') {
      content = (
        <div className="streams-grid">
          {streams.map((stream) => (
            <StreamCard key={stream.streamerLogin} stream={stream} />
          ))}
        </div>
      )
    }
  }

  return (
    <div className="favourite-streams">
      <button type="button" className="streams-toggle" onClick={toggle}>
        {expanded ? 'Hide live streams' : 'Show live streams'}
      </button>
      {content}
    </div>
  )
}

export default FavouriteStreams
