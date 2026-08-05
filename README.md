[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=obscurastudio_StreamScout&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=obscurastudio_StreamScout)

# StreamScout

StreamScout connects your Steam library with Twitch. Log in with Steam,
browse and favourite the games you own, see who's live streaming them
right now, and discover what's coming soon or trending — all in one
place.

## Features

- **Steam login** — sign in with your existing Steam account (OpenID),
  no separate registration.
- **Library browsing** — your full Steam library with search, and sort
  by last played, most playtime, or A-Z.
- **Favourites** — star any game to pin it to a dedicated Favourites
  section.
- **Live Twitch streams per game** — open any game to see who's
  streaming it right now, with sorting by viewer count, pagination, and
  a language filter. Click through straight to the streamer's channel.
- **Per-game stats** — achievement progress and a live "playing now"
  player count for every game in your library.
- **Discovery panels**:
  - *Coming Soon* and *Most Wishlisted* — ranked lists pulled from
    Steam's own storefront.
  - *Now Streaming* — live streams for the games you've favourited.
  - *Trending on Twitch* — the most-watched streams on Twitch right now.
- **Mobile-friendly layout** — a dedicated Home / Library / Twitch tab
  layout on mobile, distinct from the desktop view where everything is
  visible at once.

## Tech stack

- **Backend**: Spring Boot 4.1.0, Java 25, MongoDB. Talks to the Steam
  Web API and Twitch's Helix API.
- **Frontend**: React 19, TypeScript, Vite 8.
- **CI/CD**: GitHub Actions builds and pushes a Docker image
  (`build-backend.yml`, `build-frontend.yml`, `deploy.yml`); deployed on
  Render, which pulls the prebuilt image. The frontend is served as
  static resources from the backend jar in production.

## Running locally

Backend (`backend/`), via `./mvnw spring-boot:run`, needs these
environment variables set (see `application.properties`):

- `MONGO_DB_URI`
- `STEAM_API_KEY`
- `TWITCH_CLIENT_ID` / `TWITCH_CLIENT_SECRET`

Frontend (`frontend/`), via `npm run dev`, runs on Vite's dev server and
proxies `/api/*` and `/logout` to `localhost:8080` (see
`vite.config.ts`), so the backend needs to be running alongside it.
