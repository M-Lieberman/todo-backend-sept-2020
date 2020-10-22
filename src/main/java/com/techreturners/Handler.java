package com.techreturners;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techreturners.model.Task;
import com.techreturners.service.TaskServiceImpl;
import com.techreturners.service.TasksService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(Handler.class);

	private TasksService taskService = null;

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input);

		// Firstly work out whether we are handling a GET for tasks or a POST
		String httpMethod = (String)input.get("httpMethod");
		taskService = new TaskServiceImpl();

		Object response = null;
		if(httpMethod.equalsIgnoreCase("GET")) {
			String userId = (String) ((Map)input.get("queryStringParameters")).get("userId");
			response = taskService.getTasks(userId);
		}
		else if(httpMethod.equalsIgnoreCase("POST")) {
			String postBody = (String) input.get("body");
			try {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> map = mapper.readValue(postBody, Map.class);
				String taskDescription = (String) map.get("taskDescription");
				boolean completed = (Boolean) map.get("completed");
				String userId = (String) map.get("userId");
				taskService.saveTask(userId, taskDescription, completed);
			}
			catch (IOException e) {
				LOG.error("Unable to unmarshal request body");
			}
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
}
