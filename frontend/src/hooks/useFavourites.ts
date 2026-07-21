import { useEffect, useState } from 'react'
import {
  addFavourite,
  getFavourites,
  removeFavourite,
  type Favourite,
} from '../api/favourites'

type FavouritesStatus = 'loading' | 'loaded' | 'error'

function withoutGame(list: Favourite[], game: Favourite): Favourite[] {
  return list.filter((f) => f.appId !== game.appId)
}

function withGame(list: Favourite[], game: Favourite): Favourite[] {
  return [...list, game]
}

export function useFavourites(enabled: boolean) {
  const [favourites, setFavourites] = useState<Favourite[]>([])
  const [status, setStatus] = useState<FavouritesStatus>('loading')
  const [pendingAppIds, setPendingAppIds] = useState<Set<number>>(new Set())

  useEffect(() => {
    if (!enabled) {
      return
    }
    let cancelled = false
    setStatus('loading')

    getFavourites()
      .then((loaded) => {
        if (!cancelled) {
          setFavourites(loaded)
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

  const favouriteIds = new Set(favourites.map((f) => f.appId))

  const toggle = (game: Favourite) => {
    if (pendingAppIds.has(game.appId)) {
      return
    }

    const wasFavourite = favouriteIds.has(game.appId)

    setPendingAppIds((current) => new Set(current).add(game.appId))

    // Optimistic update.
    setFavourites((current) => (wasFavourite ? withoutGame(current, game) : withGame(current, game)))

    const request = wasFavourite ? removeFavourite(game.appId) : addFavourite(game)
    request
      .catch((error: unknown) => {
        console.error(error)
        // Revert on failure.
        setFavourites((current) => (wasFavourite ? withGame(current, game) : withoutGame(current, game)))
      })
      .finally(() => {
        setPendingAppIds((current) => {
          const next = new Set(current)
          next.delete(game.appId)
          return next
        })
      })
  }

  return { favourites, favouriteIds, status, toggle }
}
