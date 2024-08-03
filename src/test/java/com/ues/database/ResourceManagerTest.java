package com.ues.database;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.sql.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ResourceManagerTest {

    @Test
    void createData_success() throws SQLException {
        Map<String, String> data = Map.of("name", "John", "email", "admin@ucd.ie");

        try (MockedStatic<DatabaseConfig> mockedStatic = mockStatic(DatabaseConfig.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            DatabaseMetaData metaData = mock(DatabaseMetaData.class);
            ResultSet resultSet = mock(ResultSet.class);

            mockedStatic.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(metaData);
            when(metaData.getTables(null, null, "USERS", null)).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeUpdate()).thenReturn(1);

            Mono<Boolean> result = ResourceManager.createData("users", data);

            StepVerifier.create(result)
                    .expectNext(true)
                    .verifyComplete();

            verify(statement, times(1)).execute();
            verify(statement, times(1)).executeUpdate();
        }
    }

    @Test
    void createData_failure() throws SQLException {
        Map<String, String> data = Map.of("name", "John", "email", "admin@ucd.ie");

        try (MockedStatic<DatabaseConfig> mockedStatic = mockStatic(DatabaseConfig.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            DatabaseMetaData metaData = mock(DatabaseMetaData.class);
            ResultSet resultSet = mock(ResultSet.class);

            mockedStatic.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(metaData);
            when(metaData.getTables(null, null, "USERS", null)).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true); // Simulate table exists
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));

            Mono<Boolean> result = ResourceManager.createData("users", data);

            StepVerifier.create(result)
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    @Test
    void updateData_success() throws SQLException {
        Map<String, String> data = Map.of("name", "John", "age", "30");
        String condition = "id = 1";

        try (MockedStatic<DatabaseConfig> mockedStatic = mockStatic(DatabaseConfig.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);

            mockedStatic.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeUpdate()).thenReturn(1);
            Mono<Boolean> result = ResourceManager.updateData("users", data, condition);

            StepVerifier.create(result)
                    .expectNext(true)
                    .verifyComplete();
        }
    }

    @Test
    void updateData_failure() throws SQLException {
        Map<String, String> data = Map.of("name", "John", "age", "30");
        String condition = "id = 1";

        try (MockedStatic<DatabaseConfig> mockedStatic = mockStatic(DatabaseConfig.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);

            mockedStatic.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));

            Mono<Boolean> result = ResourceManager.updateData("users", data, condition);

            StepVerifier.create(result)
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    @Test
    void deleteData_success() throws SQLException {
        String condition = "id = 1";

        try (MockedStatic<DatabaseConfig> mockedStatic = mockStatic(DatabaseConfig.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);

            mockedStatic.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeUpdate()).thenReturn(1);

            Mono<Boolean> result = ResourceManager.deleteData("users", condition);

            StepVerifier.create(result)
                    .expectNext(true)
                    .verifyComplete();
        }
    }

    @Test
    void deleteData_failure() throws SQLException {
        String condition = "id = 1";

        try (MockedStatic<DatabaseConfig> mockedStatic = mockStatic(DatabaseConfig.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);

            mockedStatic.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));

            Mono<Boolean> result = ResourceManager.deleteData("users", condition);

            StepVerifier.create(result)
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    @Test
    void getData_success() throws SQLException {
        String condition = "id = 1";

        try (MockedStatic<DatabaseConfig> mockedStatic = mockStatic(DatabaseConfig.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            ResultSet resultSet = mock(ResultSet.class);
            ResultSetMetaData metaData = mock(ResultSetMetaData.class);

            mockedStatic.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);

            // Set up the ResultSetMetaData and ResultSet mocks
            when(resultSet.getMetaData()).thenReturn(metaData);
            when(metaData.getColumnCount()).thenReturn(2);
            when(metaData.getColumnName(1)).thenReturn("name");
            when(metaData.getColumnName(2)).thenReturn("age");
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString(1)).thenReturn("John");
            when(resultSet.getString(2)).thenReturn("30");

            Mono<List<Map<String, String>>> result = ResourceManager.getData("users", condition);

            List<Map<String, String>> expected = Collections.singletonList(
                Map.of("name", "John", "age", "30")
            );

            StepVerifier.create(result)
                    .expectNext(expected)
                    .verifyComplete();
        }
    }

    @Test
    void getData_failure() throws SQLException {
        String condition = "id = 1";

        try (MockedStatic<DatabaseConfig> mockedStatic = mockStatic(DatabaseConfig.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);

            mockedStatic.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenThrow(new SQLException("Database error"));

            Mono<List<Map<String, String>>> result = ResourceManager.getData("users", condition);

            StepVerifier.create(result)
                    .expectNext(Collections.emptyList())
                    .verifyComplete();
        }
    }
}