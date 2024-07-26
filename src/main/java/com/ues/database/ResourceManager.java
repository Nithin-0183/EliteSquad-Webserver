package com.ues.database;

import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ResourceManager {

    public ResourceManager() {
    }

    public static Mono<Boolean> createData(String tableName, Map<String, String> data) {
        return Mono.fromCallable(() -> {
            StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
            StringBuilder placeholders = new StringBuilder("VALUES (");

            for (String key : data.keySet()) {
                sql.append(key).append(",");
                placeholders.append("?,");
            }

            sql.setLength(sql.length() - 1); // Remove last comma
            placeholders.setLength(placeholders.length() - 1); // Remove last comma

            sql.append(") ").append(placeholders).append(")");

            try (Connection connection = DatabaseConfig.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql.toString())) {

                int index = 1;
                for (String value : data.values()) {
                    statement.setString(index++, value);
                }

                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static Mono<Boolean> updateData(String tableName, Map<String, String> data, String condition) {
        return Mono.fromCallable(() -> {
            StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

            for (String key : data.keySet()) {
                sql.append(key).append(" = ?,");
            }

            sql.setLength(sql.length() - 1); // Remove last comma
            sql.append(" WHERE ").append(condition);

            try (Connection connection = DatabaseConfig.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql.toString())) {

                int index = 1;
                for (String value : data.values()) {
                    statement.setString(index++, value);
                }

                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static Mono<Boolean> deleteData(String tableName, String condition) {
        return Mono.fromCallable(() -> {
            String sql = "DELETE FROM " + tableName + " WHERE " + condition;

            try (Connection connection = DatabaseConfig.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static Mono<List<Map<String, String>>> getData(String tableName, String condition) {
        return Mono.fromCallable(() -> {
            List<Map<String, String>> result = new ArrayList<>();
            String sql = "SELECT * FROM " + tableName + " WHERE " + condition;

            try (Connection connection = DatabaseConfig.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                int columnCount = resultSet.getMetaData().getColumnCount();
                while (resultSet.next()) {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(resultSet.getMetaData().getColumnName(i), resultSet.getString(i));
                    }
                    result.add(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return result;
        });
    }
}
