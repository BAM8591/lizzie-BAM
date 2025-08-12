package featurecat.lizzie.ai;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.analysis.MoveData;
import featurecat.lizzie.rules.Stone;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class AiCommentService {
  private static AiCommentService instance;
  private final HttpClient httpClient;

  private AiCommentService() {
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
  }

  public static synchronized AiCommentService getInstance() {
    if (instance == null) {
      instance = new AiCommentService();
    }
    return instance;
  }

  public boolean isEnabled() {
    return Lizzie.config.enableAiKeyComment && !Lizzie.config.openAiApiKey.isEmpty();
  }

  public static class MoveContext {
    public final int moveNumber;
    public final Stone stone;
    public final int[] coordinates;
    public final double winrateBefore;
    public final double winrateAfter;
    public final double winrateSwing;
    public final int playouts;
    public final List<MoveData> bestMoves;

    public MoveContext(
        int moveNumber,
        Stone stone,
        int[] coordinates,
        double winrateBefore,
        double winrateAfter,
        int playouts,
        List<MoveData> bestMoves) {
      this.moveNumber = moveNumber;
      this.stone = stone;
      this.coordinates = coordinates;
      this.winrateBefore = winrateBefore;
      this.winrateAfter = winrateAfter;
      this.winrateSwing = Math.abs(winrateAfter - winrateBefore);
      this.playouts = playouts;
      this.bestMoves = bestMoves;
    }
  }

  public String buildPrompt(MoveContext context) {
    String language = Lizzie.config.aiCommentsLanguage;
    String moveCoord = coordinatesToString(context.coordinates);
    String colorName = context.stone == Stone.BLACK ? "Black" : "White";

    StringBuilder prompt = new StringBuilder();
    prompt.append("You are a Go/Baduk/Weiqi expert analyzing a key move in a game. ");

    if ("ru".equals(language)) {
      prompt.append("Проанализируйте этот ключевой ход в игре го. ");
      prompt.append(
          String.format(
              "Ход %d: %s %s. ",
              context.moveNumber, context.stone == Stone.BLACK ? "Черные" : "Белые", moveCoord));
      prompt.append(
          String.format(
              "Винрейт изменился с %.1f%% до %.1f%% (изменение: %.1f%%). ",
              context.winrateBefore, context.winrateAfter, context.winrateSwing));
      prompt.append("Объясните значение этого хода в 2-3 предложениях. Почему этот ход важен?");
    } else if ("zh".equals(language)) {
      prompt.append("分析这个围棋关键手。");
      prompt.append(
          String.format(
              "第%d手：%s %s。",
              context.moveNumber, context.stone == Stone.BLACK ? "黑棋" : "白棋", moveCoord));
      prompt.append(
          String.format(
              "胜率从%.1f%%变为%.1f%%(变化：%.1f%%)。",
              context.winrateBefore, context.winrateAfter, context.winrateSwing));
      prompt.append("用2-3句话说明这步棋的重要性。为什么这步棋关键？");
    } else if ("ja".equals(language)) {
      prompt.append("この囲碁の重要手を分析してください。");
      prompt.append(
          String.format(
              "第%d手：%s %s。",
              context.moveNumber, context.stone == Stone.BLACK ? "黒" : "白", moveCoord));
      prompt.append(
          String.format(
              "勝率が%.1f%%から%.1f%%に変化（変化幅：%.1f%%）。",
              context.winrateBefore, context.winrateAfter, context.winrateSwing));
      prompt.append("この手の意義を2-3文で説明してください。なぜこの手が重要ですか？");
    } else {
      // Default English
      prompt.append(
          String.format("Move %d: %s plays %s. ", context.moveNumber, colorName, moveCoord));
      prompt.append(
          String.format(
              "Winrate changed from %.1f%% to %.1f%% (swing: %.1f%%). ",
              context.winrateBefore, context.winrateAfter, context.winrateSwing));
      prompt.append(
          "Explain the significance of this move in 2-3 sentences. Why is this move important?");
    }

    return prompt.toString();
  }

  public String requestComment(MoveContext context) {
    if (!isEnabled()) {
      return null;
    }

    try {
      String prompt = buildPrompt(context);

      // Log prompt for debugging purposes without API key
      System.err.println("Requesting AI comment for move " + context.moveNumber);

      JSONObject requestBody = new JSONObject();
      requestBody.put("model", "gpt-4o-mini");
      requestBody.put("max_tokens", 150);
      requestBody.put("temperature", 0.7);

      JSONArray messages = new JSONArray();
      JSONObject message = new JSONObject();
      message.put("role", "user");
      message.put("content", prompt);
      messages.put(message);
      requestBody.put("messages", messages);

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create("https://api.openai.com/v1/chat/completions"))
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer " + Lizzie.config.openAiApiKey)
              .timeout(Duration.ofSeconds(30))
              .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        JSONObject responseJson = new JSONObject(response.body());
        JSONArray choices = responseJson.getJSONArray("choices");
        if (choices.length() > 0) {
          JSONObject choice = choices.getJSONObject(0);
          JSONObject responseMessage = choice.getJSONObject("message");
          String content = responseMessage.getString("content").trim();
          return "AI: " + content;
        }
      } else {
        System.err.println("OpenAI API error: " + response.statusCode() + " " + response.body());
      }
    } catch (IOException | InterruptedException e) {
      System.err.println("AI comment request failed: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("AI comment processing error: " + e.getMessage());
    }

    return null;
  }

  private String coordinatesToString(int[] coords) {
    if (coords == null || coords.length < 2) {
      return "pass";
    }

    char letter = (char) ('A' + coords[0] + (coords[0] >= 8 ? 1 : 0)); // Skip 'I'
    int number = 19 - coords[1]; // Convert to standard board notation
    return "" + letter + number;
  }
}
