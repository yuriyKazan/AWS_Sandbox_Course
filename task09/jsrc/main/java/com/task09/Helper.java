package com.task09;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class Helper {

    private static final String URL = "https://api.open-meteo.com/v1/forecast?latitude=50.4375&longitude=30.5&"
            + "current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";

    public static String getLatestWeatherForecast() throws IOException {
        URL url = new URL(URL);
        Scanner scanner = new Scanner((InputStream) url.getContent());
        StringBuilder temp = new StringBuilder();
        while (scanner.hasNext()){
            temp.append(scanner.nextLine());
        }
        return temp.toString();
    }

    public static Map<String, AttributeValue> convertToDynamoDBMap(Map<String, Object> map) {
        Map<String, AttributeValue> item = new HashMap<>();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof Map) {
                Map<String, Object> subMap = (Map<String, Object>) value;
                item.put(key, new AttributeValue().withM(convertToDynamoDBMap(subMap)));
            } else if (value instanceof List) {
                List<Object> subList = (List<Object>) value;
                item.put(key, new AttributeValue().withL(convertToDynamoDBList(subList)));
            } else if (value instanceof String) {
                item.put(key, new AttributeValue((String) value));
            } else if (value instanceof Number) {
                item.put(key, new AttributeValue().withN(value.toString()));
            }
        }
        return item;
    }

    private static List<AttributeValue> convertToDynamoDBList(List<Object> list) {
        List<AttributeValue> dynamoDBList = new ArrayList<>();
        for (Object value : list) {
            if (value instanceof Map) {
                Map<String, Object> subMap = (Map<String, Object>) value;
                dynamoDBList.add(new AttributeValue().withM(convertToDynamoDBMap(subMap)));
            } else if (value instanceof List) {
                List<Object> subList = (List<Object>) value;
                dynamoDBList.add(new AttributeValue().withL(convertToDynamoDBList(subList)));
            } else if (value instanceof String) {
                dynamoDBList.add(new AttributeValue((String) value));
            } else if (value instanceof Number) {
                dynamoDBList.add(new AttributeValue().withN(value.toString()));
            }
        }
        return dynamoDBList;
    }

}
