package com.techreturners;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techreturners.model.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(Handler.class);

	private String DB_HOST = System.getenv("DB_HOST");
	private String DB_NAME = System.getenv("DB_NAME");
	private String DB_USER = System.getenv("DB_USER");
	private String DB_PASSWORD = System.getenv("DB_PASSWORD");

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input);

		// Firstly work out whether we are handling a GET for tasks or a POST
		String httpMethod = (String)input.get("httpMethod");

		Object response = null;
		if(httpMethod.equalsIgnoreCase("GET")) {
			String userId = (String) ((Map)input.get("queryStringParameters")).get("userId");
			response = getTasks(userId);
		}
		else if(httpMethod.equalsIgnoreCase("POST")) {
			String postBody = (String) input.get("body");
			saveTask(postBody);
		}

		// CORS from anywhere
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Headers", "Content-Type");

		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(response)
				.setHeaders(headers)
				.build();
	}

	private void saveTask(String taskInfo) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = null;
		try {
			map = mapper.readValue(taskInfo, Map.class);
			String taskDescription = (String) map.get("taskDescription");
			boolean completed = (Boolean) map.get("completed");
			String userId = (String) map.get("userId");
			String taskId = UUID.randomUUID().toString();

			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager
					.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO task VALUES (?, ?, ?, ?)");
			preparedStatement.setString(1, taskId);
			preparedStatement.setString(2, taskDescription);
			preparedStatement.setBoolean(3, completed);
			preparedStatement.setString(4, userId);
			int rowsUpdated = preparedStatement.executeUpdate();

			LOG.info("Rows updated {}", rowsUpdated);

		}
		catch (IOException | ClassNotFoundException | SQLException e) {
			LOG.error(e.getMessage());
		}
	}

	private List<Task> getTasks(String userId) {
		List<Task> tasks = new ArrayList<>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager
					.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", DB_HOST, DB_NAME, DB_USER, DB_PASSWORD));

			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM task WHERE userId = ?");
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

		return tasks;
	}
}
