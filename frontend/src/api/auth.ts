export type User = {
  steamId: string
  displayName: string
  avatarUrl: string | null
}

function readCookie(name: string): string | null {
  const match = document.cookie
    .split('; ')
    .find((row) => row.startsWith(`${name}=`))
  return match ? decodeURIComponent(match.split('=')[1]) : null
}

export async function getCurrentUser(): Promise<User | null> {
  const response = await fetch('/api/me')
  if (response.status === 401) {
    return null
  }
  if (!response.ok) {
    throw new Error(`Failed to load current user: ${response.status}`)
  }
  return (await response.json()) as User
}

export async function logout(): Promise<void> {
  const csrfToken = readCookie('XSRF-TOKEN')
  const response = await fetch('/logout', {
    method: 'POST',
    headers: csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : undefined,
  })
  if (!response.ok) {
    throw new Error(`Failed to log out: ${response.status}`)
  }
}

export function loginUrl(): string {
  return '/api/auth/steam/login'
}
