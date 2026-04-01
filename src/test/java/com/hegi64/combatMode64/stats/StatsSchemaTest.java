package com.hegi64.combatMode64.stats;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsSchemaTest {

    @Test
    void schemaCreatesTablesAndSupportsDirectionalKillCounters() throws Exception {
        Path dbFile = Files.createTempFile("combatmode64-stats", ".db");

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile)) {
            applySchema(connection);

            assertTrue(tableExists(connection, "player_stats"));
            assertTrue(tableExists(connection, "player_kill_pairs"));
            assertTrue(tableExists(connection, "kill_events"));

            String killer = "11111111-1111-1111-1111-111111111111";
            String victim = "22222222-2222-2222-2222-222222222222";

            applyKillEvent(connection, killer, "Killer", victim, "Victim", "world", "ENTITY_ATTACK", "DIAMOND_SWORD", 10.0, 64.0, -2.0, 1.0, 64.0, -2.0, 9.0, 1000L);
            applyKillEvent(connection, killer, "Killer", victim, "Victim", "world", "ENTITY_ATTACK", "BOW", 12.0, 64.0, -2.0, 2.0, 65.0, -3.0, 10.05, 1001L);
            applyKillEvent(connection, victim, "Victim", killer, "Killer", "world_nether", "PROJECTILE", "CROSSBOW", 3.0, 70.0, -4.0, 3.0, 66.0, -4.0, 4.0, 1002L);

            assertEquals(2, getKills(connection, killer));
            assertEquals(1, getDeaths(connection, killer));
            assertEquals(1, getKills(connection, victim));
            assertEquals(2, getDeaths(connection, victim));

            assertEquals(2, getPairKillCount(connection, killer, victim));
            assertEquals(1, getPairKillCount(connection, victim, killer));
            assertEquals(3, getKillEventsCount(connection));
            assertEquals("world_nether", getLastKillWorld(connection));
            assertEquals("CROSSBOW", getLastKillWeapon(connection));
        } finally {
            Files.deleteIfExists(dbFile);
        }
    }

    private void applySchema(Connection connection) throws IOException, SQLException {
        String schema = new String(
            Objects.requireNonNull(
                StatsSchemaTest.class.getClassLoader().getResourceAsStream("sql/stats/schema.sql"),
                "Missing sql/stats/schema.sql test resource"
            ).readAllBytes(),
            StandardCharsets.UTF_8
        );

        for (String statementSql : schema.split(";")) {
            String sql = statementSql.trim();
            if (sql.isEmpty()) {
                continue;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.execute();
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

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = ?"
        )) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) == 1;
            }
        }
    }

    private void applyKillEvent(
        Connection connection,
        String killerUuid,
        String killerName,
        String victimUuid,
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
        long now
    ) throws SQLException {
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO player_stats (player_uuid, player_name, kills, deaths, updated_at)
                VALUES (?, ?, 1, 0, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET
                    player_name = excluded.player_name,
                    kills = player_stats.kills + 1,
                    updated_at = excluded.updated_at
                """)) {
                statement.setString(1, killerUuid);
                statement.setString(2, killerName);
                statement.setLong(3, now);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO player_stats (player_uuid, player_name, kills, deaths, updated_at)
                VALUES (?, ?, 0, 1, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET
                    player_name = excluded.player_name,
                    deaths = player_stats.deaths + 1,
                    updated_at = excluded.updated_at
                """)) {
                statement.setString(1, victimUuid);
                statement.setString(2, victimName);
                statement.setLong(3, now);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO player_kill_pairs (killer_uuid, victim_uuid, kill_count, updated_at)
                VALUES (?, ?, 1, ?)
                ON CONFLICT(killer_uuid, victim_uuid) DO UPDATE SET
                    kill_count = player_kill_pairs.kill_count + 1,
                    updated_at = excluded.updated_at
                """)) {
                statement.setString(1, killerUuid);
                statement.setString(2, victimUuid);
                statement.setLong(3, now);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement("""
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
                """)) {
                statement.setString(1, killerUuid);
                statement.setString(2, killerName);
                statement.setString(3, victimUuid);
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
                statement.setLong(15, now);
                statement.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private int getKills(Connection connection, String playerUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT kills FROM player_stats WHERE player_uuid = ?")) {
            statement.setString(1, playerUuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    private int getDeaths(Connection connection, String playerUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT deaths FROM player_stats WHERE player_uuid = ?")) {
            statement.setString(1, playerUuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    private int getPairKillCount(Connection connection, String killerUuid, String victimUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT kill_count
            FROM player_kill_pairs
            WHERE killer_uuid = ? AND victim_uuid = ?
            """)) {
            statement.setString(1, killerUuid);
            statement.setString(2, victimUuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    private int getKillEventsCount(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM kill_events");
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private String getLastKillWorld(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT world_name
            FROM kill_events
            ORDER BY id DESC
            LIMIT 1
            """);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getString(1) : "";
        }
    }

    private String getLastKillWeapon(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT weapon_type
            FROM kill_events
            ORDER BY id DESC
            LIMIT 1
            """);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getString(1) : "";
        }
    }
}

