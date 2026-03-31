package com.hegi64.combatMode64.stats;

import com.hegi64.combatMode64.Main;
import com.hegi64.combatMode64.utils.ConfigUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public final class StatsService {

    private static final String SCHEMA_RESOURCE_PATH = "sql/stats/schema.sql";

    private final Main plugin;
    private ExecutorService writeExecutor;
    private volatile boolean active;
    private String jdbcUrl;

    public StatsService(Main plugin) {
        this.plugin = plugin;
    }

    public boolean start() {
        if (!ConfigUtil.isStatsEnabled()) {
            plugin.getLogger().info("Stats are disabled in config.");
            return false;
        }

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().severe("Failed to create plugin data folder for stats database.");
            return false;
        }

        File databaseFile = new File(dataFolder, ConfigUtil.getStatsSqliteFileName());
        jdbcUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();

        try (Connection connection = openConnection()) {
            executeSchema(connection);
        } catch (SQLException | IOException e) {
            plugin.getLogger().severe("Failed to initialize stats database: " + e.getMessage());
            return false;
        }

        writeExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "CombatMode64-StatsWriter");
            thread.setDaemon(true);
            return thread;
        });

        active = true;
        plugin.getLogger().info("Stats database initialized at " + databaseFile.getAbsolutePath());
        return true;
    }

    public void stop() {
        active = false;
        if (writeExecutor == null) {
            return;
        }

        writeExecutor.shutdown();
        try {
            if (!writeExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                writeExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            writeExecutor.shutdownNow();
        }
    }

    public boolean isActive() {
        return active;
    }

    public void recordKill(
        UUID killerUuid,
        String killerName,
        UUID victimUuid,
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
        double distance
    ) {
        if (!active || killerUuid == null || victimUuid == null || worldName == null || deathCause == null || weaponType == null) {
            return;
        }

        try {
            writeExecutor.execute(() -> {
                Connection connection = null;
                try {
                    connection = openConnection();
                    connection.setAutoCommit(false);
                    long now = Instant.now().getEpochSecond();

                    upsertKill(connection, killerUuid, killerName, now);
                    upsertDeath(connection, victimUuid, victimName, now);
                    upsertKillPair(connection, killerUuid, victimUuid, now);
                    insertKillEvent(
                        connection,
                        killerUuid,
                        killerName,
                        victimUuid,
                        victimName,
                        worldName,
                        deathCause,
                        weaponType,
                        killerX,
                        killerY,
                        killerZ,
                        victimX,
                        victimY,
                        victimZ,
                        distance,
                        now
                    );

                    connection.commit();
                } catch (SQLException e) {
                    if (connection != null) {
                        try {
                            connection.rollback();
                        } catch (SQLException ignored) {
                        }
                    }
                    plugin.getLogger().warning("Failed to persist combat stats: " + e.getMessage());
                } finally {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException ignored) {
                        }
                    }
                }
            });
        } catch (RejectedExecutionException ignored) {
            // Service is stopping; ignore late events.
        }
    }

    public Optional<PlayerStats> getPlayerStats(UUID playerUuid) {
        if (!active || playerUuid == null) {
            return Optional.empty();
        }

        String sql = "SELECT player_name, kills, deaths FROM player_stats WHERE player_uuid = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new PlayerStats(
                    resultSet.getString("player_name"),
                    resultSet.getInt("kills"),
                    resultSet.getInt("deaths")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load player stats: " + e.getMessage());
            return Optional.empty();
        }
    }

    public int getKillCountBetween(UUID killerUuid, UUID victimUuid) {
        if (!active || killerUuid == null || victimUuid == null) {
            return 0;
        }

        String sql = "SELECT kill_count FROM player_kill_pairs WHERE killer_uuid = ? AND victim_uuid = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, killerUuid.toString());
            statement.setString(2, victimUuid.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("kill_count") : 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load kill-pair stats: " + e.getMessage());
            return 0;
        }
    }

    public Optional<KillEventDetails> getLastKillForVictim(UUID victimUuid) {
        if (!active || victimUuid == null) {
            return Optional.empty();
        }

        String sql = """
            SELECT killer_name, victim_name, world_name, death_cause, weapon_type,
                   killer_x, killer_y, killer_z,
                   victim_x, victim_y, victim_z,
                   distance, killed_at
            FROM kill_events
            WHERE victim_uuid = ?
            ORDER BY killed_at DESC, id DESC
            LIMIT 1
            """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, victimUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new KillEventDetails(
                    resultSet.getString("killer_name"),
                    resultSet.getString("victim_name"),
                    resultSet.getString("world_name"),
                    resultSet.getString("death_cause"),
                    resultSet.getString("weapon_type"),
                    resultSet.getDouble("killer_x"),
                    resultSet.getDouble("killer_y"),
                    resultSet.getDouble("killer_z"),
                    resultSet.getDouble("victim_x"),
                    resultSet.getDouble("victim_y"),
                    resultSet.getDouble("victim_z"),
                    resultSet.getDouble("distance"),
                    resultSet.getLong("killed_at")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load last kill event: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<KillEventDetails> getLastKillByKiller(UUID killerUuid) {
        if (!active || killerUuid == null) {
            return Optional.empty();
        }

        String sql = """
            SELECT killer_name, victim_name, world_name, death_cause, weapon_type,
                   killer_x, killer_y, killer_z,
                   victim_x, victim_y, victim_z,
                   distance, killed_at
            FROM kill_events
            WHERE killer_uuid = ?
            ORDER BY killed_at DESC, id DESC
            LIMIT 1
            """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, killerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new KillEventDetails(
                    resultSet.getString("killer_name"),
                    resultSet.getString("victim_name"),
                    resultSet.getString("world_name"),
                    resultSet.getString("death_cause"),
                    resultSet.getString("weapon_type"),
                    resultSet.getDouble("killer_x"),
                    resultSet.getDouble("killer_y"),
                    resultSet.getDouble("killer_z"),
                    resultSet.getDouble("victim_x"),
                    resultSet.getDouble("victim_y"),
                    resultSet.getDouble("victim_z"),
                    resultSet.getDouble("distance"),
                    resultSet.getLong("killed_at")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load last kill by killer: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA synchronous = NORMAL");
        }
        return connection;
    }

    private void executeSchema(Connection connection) throws IOException, SQLException {
        String sqlScript = loadSchemaScript();
        for (String statement : sqlScript.split(";")) {
            String trimmed = statement.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(trimmed)) {
                preparedStatement.execute();
            } catch (SQLException e) {
                if (!isIgnorableSchemaError(e)) {
                    throw e;
                }
            }
        }
    }

    private boolean isIgnorableSchemaError(SQLException exception) {
        String message = exception.getMessage();
        return message != null && message.toLowerCase().contains("duplicate column name");
    }

    private String loadSchemaScript() throws IOException {
        InputStream inputStream = plugin.getResource(SCHEMA_RESOURCE_PATH);
        if (inputStream == null) {
            throw new IOException("Missing SQL schema resource: " + SCHEMA_RESOURCE_PATH);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--")) {
                    continue;
                }
                builder.append(line).append('\n');
            }
            return builder.toString();
        }
    }

    private void upsertKill(Connection connection, UUID killerUuid, String killerName, long timestamp) throws SQLException {
        String sql = """
            INSERT INTO player_stats (player_uuid, player_name, kills, deaths, updated_at)
            VALUES (?, ?, 1, 0, ?)
            ON CONFLICT(player_uuid) DO UPDATE SET
                player_name = excluded.player_name,
                kills = player_stats.kills + 1,
                updated_at = excluded.updated_at
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, killerUuid.toString());
            statement.setString(2, killerName);
            statement.setLong(3, timestamp);
            statement.executeUpdate();
        }
    }

    private void upsertDeath(Connection connection, UUID victimUuid, String victimName, long timestamp) throws SQLException {
        String sql = """
            INSERT INTO player_stats (player_uuid, player_name, kills, deaths, updated_at)
            VALUES (?, ?, 0, 1, ?)
            ON CONFLICT(player_uuid) DO UPDATE SET
                player_name = excluded.player_name,
                deaths = player_stats.deaths + 1,
                updated_at = excluded.updated_at
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, victimUuid.toString());
            statement.setString(2, victimName);
            statement.setLong(3, timestamp);
            statement.executeUpdate();
        }
    }

    private void upsertKillPair(Connection connection, UUID killerUuid, UUID victimUuid, long timestamp) throws SQLException {
        String sql = """
            INSERT INTO player_kill_pairs (killer_uuid, victim_uuid, kill_count, updated_at)
            VALUES (?, ?, 1, ?)
            ON CONFLICT(killer_uuid, victim_uuid) DO UPDATE SET
                kill_count = player_kill_pairs.kill_count + 1,
                updated_at = excluded.updated_at
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, killerUuid.toString());
            statement.setString(2, victimUuid.toString());
            statement.setLong(3, timestamp);
            statement.executeUpdate();
        }
    }

    private void insertKillEvent(
        Connection connection,
        UUID killerUuid,
        String killerName,
        UUID victimUuid,
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
        long timestamp
    ) throws SQLException {
        String sql = """
            INSERT INTO kill_events (
                killer_uuid,
                killer_name,
                victim_uuid,
                victim_name,
                world_name,
                death_cause,
                weapon_type,
                killer_x,
                killer_y,
                killer_z,
                victim_x,
                victim_y,
                victim_z,
                distance,
                killed_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, killerUuid.toString());
            statement.setString(2, killerName);
            statement.setString(3, victimUuid.toString());
            statement.setString(4, victimName);
            statement.setString(5, worldName);
            statement.setString(6, deathCause);
            statement.setString(7, weaponType);
            statement.setDouble(8, killerX);
            statement.setDouble(9, killerY);
            statement.setDouble(10, killerZ);
            statement.setDouble(11, victimX);
            statement.setDouble(12, victimY);
            statement.setDouble(13, victimZ);
            statement.setDouble(14, distance);
            statement.setLong(15, timestamp);
            statement.executeUpdate();
        }
    }
}
