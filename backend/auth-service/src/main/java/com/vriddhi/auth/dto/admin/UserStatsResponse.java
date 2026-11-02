package com.vriddhi.auth.dto.admin;

import java.util.List;
import java.util.Map;

public record UserStatsResponse(long total, long active, long suspended, long pending, long newLast7Days,
                                long loginsLast24h, long failedLoginsLast24h,
                                Map<String, Long> byRole, List<DailyCount> registrationsPerDay) {
}
