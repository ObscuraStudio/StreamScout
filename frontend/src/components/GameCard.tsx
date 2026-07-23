import { useState } from 'react'

const PLACEHOLDER_IMAGE =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="184" height="86"><rect width="184" height="86" fill="#2a2f3a"/></svg>',
  )

// Steam doesn't publish a header.jpg for every app (especially newer/smaller
// titles) - fall back through its other known per-app image variants before
// giving up and showing the gray placeholder. iconImageUrl (built from
// GetOwnedGames' img_icon_url hash) is the last real fallback - almost every
// owned game has one, even when no marketing art exists. It's a tiny native
// icon though, so it's flagged separately (iconIndex) to render at native
// size instead of being stretched to fill the card like a real thumbnail.
function buildImageCandidates(appId: number, primaryUrl: string, iconImageUrl?: string | null) {
  const cdnBase = `https://cdn.cloudflare.steamstatic.com/steam/apps/${appId}`
  const urls = [primaryUrl, `${cdnBase}/capsule_616x353.jpg`, `${cdnBase}/library_600x900.jpg`]
  const iconIndex = iconImageUrl ? urls.length : -1
  if (iconImageUrl) {
    urls.push(iconImageUrl)
  }
  urls.push(PLACEHOLDER_IMAGE)
  return { urls, iconIndex }
}

type GameCardProps = {
  game: {
    appId: number
    name: string
    imageUrl: string
    iconImageUrl?: string | null
  }
  isFavourite?: boolean
  onToggleFavourite?: () => void
  onClick?: () => void
  large?: boolean
}

function GameCard({ game, isFavourite, onToggleFavourite, onClick, large }: GameCardProps) {
  const [candidateIndex, setCandidateIndex] = useState(0)
  const { urls: imageCandidates, iconIndex } = buildImageCandidates(
    game.appId,
    game.imageUrl,
    game.iconImageUrl,
  )
  const imageSrc = imageCandidates[candidateIndex]
  const isIconFallback = candidateIndex === iconIndex

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
        className={isIconFallback ? 'game-card-image game-card-image-icon-fallback' : 'game-card-image'}
        onError={() => setCandidateIndex((i) => Math.min(i + 1, imageCandidates.length - 1))}
      />
      <div className="game-card-title">{game.name}</div>
    </div>
  )
}

export default GameCard
