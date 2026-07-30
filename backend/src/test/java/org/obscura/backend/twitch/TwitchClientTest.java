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
    private static final String STREAMS_URL =
            "https://api.twitch.tv/helix/streams?game_id={gameId}&first=100";
    private static final String STREAMS_URL_WITH_LANGUAGE =
            "https://api.twitch.tv/helix/streams?game_id={gameId}&first=100&language={language}";
    private static final String TOP_STREAMS_URL =
            "https://api.twitch.tv/helix/streams?first={first}";

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

        List<TwitchStream> streams = client.getLiveStreams("Team Fortress 2", null);

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

        List<TwitchStream> streams = client.getLiveStreams("Some Obscure Game", null);

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

        List<TwitchStream> streams = client.getLiveStreams("Team Fortress 2", null);

        assertThat(streams).isEmpty();
    }

    @Test
    void getLiveStreams_throwsTwitchApiException_whenGameLookupFails() {
        mockServer.expect(requestToUriTemplate(SEARCH_CATEGORIES_URL, "Team Fortress 2"))
                .andExpect(method(GET))
                .andRespond(withServerError());

        assertThrows(TwitchApiException.class, () -> client.getLiveStreams("Team Fortress 2", null));
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

        List<TwitchStream> streams = client.getLiveStreams("FINAL FANTASY XV WINDOWS EDITION", null);

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

        List<TwitchStream> streams = client.getLiveStreams("FINAL FANTASY XV WINDOWS EDITION", null);

        assertThat(streams).isEmpty();
    }

    @Test
    void getLiveStreams_appendsLanguageParam_whenLanguageProvided() {
        mockServer.expect(requestToUriTemplate(SEARCH_CATEGORIES_URL, "Team Fortress 2"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"data": [{"id": "1234", "name": "Team Fortress 2", "box_art_url": "https://example.com/box.jpg"}]}
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestToUriTemplate(STREAMS_URL_WITH_LANGUAGE, "1234", "de"))
                .andExpect(method(GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andExpect(header("Client-Id", "test-client-id"))
                .andRespond(withSuccess("""
                        {"data": [{
                          "user_name": "DeutscherStreamer",
                          "user_login": "deutscherstreamer",
                          "title": "Spielt TF2",
                          "viewer_count": 17,
                          "thumbnail_url": "https://static-cdn.jtvnw.net/previews-ttv/live_user_deutscherstreamer-{width}x{height}.jpg"
                        }]}
                        """, MediaType.APPLICATION_JSON));

        List<TwitchStream> streams = client.getLiveStreams("Team Fortress 2", "de");

        assertThat(streams).hasSize(1);
        TwitchStream stream = streams.getFirst();
        assertThat(stream.streamerName()).isEqualTo("DeutscherStreamer");
        assertThat(stream.streamerLogin()).isEqualTo("deutscherstreamer");
        assertThat(stream.viewerCount()).isEqualTo(17);
    }

    @Test
    void getTopStreams_returnsParsedStreams_withThumbnailPlaceholdersSubstituted() {
        mockServer.expect(requestToUriTemplate(TOP_STREAMS_URL, 8))
                .andExpect(method(GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andExpect(header("Client-Id", "test-client-id"))
                .andRespond(withSuccess("""
                        {"data": [{
                          "user_name": "BigStreamer",
                          "user_login": "bigstreamer",
                          "title": "Playing something popular",
                          "viewer_count": 45000,
                          "thumbnail_url": "https://static-cdn.jtvnw.net/previews-ttv/live_user_bigstreamer-{width}x{height}.jpg"
                        }]}
                        """, MediaType.APPLICATION_JSON));

        List<TwitchStream> streams = client.getTopStreams(8);

        assertThat(streams).hasSize(1);
        TwitchStream stream = streams.getFirst();
        assertThat(stream.streamerName()).isEqualTo("BigStreamer");
        assertThat(stream.streamerLogin()).isEqualTo("bigstreamer");
        assertThat(stream.viewerCount()).isEqualTo(45000);
        assertThat(stream.thumbnailUrl())
                .isEqualTo("https://static-cdn.jtvnw.net/previews-ttv/live_user_bigstreamer-320x180.jpg");
    }

    @Test
    void getTopStreams_returnsEmptyList_whenNoStreamsReturned() {
        mockServer.expect(requestToUriTemplate(TOP_STREAMS_URL, 8))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"data": []}
                        """, MediaType.APPLICATION_JSON));

        List<TwitchStream> streams = client.getTopStreams(8);

        assertThat(streams).isEmpty();
    }

    @Test
    void getTopStreams_throwsTwitchApiException_whenRequestFails() {
        mockServer.expect(requestToUriTemplate(TOP_STREAMS_URL, 8))
                .andExpect(method(GET))
                .andRespond(withServerError());

        assertThrows(TwitchApiException.class, () -> client.getTopStreams(8));
    }
}
