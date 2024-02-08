package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.task09.Helper.convertToDynamoDBMap;
import static com.task09.Helper.getLatestWeatherForecast;

@LambdaHandler(
		lambdaName = "processor",
		roleName = "processor-role",
		tracingMode = TracingMode.Active
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class Processor implements RequestHandler<Object, String> {


	private static final String PREFIX = "cmtr-24bd7ee1-";
	private static final String SUFFIX = "-test";
	private static final String TABLE_NAME = PREFIX + "Weather" + SUFFIX;

	public String handleRequest(Object request, Context context) {
		LambdaLogger logger = context.getLogger();
		try {
			String response = getLatestWeatherForecast();
			logger.log("Latest weather forecast: " + response);

			Gson gson = new Gson();
			Type type = new TypeToken<Map<String, Object>>() {}.getType();
			Map<String, Object> inputMap = gson.fromJson(response, type);

			logger.log("Creating DynamoDB item ");
			Map<String, AttributeValue> item = new HashMap<>();
			item.put("id", new AttributeValue(UUID.randomUUID().toString()));
			item.put("forecast", new AttributeValue().withM(convertToDynamoDBMap(inputMap)));
			logger.log("The item created: " + item);

			AmazonDynamoDB dynamoDb = AmazonDynamoDBClientBuilder.defaultClient();
			dynamoDb.putItem(TABLE_NAME, item);

			return "The item successfully saved";
		} catch (IOException e) {
			logger.log("An error happened: " + e.getMessage());
			return "The item was not saved";
		}
	}
}
