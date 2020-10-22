package com.techreturners.service;

import com.techreturners.model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private Connection mockConnection;

    @Mock
    PreparedStatement mockPreparedStatement;

    @Mock
    ResultSet mockResultSet;

    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeAll
    public static void setUp() {
        Mockito.mockStatic(DriverManager.class);
    }

    @Test
    void testFetchTasksSuccessfullyReturnsList() throws Exception {
        Mockito.when(DriverManager.getConnection(Mockito.anyString())).thenReturn(mockConnection);
        Mockito.when(mockConnection.prepareStatement(Mockito.anyString())).thenReturn(mockPreparedStatement);
        Mockito.when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        Mockito.when(mockResultSet.getString("taskId")).thenReturn("ABC1234");

        List<Task> tasks = taskService.getTasks("user1234");
        assertEquals(1, tasks.size());
    }

    @Test
    void testFetchTasksSuccessfullyReturnsListWithCorrectData() throws Exception {
        Mockito.when(DriverManager.getConnection(Mockito.anyString())).thenReturn(mockConnection);
        Mockito.when(mockConnection.prepareStatement(Mockito.anyString())).thenReturn(mockPreparedStatement);
        Mockito.when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        Mockito.when(mockResultSet.getString("taskId")).thenReturn("ABC1234");

        List<Task> tasks = taskService.getTasks("user1234");
        assertEquals("ABC1234", tasks.get(0).getTaskId());
    }

    @Test
    void testFetchTasksSuccessfullyReturnsEmptyListWhenNoData() throws Exception {
        Mockito.when(DriverManager.getConnection(Mockito.anyString())).thenReturn(mockConnection);
        Mockito.when(mockConnection.prepareStatement(Mockito.anyString())).thenReturn(mockPreparedStatement);
        Mockito.when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(false);

        List<Task> tasks = taskService.getTasks("user1234");
        assertEquals(0, tasks.size());
    }
}
