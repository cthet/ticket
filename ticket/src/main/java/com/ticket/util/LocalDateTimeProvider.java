package com.ticket.util;

import java.time.LocalDateTime;

public interface LocalDateTimeProvider {

    LocalDateTime now();

    LocalDateTime fakeLocalDateTime();
}
