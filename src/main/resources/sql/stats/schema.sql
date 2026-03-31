CREATE TABLE IF NOT EXISTS player_stats (
    player_uuid TEXT PRIMARY KEY,
    player_name TEXT NOT NULL,
    kills INTEGER NOT NULL DEFAULT 0,
    deaths INTEGER NOT NULL DEFAULT 0,
    updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS player_kill_pairs (
    killer_uuid TEXT NOT NULL,
    victim_uuid TEXT NOT NULL,
    kill_count INTEGER NOT NULL DEFAULT 0,
    updated_at INTEGER NOT NULL,
    PRIMARY KEY (killer_uuid, victim_uuid)
);

CREATE TABLE IF NOT EXISTS kill_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    killer_uuid TEXT NOT NULL,
    killer_name TEXT NOT NULL,
    victim_uuid TEXT NOT NULL,
    victim_name TEXT NOT NULL,
    world_name TEXT NOT NULL,
    death_cause TEXT NOT NULL,
    weapon_type TEXT NOT NULL DEFAULT 'UNKNOWN',
    killer_x REAL NOT NULL DEFAULT 0,
    killer_y REAL NOT NULL DEFAULT 0,
    killer_z REAL NOT NULL DEFAULT 0,
    victim_x REAL NOT NULL,
    victim_y REAL NOT NULL,
    victim_z REAL NOT NULL,
    distance REAL NOT NULL DEFAULT 0,
    killed_at INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_player_kill_pairs_killer_uuid
    ON player_kill_pairs (killer_uuid);

CREATE INDEX IF NOT EXISTS idx_player_kill_pairs_victim_uuid
    ON player_kill_pairs (victim_uuid);

CREATE INDEX IF NOT EXISTS idx_kill_events_killer_uuid
    ON kill_events (killer_uuid);

CREATE INDEX IF NOT EXISTS idx_kill_events_victim_uuid
    ON kill_events (victim_uuid);

CREATE INDEX IF NOT EXISTS idx_kill_events_killed_at
    ON kill_events (killed_at);

ALTER TABLE kill_events ADD COLUMN weapon_type TEXT NOT NULL DEFAULT 'UNKNOWN';
ALTER TABLE kill_events ADD COLUMN killer_x REAL NOT NULL DEFAULT 0;
ALTER TABLE kill_events ADD COLUMN killer_y REAL NOT NULL DEFAULT 0;
ALTER TABLE kill_events ADD COLUMN killer_z REAL NOT NULL DEFAULT 0;
ALTER TABLE kill_events ADD COLUMN distance REAL NOT NULL DEFAULT 0;
