export type SteamDiscoveryItem = {
  appId: number
  name: string
  releaseDate: string
  capsuleImageUrl: string
  rank: number
}

async function getListings(path: string): Promise<SteamDiscoveryItem[]> {
  const response = await fetch(path)
  if (!response.ok) {
    throw new Error(`Failed to load ${path}: ${response.status}`)
  }
  return (await response.json()) as SteamDiscoveryItem[]
}

export function getComingSoon(): Promise<SteamDiscoveryItem[]> {
  return getListings('/api/steam/coming-soon')
}

export function getMostWishlisted(): Promise<SteamDiscoveryItem[]> {
  return getListings('/api/steam/most-wishlisted')
}
