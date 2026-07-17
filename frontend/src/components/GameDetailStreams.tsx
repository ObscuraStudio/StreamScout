import StreamCard from './StreamCard'
import { useGameStreams } from '../hooks/useGameStreams'

function GameDetailStreams({ gameName }: { gameName: string }) {
  const { status, streams } = useGameStreams(gameName)

  if (status === 'loading') {
    return <p className="streams-message">Loading streams…</p>
  }

  if (status === 'error') {
    return <p className="streams-message">Couldn't load streams — try again.</p>
  }

  if (streams.length === 0) {
    return <p className="streams-message">No live streams found.</p>
  }

  return (
    <div className="streams-grid-detail">
      {streams.map((stream) => (
        <StreamCard key={stream.streamerLogin} stream={stream} />
      ))}
    </div>
  )
}

export default GameDetailStreams
