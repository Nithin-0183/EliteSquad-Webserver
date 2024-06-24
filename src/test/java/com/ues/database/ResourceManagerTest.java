package com.ues.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ResourceManagerTest {

    private ResourceManager resourceManager;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private MockedStatic<DatabaseConfig> mockedStaticDatabaseConfig;

    @BeforeEach
    void setUp() throws SQLException {
        resourceManager = new ResourceManager();

        connection = Mockito.mock(Connection.class);
        preparedStatement = Mockito.mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);

        mockedStaticDatabaseConfig = Mockito.mockStatic(DatabaseConfig.class);
        mockedStaticDatabaseConfig.when(DatabaseConfig::getConnection).thenReturn(connection);
    }

    @AfterEach
    void tearDown() {
        mockedStaticDatabaseConfig.close();
    }

    @Test
    void createMessage_shouldReturnTrue() throws SQLException {
        Map<String, String> data = new HashMap<>();
        data.put("user", "testUser");
        data.put("message", "testMessage");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Mono<Boolean> result = resourceManager.createMessage(data);

        StepVerifier.create(result)
                .expectNext(true)
                .expectComplete()
                .verify();

        verify(preparedStatement).setString(1, "testUser");
        verify(preparedStatement).setString(2, "testMessage");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void updateMessage_shouldReturnTrue() throws SQLException {
        Map<String, String> data = new HashMap<>();
        data.put("message", "updatedMessage");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Mono<Boolean> result = resourceManager.updateMessage("1", data);

        StepVerifier.create(result)
                .expectNext(true)
                .expectComplete()
                .verify();

        verify(preparedStatement).setString(1, "updatedMessage");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteMessage_shouldReturnTrue() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Mono<Boolean> result = resourceManager.deleteMessage("1");

        StepVerifier.create(result)
                .expectNext(true)
                .expectComplete()
                .verify();

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void getMessages_shouldReturnListOfMessages() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("user")).thenReturn("testUser");
        when(resultSet.getString("message")).thenReturn("testMessage");
        when(resultSet.getString("timestamp")).thenReturn("2024-06-22T12:00:00");

        Mono<List<Map<String, String>>> result = resourceManager.getMessages();

        StepVerifier.create(result)
                .expectNextMatches(messages -> 
                    messages.size() == 1 &&
                    messages.get(0).get("id").equals("1") &&
                    messages.get(0).get("user").equals("testUser") &&
                    messages.get(0).get("message").equals("testMessage") &&
                    messages.get(0).get("timestamp").equals("2024-06-22T12:00:00")
                )
                .expectComplete()
                .verify();

        verify(preparedStatement).executeQuery();
        verify(resultSet, times(2)).next();
        verify(resultSet).getInt("id");
        verify(resultSet).getString("user");
        verify(resultSet).getString("message");
        verify(resultSet).getString("timestamp");
    }
}
