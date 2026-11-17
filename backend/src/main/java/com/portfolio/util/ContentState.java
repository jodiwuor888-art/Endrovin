package com.portfolio.util;

public enum ContentState {
    DRAFT,
    PUBLISHED,
    UNPUBLISHED,
    ARCHIVED;

    public static ContentState fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return PUBLISHED;
        }
        try {
            return ContentState.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return PUBLISHED;
        }
    }
}
