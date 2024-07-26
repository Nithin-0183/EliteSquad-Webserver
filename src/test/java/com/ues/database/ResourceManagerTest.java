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
        Map<String, String> data = Map.of("name", "John", "age", "30");

        try (MockedStatic<DatabaseConfig> mockedStatic = mockStatic(DatabaseConfig.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);

            mockedStatic.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeUpdate()).thenReturn(1); // Simulate successful insert

            Mono<Boolean> result = ResourceManager.createData("users", data);

            StepVerifier.create(result)
                    .expectNext(true) // Expect success
                    .verifyComplete();
        }
    }

    @Test
    void createData_failure() throws SQLException {
        Map<String, String> data = Map.of("name", "John", "age", "30");

        try (MockedStatic<DatabaseConfig> mockedStatic = mockStatic(DatabaseConfig.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);

            mockedStatic.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeUpdate()).thenThrow(new SQLException("Database error")); // Simulate SQL error

            Mono<Boolean> result = ResourceManager.createData("users", data);

            StepVerifier.create(result)
                    .expectNext(false) // Expect failure
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
            when(statement.executeUpdate()).thenReturn(1); // Simulate successful update

            Mono<Boolean> result = ResourceManager.updateData("users", data, condition);

            StepVerifier.create(result)
                    .expectNext(true) // Expect success
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
            when(statement.executeUpdate()).thenThrow(new SQLException("Database error")); // Simulate SQL error

            Mono<Boolean> result = ResourceManager.updateData("users", data, condition);

            StepVerifier.create(result)
                    .expectNext(false) // Expect failure
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
            when(statement.executeUpdate()).thenReturn(1); // Simulate successful delete

            Mono<Boolean> result = ResourceManager.deleteData("users", condition);

            StepVerifier.create(result)
                    .expectNext(true) // Expect success
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
            when(statement.executeUpdate()).thenThrow(new SQLException("Database error")); // Simulate SQL error

            Mono<Boolean> result = ResourceManager.deleteData("users", condition);

            StepVerifier.create(result)
                    .expectNext(false) // Expect failure
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
            when(metaData.getColumnCount()).thenReturn(2); // Example column count
            when(metaData.getColumnName(1)).thenReturn("name");
            when(metaData.getColumnName(2)).thenReturn("age");
            when(resultSet.next()).thenReturn(true, false); // One row of data
            when(resultSet.getString(1)).thenReturn("John"); // First column value
            when(resultSet.getString(2)).thenReturn("30"); // Second column value

            Mono<List<Map<String, String>>> result = ResourceManager.getData("users", condition);

            List<Map<String, String>> expected = Collections.singletonList(
                Map.of("name", "John", "age", "30")
            );

            StepVerifier.create(result)
                    .expectNext(expected) // Expect the data list
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
            when(statement.executeQuery()).thenThrow(new SQLException("Database error")); // Simulate SQL error

            Mono<List<Map<String, String>>> result = ResourceManager.getData("users", condition);

            StepVerifier.create(result)
                    .expectNext(Collections.emptyList()) // Expect an empty list on failure
                    .verifyComplete();
        }
    }
}