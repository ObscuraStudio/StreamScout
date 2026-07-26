package org.obscura.backend.steam;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.obscura.backend.exception.SteamApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class SteamStoreSearchClient {

    private static final Logger log = LoggerFactory.getLogger(SteamStoreSearchClient.class);
    private static final long CACHE_TTL_SECONDS = 45 * 60;
    private static final String SEARCH_URL =
            "https://store.steampowered.com/search/results/?query&start=0&count={count}"
                    + "&dynamic_data=&sort_by=_ASC&filter={filter}&infinite=1&cc=us&l=english";

    private final RestClient restClient;
    private final AtomicReference<CachedListings> comingSoonCache = new AtomicReference<>();
    private final AtomicReference<CachedListings> mostWishlistedCache = new AtomicReference<>();

    public SteamStoreSearchClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<SteamStoreListing> getComingSoon(int limit) {
        return fetchCached(comingSoonCache, "popularcomingsoon", limit);
    }

    public List<SteamStoreListing> getMostWishlisted(int limit) {
        return fetchCached(mostWishlistedCache, "popularwishlist", limit);
    }

    private List<SteamStoreListing> fetchCached(AtomicReference<CachedListings> cache, String filter, int limit) {
        CachedListings current = cache.get();
        if (current != null && current.isValid()) {
            return current.listings();
        }

        List<SteamStoreListing> fresh = fetchListings(filter, limit);
        cache.set(new CachedListings(fresh, Instant.now().plusSeconds(CACHE_TTL_SECONDS)));
        return fresh;
    }

    private List<SteamStoreListing> fetchListings(String filter, int limit) {
        try {
            SearchResultsResponse response = restClient.get()
                    .uri(SEARCH_URL, limit, filter)
                    .retrieve()
                    .body(SearchResultsResponse.class);

            if (response == null || response.results_html() == null) {
                return List.of();
            }
            return parseListings(response.results_html(), limit);
        } catch (Exception e) {
            log.warn("Failed to fetch Steam store search results for filter={}: {}", filter, e.getMessage());
            throw new SteamApiException("Could not reach Steam");
        }
    }

    private List<SteamStoreListing> parseListings(String resultsHtml, int limit) {
        Document doc = Jsoup.parse(resultsHtml);
        Elements rows = doc.select("a.search_result_row");

        List<SteamStoreListing> listings = new ArrayList<>();
        int rank = 1;
        for (Element row : rows) {
            if (listings.size() >= limit) {
                break;
            }
            String appIdAttr = row.attr("data-ds-appid");
            if (appIdAttr.isBlank() || appIdAttr.contains(",")) {
                continue;
            }

            Element titleEl = row.selectFirst("span.title");
            Element releaseEl = row.selectFirst("div.search_released");
            Element imageEl = row.selectFirst("img");

            listings.add(new SteamStoreListing(
                    Integer.parseInt(appIdAttr),
                    titleEl == null ? "" : titleEl.text(),
                    releaseEl == null ? "" : releaseEl.text(),
                    imageEl == null ? "" : imageEl.attr("src"),
                    rank));
            rank++;
        }
        return listings;
    }

    private record CachedListings(List<SteamStoreListing> listings, Instant expiresAt) {
        boolean isValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }

    private record SearchResultsResponse(int success, String results_html, int total_count, int start) {
    }
}
