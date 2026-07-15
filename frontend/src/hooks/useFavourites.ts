import { useEffect, useState } from 'react'
import {
  addFavourite,
  getFavourites,
  removeFavourite,
  type Favourite,
} from '../api/favourites'

type FavouritesStatus = 'loading' | 'loaded' | 'error'

export function useFavourites(enabled: boolean) {
  const [favourites, setFavourites] = useState<Favourite[]>([])
  const [status, setStatus] = useState<FavouritesStatus>('loading')

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
    const wasFavourite = favouriteIds.has(game.appId)

    // Optimistic update.
    setFavourites((current) =>
      wasFavourite
        ? current.filter((f) => f.appId !== game.appId)
        : [...current, game],
    )

    const request = wasFavourite ? removeFavourite(game.appId) : addFavourite(game)
    request.catch((error: unknown) => {
      console.error(error)
      // Revert on failure.
      setFavourites((current) =>
        wasFavourite
          ? [...current, game]
          : current.filter((f) => f.appId !== game.appId),
      )
    })
  }

  return { favourites, favouriteIds, status, toggle }
}
