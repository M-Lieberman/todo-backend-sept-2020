package com.techreturners;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
			LOG.info("RECEIVED a POST ");
		}

		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(response)
				.build();
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
