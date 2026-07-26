import { useEffect, useState } from 'react'
import { getComingSoon, type SteamDiscoveryItem } from '../api/steamDiscovery'

type ComingSoonStatus = 'loading' | 'loaded' | 'error'

export function useComingSoon() {
  const [status, setStatus] = useState<ComingSoonStatus>('loading')
  const [items, setItems] = useState<SteamDiscoveryItem[]>([])

  useEffect(() => {
    let cancelled = false
    setStatus('loading')

    getComingSoon()
      .then((loaded) => {
        if (!cancelled) {
          setItems(loaded)
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

  return { status, items }
}
