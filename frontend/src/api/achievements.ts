export type Achievements = {
  achieved: number
  total: number
  profilePrivate: boolean
}

export async function getAchievements(appId: number): Promise<Achievements> {
  if (!Number.isInteger(appId) || appId < 0) {
    throw new Error(`Invalid appId: ${appId}`)
  }
  const response = await fetch(`/api/achievements?appId=${appId}`)
  if (!response.ok) {
    throw new Error(`Failed to load achievements: ${response.status}`)
  }
  return (await response.json()) as Achievements
}
