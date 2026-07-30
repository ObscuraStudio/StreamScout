export type Stream = {
  streamerName: string
  streamerLogin: string
  title: string
  viewerCount: number
  thumbnailUrl: string
}

export async function getStreams(gameName: string, language?: string): Promise<Stream[]> {
  const params = new URLSearchParams({ name: gameName })
  if (language) {
    params.set('language', language)
  }
  const response = await fetch(`/api/streams?${params.toString()}`)
  if (!response.ok) {
    throw new Error(`Failed to load streams: ${response.status}`)
  }
  return (await response.json()) as Stream[]
}

export async function getTrendingStreams(limit?: number): Promise<Stream[]> {
  const params = new URLSearchParams()
  if (limit) {
    params.set('limit', String(limit))
  }
  const response = await fetch(`/api/streams/trending?${params.toString()}`)
  if (!response.ok) {
    throw new Error(`Failed to load trending streams: ${response.status}`)
  }
  return (await response.json()) as Stream[]
}
