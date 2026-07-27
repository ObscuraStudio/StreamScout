import { useMostWishlisted } from '../hooks/useMostWishlisted'

function WishlistRankingPanel() {
  const { status, items } = useMostWishlisted()

  let content
  if (status === 'loading') {
    content = <p className="library-message">Loading…</p>
  } else if (status === 'error') {
    content = <p className="library-message">Couldn't load wishlist ranking.</p>
  } else if (items.length === 0) {
    content = <p className="library-message">No ranking data found.</p>
  } else {
    content = (
      <ul className="discovery-list">
        {items.map((item) => (
          <li key={item.appId} className="discovery-item">
            <a
              href={`https://store.steampowered.com/app/${item.appId}`}
              target="_blank"
              rel="noreferrer"
              className="discovery-item-link"
            >
              <span className="discovery-item-rank">#{item.rank}</span>
              <img src={item.capsuleImageUrl} alt="" className="discovery-item-image" />
              <span className="discovery-item-name">{item.name}</span>
            </a>
          </li>
        ))}
      </ul>
    )
  }

  return (
    <aside className="discovery-panel">
      <h2 className="section-heading">Most Wishlisted</h2>
      {content}
    </aside>
  )
}

export default WishlistRankingPanel
