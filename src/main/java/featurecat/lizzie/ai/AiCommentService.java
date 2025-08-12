package featurecat.lizzie.ai;

import featurecat.lizzie.Lizzie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class AiCommentService {

  public static class MoveContext {
    public final int moveNumber;
    public final String move;
    public final double winrateBefore;
    public final double winrateAfter;
    public final double winrateDelta;
    public final String position;

    public MoveContext(
        int moveNumber, String move, double winrateBefore, double winrateAfter, String position) {
      this.moveNumber = moveNumber;
      this.move = move;
      this.winrateBefore = winrateBefore;
      this.winrateAfter = winrateAfter;
      this.winrateDelta = Math.abs(winrateAfter - winrateBefore);
      this.position = position;
    }
  }

  private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
  private static final String MODEL = "gpt-4o-mini";

  /**
   * Generate an AI comment for a key move using OpenAI API
   *
   * @param context The move context containing move details and winrate information
   * @return AI-generated comment or null if generation fails
   */
  public static String generateComment(MoveContext context) {
    if (Lizzie.config.openAiApiKey == null || Lizzie.config.openAiApiKey.trim().isEmpty()) {
      if (Lizzie.config.debug) {
        System.err.println("AI Comment: No OpenAI API key configured");
      }
      return null;
    }

    try {
      String prompt = buildPrompt(context);
      String response = callOpenAiApi(prompt);

      if (response != null && !response.trim().isEmpty()) {
        // Add pacing between API calls
        Thread.sleep(150);
        return "AI: " + response.trim();
      }
    } catch (Exception e) {
      System.err.println("Failed to generate AI comment: " + e.getMessage());
      if (Lizzie.config.debug) {
        e.printStackTrace();
      }
    }

    return null;
  }

  private static String buildPrompt(MoveContext context) {
    String language = Lizzie.config.aiCommentsLanguage;
    String languageInstruction = getLanguageInstruction(language);

    double winrateChangePercent = context.winrateDelta * 100;
    String winrateDirection =
        context.winrateAfter > context.winrateBefore ? "increased" : "decreased";

    return String.format(
        "You are a Go/Weiqi/Baduk expert commentator. %s "
            + "Analyze this move and provide a concise 1-2 sentence commentary: "
            + "Move %d: %s. Winrate %s by %.1f%% (from %.1f%% to %.1f%%). "
            + "Focus on why this move was significant tactically or strategically. "
            + "Be specific and insightful, avoiding generic phrases.",
        languageInstruction,
        context.moveNumber,
        context.move,
        winrateDirection,
        winrateChangePercent,
        context.winrateBefore * 100,
        context.winrateAfter * 100);
  }

  private static String getLanguageInstruction(String language) {
    switch (language.toLowerCase()) {
      case "ru":
        return "Respond in Russian.";
      case "uk":
        return "Respond in Ukrainian.";
      case "zh":
      case "zh_cn":
        return "Respond in Simplified Chinese.";
      case "ja":
      case "ja_jp":
        return "Respond in Japanese.";
      default:
        return "Respond in English.";
    }
  }

  private static String callOpenAiApi(String prompt) throws IOException {
    URL url = new URL(OPENAI_API_URL);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("Authorization", "Bearer " + Lizzie.config.openAiApiKey);
    connection.setDoOutput(true);

    // Build request body
    JSONObject requestBody = new JSONObject();
    requestBody.put("model", MODEL);
    requestBody.put("max_tokens", 150);
    requestBody.put("temperature", 0.7);

    JSONArray messages = new JSONArray();
    JSONObject message = new JSONObject();
    message.put("role", "user");
    message.put("content", prompt);
    messages.put(message);
    requestBody.put("messages", messages);

    // Send request
    try (OutputStream os = connection.getOutputStream()) {
      byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
      os.write(input, 0, input.length);
    }

    // Read response
    int responseCode = connection.getResponseCode();
    if (responseCode != 200) {
      if (Lizzie.config.debug) {
        System.err.println("OpenAI API returned HTTP " + responseCode);
        try (BufferedReader br =
            new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
          String line;
          StringBuilder errorResponse = new StringBuilder();
          while ((line = br.readLine()) != null) {
            errorResponse.append(line);
          }
          System.err.println("Error response: " + errorResponse.toString());
        }
      }
      return null;
    }

    try (BufferedReader br =
        new BufferedReader(
            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        response.append(line);
      }

      // Parse response to extract the message content
      JSONObject responseObj = new JSONObject(response.toString());
      JSONArray choices = responseObj.getJSONArray("choices");
      if (choices.length() > 0) {
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject responseMessage = firstChoice.getJSONObject("message");
        return responseMessage.getString("content");
      }
    }

    return null;
  }
}
