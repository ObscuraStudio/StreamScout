import { useEffect, useState } from 'react'
import { getMostWishlisted, type SteamDiscoveryItem } from '../api/steamDiscovery'

type MostWishlistedStatus = 'loading' | 'loaded' | 'error'

export function useMostWishlisted() {
  const [status, setStatus] = useState<MostWishlistedStatus>('loading')
  const [items, setItems] = useState<SteamDiscoveryItem[]>([])

  useEffect(() => {
    let cancelled = false
    setStatus('loading')

    getMostWishlisted()
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
