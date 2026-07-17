import type { Stream } from '../api/streams'

function StreamCard({ stream }: { stream: Stream }) {
  return (
    <div className="stream-card">
      <img src={stream.thumbnailUrl} alt={stream.title} className="stream-card-image" />
      <div className="stream-card-name">{stream.streamerName}</div>
      <div className="stream-card-viewers">{stream.viewerCount} viewers</div>
    </div>
  )
}

export default StreamCard
