package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

@LambdaHandler(lambdaName = "sqs_handler",
		roleName = "sqs_handler-role",
		timeout = 20
)
@SqsTriggerEventSource(
		targetQueue = "async_queue",
		batchSize = 10
)
public class SQSHandler implements RequestHandler<SQSEvent, Void> {

	@Override
	public Void handleRequest(SQSEvent event, Context context) {
		LambdaLogger logger = context.getLogger();
		for (SQSEvent.SQSMessage msg : event.getRecords()){
			logger.log("New message: " + msg.getBody());
		}
		return null;
	}
}
