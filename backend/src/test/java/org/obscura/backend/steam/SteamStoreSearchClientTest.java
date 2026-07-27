package org.obscura.backend.steam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.obscura.backend.exception.SteamApiException;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SteamStoreSearchClientTest {

    private static final String SEARCH_URL =
            "https://store.steampowered.com/search/results/?query&start=0&count={count}"
                    + "&dynamic_data=&sort_by=_ASC&filter={filter}&infinite=1&cc=us&l=english";

    private MockRestServiceServer mockServer;
    private SteamStoreSearchClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new SteamStoreSearchClient(builder.build());
    }

    private static String resultRow(String appId, String name, String releaseDate, String imageUrl) {
        return """
                <a href="https://store.steampowered.com/app/%s/" data-ds-appid="%s" class="search_result_row ds_collapse_flag">
                <div class="col search_capsule"><img src="%s"></div>
                <div class="responsive_search_name_combined">
                <div class="col search_name ellipsis"><span class="title">%s</span></div>
                <div class="col search_released responsive_secondrow">%s</div>
                </div>
                </a>
                """.formatted(appId, appId, imageUrl, name, releaseDate);
    }

    private static String jsonResponse(String resultsHtml) {
        String escaped = resultsHtml
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
        return "{\"success\": 1, \"results_html\": \"" + escaped + "\", \"total_count\": 1, \"start\": 0}";
    }

    @Test
    void getComingSoon_returnsParsedListings_inRankOrder() {
        String html = resultRow("1234", "Example Game", "Coming Soon", "https://cdn.example.com/1234/capsule.jpg")
                + resultRow("5678", "Second Game", "Q4 2026", "https://cdn.example.com/5678/capsule.jpg");
        mockServer.expect(requestToUriTemplate(SEARCH_URL, 10, "popularcomingsoon"))
                .andExpect(method(GET))
                .andRespond(withSuccess(jsonResponse(html), MediaType.APPLICATION_JSON));

        List<SteamStoreListing> listings = client.getComingSoon(10);

        assertThat(listings).hasSize(2);
        assertThat(listings.get(0)).isEqualTo(
                new SteamStoreListing(1234, "Example Game", "Coming Soon", "https://cdn.example.com/1234/capsule.jpg", 1));
        assertThat(listings.get(1)).isEqualTo(
                new SteamStoreListing(5678, "Second Game", "Q4 2026", "https://cdn.example.com/5678/capsule.jpg", 2));
    }

    @Test
    void getMostWishlisted_usesPopularWishlistFilter() {
        String html = resultRow("999", "Anticipated Game", "Q1 2027", "https://cdn.example.com/999/capsule.jpg");
        mockServer.expect(requestToUriTemplate(SEARCH_URL, 10, "popularwishlist"))
                .andExpect(method(GET))
                .andRespond(withSuccess(jsonResponse(html), MediaType.APPLICATION_JSON));

        List<SteamStoreListing> listings = client.getMostWishlisted(10);

        assertThat(listings).containsExactly(
                new SteamStoreListing(999, "Anticipated Game", "Q1 2027", "https://cdn.example.com/999/capsule.jpg", 1));
    }

    @Test
    void getComingSoon_respectsLimit_evenWhenMoreRowsPresent() {
        String html = resultRow("1", "Game One", "Soon", "https://cdn/1.jpg")
                + resultRow("2", "Game Two", "Soon", "https://cdn/2.jpg")
                + resultRow("3", "Game Three", "Soon", "https://cdn/3.jpg");
        mockServer.expect(requestToUriTemplate(SEARCH_URL, 2, "popularcomingsoon"))
                .andExpect(method(GET))
                .andRespond(withSuccess(jsonResponse(html), MediaType.APPLICATION_JSON));

        List<SteamStoreListing> listings = client.getComingSoon(2);

        assertThat(listings).hasSize(2);
        assertThat(listings).extracting(SteamStoreListing::appId).containsExactly(1, 2);
    }

    @Test
    void getComingSoon_returnsEmptyList_whenNoRowsMatch() {
        mockServer.expect(requestToUriTemplate(SEARCH_URL, 10, "popularcomingsoon"))
                .andExpect(method(GET))
                .andRespond(withSuccess(jsonResponse(""), MediaType.APPLICATION_JSON));

        List<SteamStoreListing> listings = client.getComingSoon(10);

        assertThat(listings).isEmpty();
    }

    @Test
    void getComingSoon_throwsSteamApiException_whenRequestFails() {
        mockServer.expect(requestToUriTemplate(SEARCH_URL, 10, "popularcomingsoon"))
                .andExpect(method(GET))
                .andRespond(withServerError());

        assertThrows(SteamApiException.class, () -> client.getComingSoon(10));
    }

    @Test
    void getComingSoon_cachesResults_soSecondCallDoesNotHitSteamAgain() {
        String html = resultRow("1234", "Example Game", "Coming Soon", "https://cdn.example.com/1234/capsule.jpg");
        mockServer.expect(requestToUriTemplate(SEARCH_URL, 10, "popularcomingsoon"))
                .andExpect(method(GET))
                .andRespond(withSuccess(jsonResponse(html), MediaType.APPLICATION_JSON));

        List<SteamStoreListing> first = client.getComingSoon(10);
        List<SteamStoreListing> second = client.getComingSoon(10);

        assertThat(second).isEqualTo(first);
        mockServer.verify();
    }
}
