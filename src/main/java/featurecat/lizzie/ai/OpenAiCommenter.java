package featurecat.lizzie.ai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.json.JSONObject;

public class OpenAiCommenter {
  private final String apiKey;
  private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";
  private static final String MODEL = "gpt-4o-mini";

  public static class MoveInfo {
    public final int moveNumber;
    public final String playerColor; // "B" or "W"
    public final double deltaScoreMean;
    public final Double prevScoreMean; // nullable
    public final Double curScoreMean; // nullable
    public final String coord; // e.g. "D4" or "pass"

    public MoveInfo(
        int moveNumber,
        String playerColor,
        double deltaScoreMean,
        Double prevScoreMean,
        Double curScoreMean,
        String coord) {
      this.moveNumber = moveNumber;
      this.playerColor = playerColor;
      this.deltaScoreMean = deltaScoreMean;
      this.prevScoreMean = prevScoreMean;
      this.curScoreMean = curScoreMean;
      this.coord = coord;
    }
  }

  public OpenAiCommenter(String apiKey) {
    this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
  }

  public String generateComment(String lang, MoveInfo info) {
    if (apiKey.isEmpty()) return "";
    String system = systemPrompt(lang);
    String user = userPrompt(lang, info);
    try {
      String body =
          new JSONObject()
              .put("model", MODEL)
              .put("temperature", 0.3)
              .put("max_tokens", 120)
              .put(
                  "messages",
                  new org.json.JSONArray()
                      .put(new JSONObject().put("role", "system").put("content", system))
                      .put(new JSONObject().put("role", "user").put("content", user)))
              .toString();

      HttpURLConnection conn = (HttpURLConnection) new URL(ENDPOINT).openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setConnectTimeout(10_000);
      conn.setReadTimeout(10_000);
      conn.setRequestProperty("Authorization", "Bearer " + apiKey);
      conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

      try (OutputStream os = conn.getOutputStream()) {
        os.write(body.getBytes(StandardCharsets.UTF_8));
      }

      int code = conn.getResponseCode();
      if (code != 200) return "";

      StringBuilder sb = new StringBuilder();
      try (BufferedReader br =
          new BufferedReader(
              new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
      }
      JSONObject resp = new JSONObject(sb.toString());
      String text =
          resp.optJSONArray("choices")
              .optJSONObject(0)
              .optJSONObject("message")
              .optString("content", "");
      return sanitizeForSgf(text);
    } catch (Exception e) {
      return "";
    }
  }

  private static String systemPrompt(String lang) {
    switch (lang) {
      case "en":
        return "You are a concise Go (Baduk, Weiqi) commentator. Keep answers in 1-3 short sentences, plain text, no markdown.";
      case "uk":
        return "Ви лаконічний коментатор гри Го. Пишіть 1–3 короткі речення, простий текст, без форматування.";
      case "ru":
      default:
        return "Вы лаконичный комментатор партии Го. Пишите 1–3 коротких предложения, простой текст, без форматирования.";
    }
  }

  private static String userPrompt(String lang, MoveInfo i) {
    String delta = String.format("%.1f", i.deltaScoreMean);
    String prev = i.prevScoreMean == null ? "?" : String.format("%.1f", i.prevScoreMean);
    String cur = i.curScoreMean == null ? "?" : String.format("%.1f", i.curScoreMean);
    String who =
        "B".equals(i.playerColor)
            ? (lang.equals("en") ? "Black" : lang.equals("uk") ? "Чорні" : "Чёрные")
            : (lang.equals("en") ? "White" : lang.equals("uk") ? "Білі" : "Белые");
    String coord = i.coord == null ? "?" : i.coord;
    switch (lang) {
      case "en":
        return "After move "
            + i.moveNumber
            + " by "
            + who
            + " ("
            + coord
            + "), the score lead changed by "
            + delta
            + " points (from "
            + prev
            + " to "
            + cur
            + "). Explain briefly why this is important and the strategic idea.";
      case "uk":
        return "Після ходу "
            + i.moveNumber
            + " за "
            + who
            + " ("
            + coord
            + "), зміна переваги склала "
            + delta
            + " очка (з "
            + prev
            + " до "
            + cur
            + "). Коротко поясніть, чому це важливо і яка стратегічна ідея.";
      case "ru":
      default:
        return "После хода "
            + i.moveNumber
            + " за "
            + who
            + " ("
            + coord
            + ") преимущество изменилось на "
            + delta
            + " очка (с "
            + prev
            + " до "
            + cur
            + "). Коротко объясните, почему это важно и какова стратегическая идея.";
    }
  }

  private static String sanitizeForSgf(String s) {
    if (s == null) return "";
    // SGF requires escaping ']' and '\\'
    String cleaned = s.replace("\\", "\\\\").replace("]", "\\]");
    // Optionally collapse newlines
    cleaned = cleaned.replace('\r', ' ').replace('\n', ' ').trim();
    return cleaned;
  }
}
