package org.obscura.backend.twitch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.obscura.backend.exception.TwitchApiException;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TwitchClientTest {

    private static final String SEARCH_CATEGORIES_URL =
            "https://api.twitch.tv/helix/search/categories?query={name}&first=1";
    private static final String STREAMS_URL = "https://api.twitch.tv/helix/streams?game_id={gameId}&first=6";

    private MockRestServiceServer mockServer;
    private TwitchClient client;
    private TwitchAuthClient authClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        authClient = mock(TwitchAuthClient.class);
        when(authClient.getAppAccessToken()).thenReturn("test-token");
        client = new TwitchClient(builder.build(), authClient, "test-client-id");
    }

    @Test
    void getLiveStreams_returnsParsedStreams_withThumbnailPlaceholdersSubstituted() {
        mockServer.expect(requestToUriTemplate(SEARCH_CATEGORIES_URL, "Team Fortress 2"))
                .andExpect(method(GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andExpect(header("Client-Id", "test-client-id"))
                .andRespond(withSuccess("""
                        {"data": [{"id": "1234", "name": "Team Fortress 2", "box_art_url": "https://example.com/box.jpg"}]}
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestToUriTemplate(STREAMS_URL, "1234"))
                .andExpect(method(GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andExpect(header("Client-Id", "test-client-id"))
                .andRespond(withSuccess("""
                        {"data": [{
                          "user_name": "SomeStreamer",
                          "user_login": "somestreamer",
                          "title": "Playing TF2",
                          "viewer_count": 42,
                          "thumbnail_url": "https://static-cdn.jtvnw.net/previews-ttv/live_user_somestreamer-{width}x{height}.jpg"
                        }]}
                        """, MediaType.APPLICATION_JSON));

        List<TwitchStream> streams = client.getLiveStreams("Team Fortress 2");

        assertThat(streams).hasSize(1);
        TwitchStream stream = streams.getFirst();
        assertThat(stream.streamerName()).isEqualTo("SomeStreamer");
        assertThat(stream.streamerLogin()).isEqualTo("somestreamer");
        assertThat(stream.title()).isEqualTo("Playing TF2");
        assertThat(stream.viewerCount()).isEqualTo(42);
        assertThat(stream.thumbnailUrl())
                .isEqualTo("https://static-cdn.jtvnw.net/previews-ttv/live_user_somestreamer-320x180.jpg");
    }

    @Test
    void getLiveStreams_returnsEmptyList_whenNoGameCategoryMatches() {
        mockServer.expect(requestToUriTemplate(SEARCH_CATEGORIES_URL, "Some Obscure Game"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"data": []}
                        """, MediaType.APPLICATION_JSON));

        List<TwitchStream> streams = client.getLiveStreams("Some Obscure Game");

        assertThat(streams).isEmpty();
    }

    @Test
    void getLiveStreams_returnsEmptyList_whenCategoryHasNoLiveStreams() {
        mockServer.expect(requestToUriTemplate(SEARCH_CATEGORIES_URL, "Team Fortress 2"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"data": [{"id": "1234", "name": "Team Fortress 2", "box_art_url": "https://example.com/box.jpg"}]}
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestToUriTemplate(STREAMS_URL, "1234"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"data": []}
                        """, MediaType.APPLICATION_JSON));

        List<TwitchStream> streams = client.getLiveStreams("Team Fortress 2");

        assertThat(streams).isEmpty();
    }

    @Test
    void getLiveStreams_throwsTwitchApiException_whenGameLookupFails() {
        mockServer.expect(requestToUriTemplate(SEARCH_CATEGORIES_URL, "Team Fortress 2"))
                .andExpect(method(GET))
                .andRespond(withServerError());

        assertThrows(TwitchApiException.class, () -> client.getLiveStreams("Team Fortress 2"));
    }

    @Test
    void getLiveStreams_stripsEditionSuffix_andResolvesOnFirstAttempt() {
        mockServer.expect(requestToUriTemplate(SEARCH_CATEGORIES_URL, "FINAL FANTASY XV"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"data": [{"id": "5678", "name": "FINAL FANTASY XV", "box_art_url": "https://example.com/box.jpg"}]}
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestToUriTemplate(STREAMS_URL, "5678"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"data": []}
                        """, MediaType.APPLICATION_JSON));

        List<TwitchStream> streams = client.getLiveStreams("FINAL FANTASY XV WINDOWS EDITION");

        assertThat(streams).isEmpty();
    }

    @Test
    void getLiveStreams_fallsBackToOriginalName_whenStrippedNameHasNoMatch() {
        mockServer.expect(requestToUriTemplate(SEARCH_CATEGORIES_URL, "FINAL FANTASY XV"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"data": []}
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestToUriTemplate(SEARCH_CATEGORIES_URL, "FINAL FANTASY XV WINDOWS EDITION"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"data": [{"id": "9999", "name": "FINAL FANTASY XV WINDOWS EDITION", "box_art_url": "https://example.com/box.jpg"}]}
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestToUriTemplate(STREAMS_URL, "9999"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"data": []}
                        """, MediaType.APPLICATION_JSON));

        List<TwitchStream> streams = client.getLiveStreams("FINAL FANTASY XV WINDOWS EDITION");

        assertThat(streams).isEmpty();
    }
}
