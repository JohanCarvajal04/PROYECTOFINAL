package com.app.uteq.Dtos;

import java.time.LocalDateTime;

public record EmailResponse(
        boolean ok,
        String message,
        LocalDateTime timestamp
) {}
