package com.vriddhi.progress.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeitnerTest {

    @Test
    void correctAnswerMovesUpOneBox() {
        assertEquals(2, Leitner.nextBox(1, true));
        assertEquals(4, Leitner.nextBox(3, true));
    }

    @Test
    void correctAnswerNeverExceedsTopBox() {
        assertEquals(Leitner.MAX_BOX, Leitner.nextBox(Leitner.MAX_BOX, true));
    }

    @Test
    void wrongAnswerAlwaysResetsToFirstBox() {
        assertEquals(Leitner.MIN_BOX, Leitner.nextBox(1, false));
        assertEquals(Leitner.MIN_BOX, Leitner.nextBox(5, false));
    }

    @Test
    void intervalsDoubleFromOneDayToSixteenDays() {
        assertEquals(Duration.ofDays(1), Leitner.interval(1));
        assertEquals(Duration.ofDays(2), Leitner.interval(2));
        assertEquals(Duration.ofDays(4), Leitner.interval(3));
        assertEquals(Duration.ofDays(8), Leitner.interval(4));
        assertEquals(Duration.ofDays(16), Leitner.interval(5));
    }

    @Test
    void outOfRangeBoxesAreClamped() {
        assertEquals(Duration.ofDays(1), Leitner.interval(0));
        assertEquals(Duration.ofDays(16), Leitner.interval(9));
    }
}
