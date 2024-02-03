package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;

@LambdaHandler(lambdaName = "uuid_generator",
	roleName = "uuid_generator-role"
)
@RuleEventSource(
		targetRule = "uuid_trigger"
)
public class UuidGenerator implements RequestHandler<Object, Void> {

	private static final String PREFIX = "cmtr-24bd7ee1-";
	private static final String SUFFIX = "-test";
	private static final String BUCKET = PREFIX + "uuid-storage" + SUFFIX;
	private static final int NUMBER_OF_UUID = 10;
	private static final String SUCCESS_MESSAGE = NUMBER_OF_UUID + " UUID were stored in S3 bucket: " + BUCKET;
	private static final String IDS = "ids";

	public Void handleRequest(Object request, Context context) {
		LambdaLogger logger = context.getLogger();
		logger.log("Generating 10 UUID");
		List<String> uuids = new ArrayList<>();
		for (int i = 0; i < NUMBER_OF_UUID; i++) {
			uuids.add(UUID.randomUUID().toString());
		}

		Map<String, Object> data = new HashMap<>();
		data.put(IDS, uuids);
		ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String resul = "";
		try {
			resul = objectWriter.writeValueAsString(data);
		} catch (JsonProcessingException exception) {
			logger.log("Exception: " + exception.getMessage());
		}
		logger.log("Prepared 10 UUID: " + resul);
		try {
			AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
			InputStream stream = new StringInputStream(resul);
			ObjectMetadata meta = new ObjectMetadata();
			s3Client.putObject(BUCKET, Instant.now().toString(), stream, meta);
		} catch (Exception exception) {
			logger.log("Unable to store " + resul + " into S3 bucket: " + BUCKET + "\n"
					+ exception.getMessage());
		}
		logger.log(SUCCESS_MESSAGE);
		return null;
	}
}
