package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.my.meteo.OpenMeteoApi;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.io.IOException;


@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
		layers = {"api-sdk-layer"}
)
@LambdaLayer(
		layerName = "api-sdk-layer",
		libraries = {"library/open-meteo-api.jar"},
		runtime = DeploymentRuntime.JAVA8,
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<Object, String> {

	public String handleRequest(Object request, Context context) {
		LambdaLogger logger = context.getLogger();
		OpenMeteoApi publicApi = new OpenMeteoApi();
		logger.log("Requesting latest weather forecast");
		String response = null;
		try {
			response = publicApi.getLatestWeatherForecast();
		} catch (IOException e) {
			logger.log("An error happened: ");
			throw new RuntimeException(e);
		}
		logger.log("Response: " + response);
		return response;
	}
}
