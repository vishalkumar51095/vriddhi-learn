package com.vriddhi.learning.service;

import com.vriddhi.learning.dto.ImportResult;
import com.vriddhi.learning.dto.LessonImportItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads every classpath:lessons/*.md at startup. Adding a new lesson is then: drop a file into
 * src/main/resources/lessons, redeploy. For no-redeploy updates use POST /api/admin/lessons/import.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LessonBootstrapLoader implements ApplicationRunner {

    private final LessonImportService importService;

    @Value("${learning.bootstrap.enabled:true}")
    private boolean enabled;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!enabled) {
            log.info("Lesson bootstrap disabled");
            return;
        }
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:lessons/*.md");
        List<LessonImportItem> items = new ArrayList<>();
        for (Resource resource : resources) {
            String text = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            items.add(LessonFileParser.parse(String.valueOf(resource.getFilename()), text));
        }
        if (items.isEmpty()) {
            log.info("No lesson files found on the classpath");
            return;
        }
        ImportResult result = importService.importAll(items);
        log.info("Lesson bootstrap: {} created, {} updated, {} unchanged", result.created(), result.updated(), result.unchanged());
    }
}
