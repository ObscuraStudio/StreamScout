export type Stream = {
  streamerName: string
  streamerLogin: string
  title: string
  viewerCount: number
  thumbnailUrl: string
}

export async function getStreams(appId: number): Promise<Stream[]> {
  const response = await fetch(`/api/favourites/${appId}/streams`)
  if (!response.ok) {
    throw new Error(`Failed to load streams: ${response.status}`)
  }
  return (await response.json()) as Stream[]
}
