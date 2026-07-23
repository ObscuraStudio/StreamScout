import { useAchievements } from '../hooks/useAchievements'

function AchievementSummary({ appId }: Readonly<{ appId: number }>) {
  const { achievements, status } = useAchievements(appId)

  if (status === 'loading') {
    return <p className="library-message">Loading stats…</p>
  }

  if (achievements?.profilePrivate) {
    return (
      <p className="library-message">Achievements couldn't load, because Profile isn't public.</p>
    )
  }

  if (status === 'error' || !achievements || achievements.total === 0) {
    return <p className="library-message">No Achievements for this game.</p>
  }

  return (
    <p className="library-message">
      {achievements.achieved}/{achievements.total} achievements
    </p>
  )
}

export default AchievementSummary
