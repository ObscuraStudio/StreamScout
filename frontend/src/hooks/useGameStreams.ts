import { useEffect, useState } from 'react'
import { getStreams, type Stream } from '../api/streams'

type StreamsStatus = 'loading' | 'loaded' | 'error'

export function useGameStreams(gameName: string) {
  const [status, setStatus] = useState<StreamsStatus>('loading')
  const [streams, setStreams] = useState<Stream[]>([])

  useEffect(() => {
    let cancelled = false
    setStatus('loading')

    getStreams(gameName)
      .then((loaded) => {
        if (!cancelled) {
          setStreams(loaded)
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
  }, [gameName])

  return { status, streams }
}
