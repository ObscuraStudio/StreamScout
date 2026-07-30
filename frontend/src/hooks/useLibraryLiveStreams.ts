import { useEffect, useState } from 'react'
import { getStreams, type Stream } from '../api/streams'
import type { Game } from '../api/library'

export type LibraryLiveEntry = {
  appId: number
  gameName: string
  stream: Stream
}

type LibraryLiveStatus = 'loading' | 'loaded'

const CANDIDATE_COUNT = 8
const DISPLAY_COUNT = 5

export function useLibraryLiveStreams(games: Game[]) {
  const [status, setStatus] = useState<LibraryLiveStatus>('loading')
  const [entries, setEntries] = useState<LibraryLiveEntry[]>([])

  useEffect(() => {
    if (games.length === 0) {
      setStatus('loaded')
      setEntries([])
      return
    }

    let cancelled = false
    setStatus('loading')

    const candidates = [...games]
      .sort((a, b) => b.playtimeHours - a.playtimeHours)
      .slice(0, CANDIDATE_COUNT)

    Promise.all(
      candidates.map((game) =>
        getStreams(game.name)
          .then((streams) => ({ game, streams }))
          .catch(() => ({ game, streams: [] as Stream[] })),
      ),
    ).then((results) => {
      if (cancelled) {
        return
      }

      const found = results
        .filter((result) => result.streams.length > 0)
        .map((result) => ({
          appId: result.game.appId,
          gameName: result.game.name,
          stream: result.streams.reduce(
            (top, s) => (s.viewerCount > top.viewerCount ? s : top),
            result.streams[0],
          ),
        }))
        .sort((a, b) => b.stream.viewerCount - a.stream.viewerCount)
        .slice(0, DISPLAY_COUNT)

      setEntries(found)
      setStatus('loaded')
    })

    return () => {
      cancelled = true
    }
  }, [games])

  return { status, entries }
}
