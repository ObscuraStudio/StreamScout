import { useComingSoon } from '../hooks/useComingSoon'

function ComingSoonPanel() {
  const { status, items } = useComingSoon()

  let content
  if (status === 'loading') {
    content = <p className="library-message">Loading…</p>
  } else if (status === 'error') {
    content = <p className="library-message">Couldn't load coming soon titles.</p>
  } else if (items.length === 0) {
    content = <p className="library-message">No upcoming titles found.</p>
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
              <img src={item.capsuleImageUrl} alt="" className="discovery-item-image" />
              <span className="discovery-item-name">{item.name}</span>
              <span className="discovery-item-meta">{item.releaseDate}</span>
            </a>
          </li>
        ))}
      </ul>
    )
  }

  return (
    <aside className="discovery-panel">
      <h2 className="section-heading">Coming Soon</h2>
      {content}
    </aside>
  )
}

export default ComingSoonPanel
