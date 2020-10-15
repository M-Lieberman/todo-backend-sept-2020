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

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input);

		String userId = (String) ((Map)input.get("queryStringParameters")).get("userId");
		LOG.info("UserId: {}", userId);

		List<Task> tasks = new ArrayList<>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager
					.getConnection("jdbc:mysql://localhost/tasks?"
							+ "user=user&password=password");

			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM task WHERE userId = ?");
			preparedStatement.setString(1, userId);
			ResultSet resultSet = preparedStatement.executeQuery();


			while (resultSet.next()) {
				String taskId = resultSet.getString("taskId");
				String description = resultSet.getString("description");
				boolean completed = resultSet.getBoolean("completed");
				LOG.info("Task: {} - {}", taskId, description);
				tasks.add(new Task(taskId, description, completed));
			}
		}
		catch (ClassNotFoundException | SQLException e) {
			LOG.error(e.getMessage());
		}

		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(tasks)
				.build();
	}
}
