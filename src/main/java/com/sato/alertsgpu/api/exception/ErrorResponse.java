package com.sato.alertsgpu.api.exception;

import java.time.Instant;

public record ErrorResponse(
    String code,
    String message,
    Instant timestamp
) {
}
