package com.laby.scheduling.scheduling_service.entity;

public enum Level {
    KG,
    PRIMARY,
    SECONDARY,
    HIGHER_SECONDARY;

    public Level oneLevelAbove() {
        return switch (this) {
            case KG -> PRIMARY;
            case PRIMARY -> SECONDARY;
            case SECONDARY -> HIGHER_SECONDARY;
            case HIGHER_SECONDARY -> null;
        };
    }
}
