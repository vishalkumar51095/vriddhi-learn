package com.vriddhi.progress.dto;

/** applied=false means this attempt had already been counted (a harmless retry). */
public record InternalResultsResponse(boolean applied) {
}
