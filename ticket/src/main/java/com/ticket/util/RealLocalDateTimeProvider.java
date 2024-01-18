package com.ticket.util;

import java.time.LocalDateTime;

public class RealLocalDateTimeProvider implements LocalDateTimeProvider{

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public LocalDateTime fakeLocalDateTime() {
        return LocalDateTime.of(2023, 1, 1, 12, 0);
    }
}
