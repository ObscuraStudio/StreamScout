import { readCsrfToken } from './csrf'

export type Favourite = {
  appId: number
  name: string
  imageUrl: string
}

export async function getFavourites(): Promise<Favourite[]> {
  const response = await fetch('/api/favourites')
  if (!response.ok) {
    throw new Error(`Failed to load favourites: ${response.status}`)
  }
  return (await response.json()) as Favourite[]
}

export async function addFavourite(game: Favourite): Promise<void> {
  const csrfToken = readCsrfToken()
  const response = await fetch('/api/favourites', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : {}),
    },
    body: JSON.stringify(game),
  })
  if (!response.ok) {
    throw new Error(`Failed to add favourite: ${response.status}`)
  }
}

export async function removeFavourite(appId: number): Promise<void> {
  const csrfToken = readCsrfToken()
  const response = await fetch(`/api/favourites/${appId}`, {
    method: 'DELETE',
    headers: csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : undefined,
  })
  if (!response.ok) {
    throw new Error(`Failed to remove favourite: ${response.status}`)
  }
}
