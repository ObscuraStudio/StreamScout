import { useEffect, useState } from 'react'

// Must match the `@media (max-width: 768px)` breakpoint in App.css.
const MOBILE_QUERY = '(max-width: 768px)'

export function useIsMobile() {
  const [isMobile, setIsMobile] = useState(() => window.matchMedia(MOBILE_QUERY).matches)

  useEffect(() => {
    const mediaQuery = window.matchMedia(MOBILE_QUERY)
    const handleChange = () => setIsMobile(mediaQuery.matches)

    mediaQuery.addEventListener('change', handleChange)
    return () => mediaQuery.removeEventListener('change', handleChange)
  }, [])

  return isMobile
}
