package com.ues.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;

public class ResourceManager {

    public static Mono<Boolean> createMessage(Map<String, String> data) {
        return Mono.fromCallable(() -> {
            String sql = "INSERT INTO Messages (user, message) VALUES (?, ?)";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, data.get("user"));
                stmt.setString(2, data.get("message"));
                int result = stmt.executeUpdate();
                return result > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }).defaultIfEmpty(false);
    }

    public static Mono<Boolean> updateMessage(String id, Map<String, String> data) {
        return Mono.fromCallable(() -> {
            String sql = "UPDATE Messages SET message = ? WHERE id = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, data.get("message"));
                stmt.setInt(2, Integer.parseInt(id));
                int result = stmt.executeUpdate();
                return result > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }).defaultIfEmpty(false);
    }

    public static Mono<Boolean> deleteMessage(String id) {
        return Mono.fromCallable(() -> {
            String sql = "DELETE FROM Messages WHERE id = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, Integer.parseInt(id));
                int result = stmt.executeUpdate();
                return result > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }).defaultIfEmpty(false);
    }

    public static Mono<List<Map<String, String>>> getMessages() {
        return Mono.fromCallable(() -> {
            String sql = "SELECT * FROM Messages ORDER BY timestamp DESC";
            List<Map<String, String>> messages = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(Map.of(
                        "id", String.valueOf(rs.getInt("id")),
                        "user", rs.getString("user"),
                        "message", rs.getString("message"),
                        "timestamp", rs.getString("timestamp")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return messages;
        }).defaultIfEmpty(new ArrayList<>());
    }
}
