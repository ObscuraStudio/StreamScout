import { useState } from 'react'

const PLACEHOLDER_IMAGE =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="184" height="86"><rect width="184" height="86" fill="#2a2f3a"/></svg>',
  )

type GameCardProps = {
  game: {
    appId: number
    name: string
    imageUrl: string
    playtimeHours?: number
  }
  isFavourite?: boolean
  onToggleFavourite?: () => void
  onClick?: () => void
  large?: boolean
}

function GameCard({ game, isFavourite, onToggleFavourite, onClick, large }: GameCardProps) {
  const [imageSrc, setImageSrc] = useState(game.imageUrl)

  return (
    <div
      className={large ? 'game-card game-card-large' : 'game-card'}
      onClick={onClick}
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      onKeyDown={(event) => {
        if (onClick && (event.key === 'Enter' || event.key === ' ')) {
          event.preventDefault()
          onClick()
        }
      }}
    >
      {onToggleFavourite && (
        <button
          type="button"
          className="game-card-favourite"
          aria-pressed={isFavourite}
          aria-label={isFavourite ? `Unfavourite ${game.name}` : `Favourite ${game.name}`}
          onClick={(event) => {
            event.stopPropagation()
            onToggleFavourite()
          }}
        >
          {isFavourite ? '★' : '☆'}
        </button>
      )}
      <img
        src={imageSrc}
        alt={game.name}
        className="game-card-image"
        onError={() => setImageSrc(PLACEHOLDER_IMAGE)}
      />
      <div className="game-card-title">{game.name}</div>
      {game.playtimeHours !== undefined && (
        <div className="game-card-playtime">{game.playtimeHours} h played</div>
      )}
    </div>
  )
}

export default GameCard
