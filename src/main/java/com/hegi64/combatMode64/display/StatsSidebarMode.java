package com.hegi64.combatMode64.display;

public enum StatsSidebarMode {
    OFF,
    COMPACT,
    EXTENDED;

    public static StatsSidebarMode fromString(String raw) {
        if (raw == null) {
            return OFF;
        }

        return switch (raw.trim().toLowerCase()) {
            case "on", "compact" -> COMPACT;
            case "extended" -> EXTENDED;
            case "off" -> OFF;
            default -> OFF;
        };
    }
}

