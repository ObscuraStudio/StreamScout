import { useState } from 'react'
import { getStreams, type Stream } from '../api/streams'

type StreamsStatus = 'idle' | 'loading' | 'loaded' | 'error'

export function useGameStreams(appId: number) {
  const [status, setStatus] = useState<StreamsStatus>('idle')
  const [streams, setStreams] = useState<Stream[]>([])
  const [expanded, setExpanded] = useState(false)

  const toggle = () => {
    if (expanded) {
      setExpanded(false)
      return
    }

    setExpanded(true)

    if (status === 'loaded') {
      return
    }

    setStatus('loading')
    getStreams(appId)
      .then((loaded) => {
        setStreams(loaded)
        setStatus('loaded')
      })
      .catch((error: unknown) => {
        console.error(error)
        setStatus('error')
      })
  }

  return { status, streams, expanded, toggle }
}
