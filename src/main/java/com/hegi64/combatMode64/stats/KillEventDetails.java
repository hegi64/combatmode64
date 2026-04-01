package com.hegi64.combatMode64.stats;

public record KillEventDetails(
    String killerName,
    String victimName,
    String worldName,
    String deathCause,
    String weaponType,
    double killerX,
    double killerY,
    double killerZ,
    double victimX,
    double victimY,
    double victimZ,
    double distance,
    long killedAt
) {
}

