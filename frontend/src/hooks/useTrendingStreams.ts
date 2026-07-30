import { useEffect, useState } from 'react'
import { getTrendingStreams, type Stream } from '../api/streams'

type TrendingStreamsStatus = 'loading' | 'loaded' | 'error'

export function useTrendingStreams() {
  const [status, setStatus] = useState<TrendingStreamsStatus>('loading')
  const [streams, setStreams] = useState<Stream[]>([])

  useEffect(() => {
    let cancelled = false
    setStatus('loading')

    getTrendingStreams()
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
  }, [])

  return { status, streams }
}
