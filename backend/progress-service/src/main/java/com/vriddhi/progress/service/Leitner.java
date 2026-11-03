package com.vriddhi.progress.service;

import java.time.Duration;

/** Leitner-box scheduling: a correct answer moves a question up a box (longer gap), a wrong answer resets it. */
public final class Leitner {

    public static final int MIN_BOX = 1;
    public static final int MAX_BOX = 5;

    private static final int[] INTERVAL_DAYS = {1, 2, 4, 8, 16};

    private Leitner() {
    }

    /** Box after answering. A brand-new question answered correctly goes straight to box 2. */
    public static int nextBox(int currentBox, boolean correct) {
        return correct ? Math.min(MAX_BOX, currentBox + 1) : MIN_BOX;
    }

    public static Duration interval(int box) {
        int index = Math.max(MIN_BOX, Math.min(MAX_BOX, box)) - 1;
        return Duration.ofDays(INTERVAL_DAYS[index]);
    }
}
