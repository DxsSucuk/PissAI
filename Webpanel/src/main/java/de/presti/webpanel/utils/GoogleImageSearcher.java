package de.presti.webpanel.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ThreadLocalRandom;

public class GoogleImageSearcher {

    static HttpClient httpClient = HttpClient.newHttpClient();
    static String googleKey = "AIzaSyDtQBOPVnlcPlEuevIyW0nmF1PX3wb-Nqo", engineId = "7afbd8503f9d5c7d9";

    public static String searchForImage(String query) {
        try {
            switch (query.toLowerCase()) {
                case "tommy" -> query = "tommyinnit";
                case "sapnap" -> query = "sapnap";
                case "george" -> query = "georgenotfound";
                default -> query = "dreamyt";
            }
            HttpRequest httpRequest = HttpRequest.newBuilder().GET().header("Accept", "application/json")
                    .uri(new URI("https://www.googleapis.com/customsearch/v1?key=" + googleKey + "&cx=" + engineId + "&q=" + query
                            + "&searchType=image&start=" + ThreadLocalRandom.current().nextInt(100))).build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            String content = httpResponse.body();

            JsonElement jsonElement = JsonParser.parseString(content);
            if (jsonElement.isJsonObject()) {
                JsonObject rootObject = jsonElement.getAsJsonObject();
                if (rootObject.has("items") && rootObject.get("items").isJsonArray()) {
                    JsonArray jsonArray = rootObject.getAsJsonArray("items");

                    JsonElement jsonElement1 = jsonArray.get(ThreadLocalRandom.current().nextInt(jsonArray.size() - 1));
                    if (jsonElement1.isJsonObject()) {
                        JsonObject jsonObject = jsonElement1.getAsJsonObject();
                        return jsonObject.has("link") ? jsonObject.get("link").getAsString() : "";
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return "";
    }

}
