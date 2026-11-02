package com.vriddhi.auth.controller;

import com.vriddhi.auth.dao.AdminQueryDao;
import com.vriddhi.auth.dto.admin.AuditLogRow;
import com.vriddhi.auth.dto.admin.PagedResponse;
import com.vriddhi.auth.dto.admin.UserStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminQueryDao dao;

    @GetMapping("/stats/users")
    public UserStatsResponse userStats() {
        return dao.userStats(14);
    }

    @GetMapping("/audit-logs")
    public PagedResponse<AuditLogRow> auditLogs(@RequestParam(defaultValue = "") String event,
                                                @RequestParam(defaultValue = "") String q,
                                                @RequestParam(defaultValue = "") String actorId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "25") int size) {
        int safeSize = Math.min(Math.max(size, 1), 200);
        return dao.searchAudit(event.trim().toUpperCase(), q.trim(), actorId.trim(), Math.max(page, 0), safeSize);
    }
}
