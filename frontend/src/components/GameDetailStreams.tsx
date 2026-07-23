import { useEffect, useMemo, useState } from 'react'
import StreamCard from './StreamCard'
import { useGameStreams } from '../hooks/useGameStreams'

type SortOrder = 'mostViewers' | 'leastViewers'
const PAGE_SIZE = 12

function GameDetailStreams({ gameName }: { gameName: string }) {
  const [language, setLanguage] = useState('')
  const [sortOrder, setSortOrder] = useState<SortOrder>('mostViewers')
  const [page, setPage] = useState(1)

  const { status, streams } = useGameStreams(gameName, language)

  // Reset to page 1 whenever sort or language changes - the underlying
  // result set shifts, so staying on e.g. page 4 could land on an empty page.
  useEffect(() => {
    setPage(1)
  }, [sortOrder, language])

  const sortedStreams = useMemo(
    () =>
      [...streams].sort((a, b) =>
        sortOrder === 'mostViewers' ? b.viewerCount - a.viewerCount : a.viewerCount - b.viewerCount,
      ),
    [streams, sortOrder],
  )

  const pageCount = Math.max(1, Math.ceil(sortedStreams.length / PAGE_SIZE))
  const safePage = Math.min(page, pageCount)
  const pagedStreams = sortedStreams.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE)

  let content
  if (status === 'loading') {
    content = <p className="streams-message">Loading streams…</p>
  } else if (status === 'error') {
    content = <p className="streams-message">Couldn't load streams — try again.</p>
  } else if (streams.length === 0) {
    content = <p className="streams-message">No live streams found.</p>
  } else {
    content = (
      <>
        <div className="streams-grid-detail">
          {pagedStreams.map((stream) => (
            <StreamCard key={stream.streamerLogin} stream={stream} />
          ))}
        </div>
        {pageCount > 1 && (
          <div className="streams-pagination">
            {Array.from({ length: pageCount }, (_, i) => i + 1).map((n) => (
              <button
                key={n}
                type="button"
                className={
                  n === safePage
                    ? 'streams-page-button streams-page-button-active'
                    : 'streams-page-button'
                }
                onClick={() => setPage(n)}
              >
                {n}
              </button>
            ))}
          </div>
        )}
      </>
    )
  }

  return (
    <>
      <div className="streams-controls">
        <select
          className="library-sort"
          value={language}
          onChange={(e) => setLanguage(e.target.value)}
        >
          <option value="">All Languages</option>
          <option value="de">German</option>
          <option value="en">English</option>
          <option value="ja">Japanese</option>
        </select>
        <select
          className="library-sort"
          value={sortOrder}
          onChange={(e) => setSortOrder(e.target.value as SortOrder)}
        >
          <option value="mostViewers">Most Viewers</option>
          <option value="leastViewers">Least Viewers</option>
        </select>
      </div>
      {content}
    </>
  )
}

export default GameDetailStreams
