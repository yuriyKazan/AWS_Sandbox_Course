package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.syndicate.deployment.annotations.events.SnsEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

@LambdaHandler(lambdaName = "sns_handler",
	roleName = "sns_handler-role"
)
@SnsEventSource(
		targetTopic = "lambda_topic"
)
public class SnsHandler implements RequestHandler<SNSEvent, Void> {
	@Override
	public Void handleRequest(SNSEvent event, Context context) {
		LambdaLogger logger = context.getLogger();
		for (SNSEvent.SNSRecord record : event.getRecords()) {
			SNSEvent.SNS sns = record.getSNS();
			logger.log("New message: " + sns.getMessage());
		}
		return null;
	}
}
