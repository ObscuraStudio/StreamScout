package org.obscura.backend.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void handleUnexpected_returnsGenericMessage_notTheRawExceptionText() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ErrorResponse response = handler.handleUnexpected(new RuntimeException("db connection refused"));

        assertThat(response.message()).isEqualTo("An unexpected error occurred");
        assertThat(response.message()).doesNotContain("db connection refused");
        assertThat(response.timestamp()).isNotNull();
    }
}
