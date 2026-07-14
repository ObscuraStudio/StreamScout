import { useState } from 'react'
import type { Game } from '../api/library'

const PLACEHOLDER_IMAGE =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="184" height="86"><rect width="184" height="86" fill="#2a2f3a"/></svg>',
  )

function GameCard({ game }: { game: Game }) {
  const [imageSrc, setImageSrc] = useState(game.imageUrl)

  return (
    <div className="game-card">
      <img
        src={imageSrc}
        alt={game.name}
        className="game-card-image"
        onError={() => setImageSrc(PLACEHOLDER_IMAGE)}
      />
      <div className="game-card-title">{game.name}</div>
      <div className="game-card-playtime">{game.playtimeHours} h played</div>
    </div>
  )
}

export default GameCard
