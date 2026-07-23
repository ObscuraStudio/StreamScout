import { useEffect, useState } from 'react'
import { getStreams, type Stream } from '../api/streams'

type StreamsStatus = 'loading' | 'loaded' | 'error'

export function useGameStreams(gameName: string, language: string) {
  const [status, setStatus] = useState<StreamsStatus>('loading')
  const [streams, setStreams] = useState<Stream[]>([])

  useEffect(() => {
    let cancelled = false
    setStatus('loading')

    getStreams(gameName, language || undefined)
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
  }, [gameName, language])

  return { status, streams }
}
