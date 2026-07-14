package org.obscura.backend.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerSteamApiTest {

    @Test
    void handleSteamApi_returnsTheExceptionMessageAndTimestamp() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ErrorResponse response = handler.handleSteamApi(new SteamApiException("Could not reach Steam"));

        assertThat(response.message()).isEqualTo("Could not reach Steam");
        assertThat(response.timestamp()).isNotNull();
    }
}
