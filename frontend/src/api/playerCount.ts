export async function getPlayerCount(appId: number): Promise<number> {
  const response = await fetch(`/api/player-count?appId=${appId}`)
  if (!response.ok) {
    throw new Error(`Failed to load player count: ${response.status}`)
  }
  const data = (await response.json()) as { playerCount: number }
  return data.playerCount
}
