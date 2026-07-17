package org.obscura.backend.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTwitchApiTest {

    @Test
    void handleTwitchApi_returnsTheExceptionMessageAndTimestamp() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ErrorResponse response = handler.handleTwitchApi(new TwitchApiException("Could not reach Twitch"));

        assertThat(response.message()).isEqualTo("Could not reach Twitch");
        assertThat(response.timestamp()).isNotNull();
    }
}
