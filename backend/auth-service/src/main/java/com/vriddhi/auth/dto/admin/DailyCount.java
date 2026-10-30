package com.vriddhi.auth.dto.admin;

import java.time.LocalDate;

public record DailyCount(LocalDate date, long count) {
}
