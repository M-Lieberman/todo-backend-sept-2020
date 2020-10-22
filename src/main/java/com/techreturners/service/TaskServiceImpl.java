package com.techreturners.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techreturners.model.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaskServiceImpl implements TasksService{

    private static final Logger LOG = LogManager.getLogger(TaskServiceImpl.class);
    private String DB_HOST = System.getenv("DB_HOST");
    private String DB_NAME = System.getenv("DB_NAME");
    private String DB_USER = System.getenv("DB_USER");
    private String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private Connection connection = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    @Override
    public List<Task> getTasks(String userId) {
        List<Task> tasks = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            preparedStatement = connection.prepareStatement("SELECT * FROM task WHERE userId = ?");
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String taskId = resultSet.getString("taskId");
                String description = resultSet.getString("description");
                boolean completed = resultSet.getBoolean("completed");
                tasks.add(new Task(taskId, description, completed));
            }
        }
        catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
        finally {
            close();
        }

        return tasks;
    }

    @Override
    public void saveTask(String userId, String taskDescription, boolean completed) {
        try {
            String taskId = UUID.randomUUID().toString();

            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

            preparedStatement = connection.prepareStatement("INSERT INTO task VALUES (?, ?, ?, ?)");
            preparedStatement.setString(1, taskId);
            preparedStatement.setString(2, taskDescription);
            preparedStatement.setBoolean(3, completed);
            preparedStatement.setString(4, userId);
            int rowsUpdated = preparedStatement.executeUpdate();

            LOG.info("Rows updated {}", rowsUpdated);

        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
        finally {
            close();
        }
    }

    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
        catch (Exception e) {
            LOG.error("Unable to close connections to MySQL - {}", e.getMessage());
        }
    }
}
