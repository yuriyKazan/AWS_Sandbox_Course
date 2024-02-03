package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@LambdaHandler(lambdaName = "audit_producer",
	    roleName = "audit_producer-role",
		timeout = 20
)
@DynamoDbTriggerEventSource(
		targetTable = "Configuration",
		batchSize = 10
)
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

	private static final String PREFIX = "cmtr-24bd7ee1-";
	private static final String SUFFIX = "-test";
	private static final String TABLE_NAME = PREFIX + "Audit" + SUFFIX;
	private static final String INSERT_EVENT = "INSERT";
	private static final String MODIFY_EVENT = "MODIFY";
	private static final String ID = "id";
	private static final String ITEM_KEY = "itemKey";
	private static final String MODIFICATION_TIME = "modificationTime";
	private static final String NEW_VALUE = "newValue";
	private static final String OLD_VALUE = "oldValue";
	private static final String UPDATED_ATTRIBUTE = "updatedAttribute";
	private static final String KEY = "key";
	private static final String VALUE = "value";


	public Void handleRequest(DynamodbEvent ddbEvent, Context context) {
		LambdaLogger logger = context.getLogger();
		for (DynamodbEvent.DynamodbStreamRecord record : ddbEvent.getRecords()) {
			logger.log("Processing Dynamodb records");
			if (Objects.isNull(record)) {
				continue;
			}
			String eventType = record.getEventName();
			if (INSERT_EVENT.equals(eventType) || MODIFY_EVENT.equals(eventType)) {
				logger.log("Was found Dynamodb record with event type: " + eventType);
				Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
				Map<String, AttributeValue> oldImage = record.getDynamodb().getOldImage();

				logger.log("Creating basic record request for Dynamodb table: " + TABLE_NAME);
				PutItemRequest putItemRequest = new PutItemRequest();
				putItemRequest.withTableName(TABLE_NAME)
						.addItemEntry(ID, new AttributeValue().withS(UUID.randomUUID().toString()))
						.addItemEntry(ITEM_KEY, new AttributeValue().withS(newImage.get(KEY).getS()))
						.addItemEntry(MODIFICATION_TIME, new AttributeValue().withS(Instant.now().toString()));

				if (eventType.equals(INSERT_EVENT)) {
					Map<String, AttributeValue> newValueMap = newImage.entrySet().stream()
							.collect(Collectors.toMap(Map.Entry::getKey, entry -> new AttributeValue(entry.getValue()
									.getS())));

					putItemRequest.addItemEntry(NEW_VALUE, new AttributeValue().withM(newValueMap));
				} else {
					putItemRequest.addItemEntry(OLD_VALUE, oldImage.get(VALUE));
					putItemRequest.addItemEntry(NEW_VALUE, newImage.get(VALUE));
					putItemRequest.addItemEntry(UPDATED_ATTRIBUTE, new AttributeValue().withS(VALUE));
				}
				AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
				logger.log("Putting record into Dynamodb table: " + putItemRequest);
				ddb.putItem(putItemRequest);
			}
		}
		return null;
	}
}
