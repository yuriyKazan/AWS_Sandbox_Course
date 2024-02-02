package com.task05;

import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

import java.time.Instant;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role"
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final String TABLE_NAME = "Events";
	private static final int CREATED_CODE = 201;
	private static final int INTERNAL_SERVER_CODE = 500;
	private static final String ID = "id";
	private static final String PRINCIPAL_ID = "principalId";
	private static final String BODY = "body";
	private static final String CREATED_AT = "createdAt";

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		LambdaLogger logger = context.getLogger();
		try {
			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
			DynamoDB dynamoDB = new DynamoDB(client);
			Table table = null;
			try {
				table = dynamoDB.getTable(TABLE_NAME);
			} catch (ResourceNotFoundException e) {
				logger.log("Exception: " + e.getMessage());
			}

			ObjectMapper objectMapper = new ObjectMapper();
			Request eventRequest = objectMapper.readValue(request.getBody(), Request.class);

			String id = UUID.randomUUID().toString();
			String createAt = Instant.now().toString();

			Item item = new Item()
					.withPrimaryKey(ID, id)
					.withNumber(PRINCIPAL_ID, eventRequest.getPrincipalId())
					.withString(CREATED_AT, createAt)
					.withMap(BODY, eventRequest.getContent());
			assert table != null;
			PutItemOutcome outcome = table.putItem(item);

			return new APIGatewayProxyResponseEvent()
					.withStatusCode(CREATED_CODE)
					.withBody(objectMapper.writeValueAsString(outcome));
		} catch (Exception e) {
			logger.log("Exception: " + e.getMessage());
			return new APIGatewayProxyResponseEvent()
					.withStatusCode(INTERNAL_SERVER_CODE)
					.withBody("An error occurred: " + e.getMessage());
		}
	}
}
