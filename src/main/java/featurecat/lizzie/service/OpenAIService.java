package featurecat.lizzie.service;

import featurecat.lizzie.util.AjaxHttpRequest;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

/** Service for communicating with OpenAI API to generate AI comments for Go moves. */
public class OpenAIService {
  private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
  private static final String MODEL = "gpt-3.5-turbo";

  private String apiKey;

  public OpenAIService(String apiKey) {
    this.apiKey = apiKey;
  }

  /**
   * Generate an AI comment for a key move in Go.
   *
   * @param moveDescription Description of the move (e.g., "черные B4")
   * @param scoreDelta The change in score mean (positive means better for the current player)
   * @return AI generated comment explaining the significance of the move
   * @throws IOException if API request fails
   */
  public String generateMoveComment(String moveDescription, double scoreDelta) throws IOException {
    if (apiKey == null || apiKey.trim().isEmpty()) {
      throw new IOException("OpenAI API key not configured");
    }

    String prompt =
        String.format(
            "В партии Го после хода %s преимущество изменилось на %.1f очков. Объясни, почему это важно и какой стратегический смысл у такого изменения.",
            moveDescription, Math.abs(scoreDelta));

    JSONObject requestBody = new JSONObject();
    requestBody.put("model", MODEL);
    requestBody.put("max_tokens", 150);
    requestBody.put("temperature", 0.7);

    JSONArray messages = new JSONArray();
    JSONObject systemMessage = new JSONObject();
    systemMessage.put("role", "system");
    systemMessage.put(
        "content",
        "Ты эксперт по игре Го. Объясняй значимость ходов кратко и понятно, сосредотачиваясь на стратегических аспектах.");
    messages.put(systemMessage);

    JSONObject userMessage = new JSONObject();
    userMessage.put("role", "user");
    userMessage.put("content", prompt);
    messages.put(userMessage);

    requestBody.put("messages", messages);

    AjaxHttpRequest request = new AjaxHttpRequest();
    request.setRequestHeader("Authorization", "Bearer " + apiKey);
    request.setRequestHeader("Content-Type", "application/json");

    try {
      request.open("POST", OPENAI_API_URL, false);
      request.send(requestBody.toString());

      int status = request.getReadyState();
      String responseText = request.getResponseText();

      if (status != AjaxHttpRequest.STATE_COMPLETE) {
        throw new IOException("Request failed with status: " + status);
      }

      JSONObject response = new JSONObject(responseText);

      if (response.has("error")) {
        JSONObject error = response.getJSONObject("error");
        throw new IOException("OpenAI API error: " + error.optString("message", "Unknown error"));
      }

      if (response.has("choices") && response.getJSONArray("choices").length() > 0) {
        JSONObject choice = response.getJSONArray("choices").getJSONObject(0);
        if (choice.has("message")) {
          JSONObject message = choice.getJSONObject("message");
          return message.optString("content", "").trim();
        }
      }

      throw new IOException("Invalid response format from OpenAI API");

    } catch (Exception e) {
      throw new IOException("Failed to get AI comment: " + e.getMessage(), e);
    }
  }

  /**
   * Test if the API key is valid by making a simple request.
   *
   * @return true if API key is valid, false otherwise
   */
  public boolean testApiKey() {
    try {
      generateMoveComment("тестовый ход", 1.0);
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
