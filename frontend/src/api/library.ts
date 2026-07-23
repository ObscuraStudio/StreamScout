export type Game = {
  appId: number
  name: string
  playtimeHours: number
  imageUrl: string
  lastPlayedEpochSeconds: number
  iconImageUrl: string | null
}

export async function getLibrary(): Promise<Game[]> {
  const response = await fetch('/api/library')
  if (!response.ok) {
    throw new Error(`Failed to load library: ${response.status}`)
  }
  return (await response.json()) as Game[]
}
