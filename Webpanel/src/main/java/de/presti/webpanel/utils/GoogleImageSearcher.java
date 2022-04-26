package de.presti.webpanel.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.presti.webpanel.WebpanelApplication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GoogleImageSearcher {

    static HttpClient httpClient = HttpClient.newHttpClient();
    static String googleKey = "AIzaSyDtQBOPVnlcPlEuevIyW0nmF1PX3wb-Nqo", engineId = "7afbd8503f9d5c7d9";

    static List<String> list = new ArrayList<>();
    static int currentIndex = 0;
    static int keyRetries = 0;

    public GoogleImageSearcher() {
        list.add("AIzaSyDbNx9Uxw8SYMzAA3QcvFZjGfRbexvZJnU");
        list.add("AIzaSyDtQBOPVnlcPlEuevIyW0nmF1PX3wb-Nqo");
        list.add("AIzaSyDe7G_t6UZXh_mwA6a8x3Q84yaaVOsUrko");
    }

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

            if (httpResponse.statusCode() == 429 && keyRetries <= 2) {
                System.out.println("Retry with new Keys!");
                selectNewKeyWithID();
                keyRetries++;
                return searchForImage(query);
            } else if (httpResponse.statusCode() != 200 && httpResponse.statusCode() != 429) {
                System.out.println(httpResponse.statusCode() + " - " + content);
                return "https://utils.ree6.de/pissai/googleIssue2.png";
            } else if (httpResponse.statusCode() == 429) {
                return "https://utils.ree6.de/pissai/googleIssue.png";
            }

            keyRetries = 0;
            JsonElement jsonElement = JsonParser.parseString(content);
            if (jsonElement.isJsonObject()) {
                JsonObject rootObject = jsonElement.getAsJsonObject();
                if (rootObject.has("items") && rootObject.get("items").isJsonArray()) {
                    JsonArray jsonArray = rootObject.getAsJsonArray("items");
                    String url = getRandomLink(jsonArray);

                    int retry = 0;

                    while (WebpanelApplication.getInstance().getSqlConnector().getSqlWorker().urlEntryExists(url) && retry <= 5) {
                        url = getRandomLink(jsonArray);
                        retry++;
                    }

                    return url;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return "";
    }

    private static void selectNewKeyWithID() {
        if (currentIndex >= list.size()) currentIndex = 0;
        String data = list.get(currentIndex);

        if (data.equalsIgnoreCase(googleKey)) {
            selectNewKeyWithID();
            return;
        }

        currentIndex++;
        googleKey = data;
    }

    private static String getRandomLink(JsonArray jsonArray) {
        JsonElement jsonElement1 = jsonArray.get(ThreadLocalRandom.current().nextInt(jsonArray.size() - 1));
        if (jsonElement1.isJsonObject()) {
            JsonObject jsonObject = jsonElement1.getAsJsonObject();
            return jsonObject.has("link") ? jsonObject.get("link").getAsString() : "";
        }

        return "";
    }

}
