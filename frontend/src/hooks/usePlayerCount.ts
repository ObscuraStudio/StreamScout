import { useEffect, useState } from 'react'
import { getPlayerCount } from '../api/playerCount'

type PlayerCountStatus = 'loading' | 'loaded' | 'error'

export function usePlayerCount(appId: number) {
  const [playerCount, setPlayerCount] = useState<number | null>(null)
  const [status, setStatus] = useState<PlayerCountStatus>('loading')

  useEffect(() => {
    let cancelled = false
    setStatus('loading')

    getPlayerCount(appId)
      .then((loaded) => {
        if (!cancelled) {
          setPlayerCount(loaded)
          setStatus('loaded')
        }
      })
      .catch((error: unknown) => {
        if (!cancelled) {
          console.error(error)
          setStatus('error')
        }
      })

    return () => {
      cancelled = true
    }
  }, [appId])

  return { playerCount, status }
}
