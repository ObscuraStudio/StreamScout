import { usePlayerCount } from '../hooks/usePlayerCount'

function PlayerCountBadge({ appId }: Readonly<{ appId: number }>) {
  const { playerCount, status } = usePlayerCount(appId)

  if (status === 'loading') {
    return null
  }

  if (status === 'error' || !playerCount) {
    return null
  }

  return <p className="library-message">{playerCount.toLocaleString()} playing now</p>
}

export default PlayerCountBadge
