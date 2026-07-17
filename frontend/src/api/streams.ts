export type Stream = {
  streamerName: string
  streamerLogin: string
  title: string
  viewerCount: number
  thumbnailUrl: string
}

export async function getStreams(gameName: string): Promise<Stream[]> {
  const response = await fetch(`/api/streams?name=${encodeURIComponent(gameName)}`)
  if (!response.ok) {
    throw new Error(`Failed to load streams: ${response.status}`)
  }
  return (await response.json()) as Stream[]
}
