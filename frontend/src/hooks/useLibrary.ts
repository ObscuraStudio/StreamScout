import { useEffect, useState } from 'react'
import { getLibrary, type Game } from '../api/library'

type LibraryStatus = 'loading' | 'loaded' | 'error'

export function useLibrary(enabled: boolean) {
  const [games, setGames] = useState<Game[]>([])
  const [status, setStatus] = useState<LibraryStatus>('loading')

  useEffect(() => {
    if (!enabled) {
      return
    }

    let cancelled = false
    setStatus('loading')

    getLibrary()
      .then((loadedGames) => {
        if (!cancelled) {
          setGames(loadedGames)
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
  }, [enabled])

  return { games, status }
}
