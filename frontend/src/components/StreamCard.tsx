import type { Stream } from '../api/streams'

function StreamCard({ stream }: { stream: Stream }) {
  return (
    <div className="stream-card">
      <img src={stream.thumbnailUrl} alt={stream.title} className="stream-card-image" />
      <div className="stream-card-name">{stream.streamerName}</div>
      <div className="stream-card-viewers">{stream.viewerCount} viewers</div>
      <a
        href={`https://twitch.tv/${stream.streamerLogin}`}
        target="_blank"
        rel="noopener noreferrer"
        className="stream-card-watch-link"
      >
        Watch on Twitch
      </a>
    </div>
  )
}

export default StreamCard
