package featurecat.lizzie.ai;

import featurecat.lizzie.Lizzie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

/** Service to obtain concise AI commentary for key Go moves based on winrate swings. */
public class AiCommentService {
  private static final AiCommentService INSTANCE = new AiCommentService();

  public static AiCommentService get() {
    return INSTANCE;
  }

  private final HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  private AiCommentService() {}

  public boolean isEnabled() {
    return Lizzie.config.enableAiKeyComment
        && Lizzie.config.openAiApiKey != null
        && !Lizzie.config.openAiApiKey.trim().isEmpty();
  }

  public static class MoveContext {
    public int moveNumber;
    public boolean isBlack;
    public String playedCoord;
    public double winrateBefore;
    public double playedWinrate;
    public double bestWinrate;
    public String bestCoord;

    public double delta() {
      return Math.abs(bestWinrate - playedWinrate);
    }
  }

  private String pct(double v) {
    return String.format("%.1f", v * 100);
  }

  public String buildPrompt(MoveContext ctx) {
    String language = Lizzie.config.aiCommentsLanguage;
    return "Analyze a Go move. Move number: "
        + ctx.moveNumber
        + ". Color: "
        + (ctx.isBlack ? "Black" : "White")
        + ". Played coord: "
        + ctx.playedCoord
        + ". Previous winrate: "
        + pct(ctx.winrateBefore)
        + "%."
        + " Played move winrate: "
        + pct(ctx.playedWinrate)
        + "%."
        + " Best alternative winrate (approx): "
        + pct(ctx.bestWinrate)
        + "%."
        + " Winrate swing: "
        + pct(Math.abs(ctx.bestWinrate - ctx.playedWinrate))
        + "%."
        + " Provide concise explanation (<=2 sentences) in language code: "
        + language
        + ". No markdown.";
  }

  public String requestComment(MoveContext ctx) {
    if (!isEnabled()) return null;
    String language = Lizzie.config.aiCommentsLanguage;
    String prompt = buildPrompt(ctx);
    if (Lizzie.config.debug) System.out.println("[AI] Prompt (" + ctx.moveNumber + ") " + prompt);
    try {
      JSONObject body =
          new JSONObject()
              .put("model", "gpt-4o-mini")
              .put(
                  "messages",
                  new JSONArray()
                      .put(
                          new JSONObject()
                              .put("role", "system")
                              .put(
                                  "content",
                                  "You are a Go analysis assistant. Respond ONLY in language code: "
                                      + language
                                      + ". Max 2 short sentences."))
                      .put(new JSONObject().put("role", "user").put("content", prompt)))
              .put("temperature", 0.2)
              .put("max_tokens", 120);
      HttpRequest req =
          HttpRequest.newBuilder()
              .uri(URI.create("https://api.openai.com/v1/chat/completions"))
              .timeout(Duration.ofSeconds(20))
              .header("Authorization", "Bearer " + Lizzie.config.openAiApiKey)
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
              .build();
      HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() == 200) {
        JSONObject json = new JSONObject(resp.body());
        String content =
            json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
                .replace("\n", " ");
        return "AI: " + content;
      } else {
        System.err.println(
            "[AI] OpenAI API error "
                + resp.statusCode()
                + ": "
                + resp.body().substring(0, Math.min(300, resp.body().length())));
      }
    } catch (Exception e) {
      System.err.println("[AI] Exception: " + e.getMessage());
      if (Lizzie.config.debug) e.printStackTrace();
    }
    return null;
  }
}
