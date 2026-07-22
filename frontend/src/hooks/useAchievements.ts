import { useEffect, useState } from 'react'
import { getAchievements, type Achievements } from '../api/achievements'

type AchievementsStatus = 'loading' | 'loaded' | 'error'

export function useAchievements(appId: number) {
  const [achievements, setAchievements] = useState<Achievements | null>(null)
  const [status, setStatus] = useState<AchievementsStatus>('loading')

  useEffect(() => {
    let cancelled = false
    setStatus('loading')

    getAchievements(appId)
      .then((loaded) => {
        if (!cancelled) {
          setAchievements(loaded)
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
  }, [appId])

  return { achievements, status }
}
