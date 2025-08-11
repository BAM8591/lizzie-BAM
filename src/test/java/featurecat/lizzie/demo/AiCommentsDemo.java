package featurecat.lizzie.demo;

/**
 * Demonstration of the AI Comments feature functionality This shows how the feature would work in
 * practice
 */
public class AiCommentsDemo {

  public static void main(String[] args) {
    System.out.println("=== Lizzie-BAM AI Comments Feature Demo ===");
    System.out.println();

    // Simulate configuration
    boolean enableAiKeyComment = true;
    double threshold = 1.0;
    String language = "ru";
    String apiKey = "sk-dummy-api-key"; // Would be real OpenAI key in practice

    System.out.println("Configuration:");
    System.out.println("  Enable AI Comments: " + enableAiKeyComment);
    System.out.println("  Score Mean Threshold: " + threshold);
    System.out.println("  Language: " + language);
    System.out.println("  API Key: " + (apiKey.isEmpty() ? "Not set" : "Set"));
    System.out.println();

    // Simulate game moves with score changes
    System.out.println("Analyzing game moves for AI comments:");
    System.out.println();

    simulateMove(1, "B", "D4", 0.0, 0.2, 0.2, threshold);
    simulateMove(2, "W", "Q16", 0.2, 0.1, 0.1, threshold);
    simulateMove(3, "B", "Q4", 0.1, 0.3, 0.2, threshold);
    simulateMove(4, "W", "D16", 0.3, 0.2, 0.1, threshold);
    simulateMove(5, "B", "C3", 0.2, 1.8, 1.6, threshold); // Big gain - should trigger AI comment
    simulateMove(6, "W", "R4", 1.8, 0.5, 1.3, threshold); // Big loss - should trigger AI comment
    simulateMove(7, "B", "P3", 0.5, 0.7, 0.2, threshold);

    System.out.println();
    System.out.println("=== Sample SGF Output ===");
    System.out.println("(;FF[4]GM[1]SZ[19]");
    System.out.println(";B[dd]");
    System.out.println(";W[qd]");
    System.out.println(";B[qq]");
    System.out.println(";W[dd]");
    System.out.println(
        ";B[cc]C[Этот ход значительно улучшил позицию черных на 1.6 очка. Черные захватили важный угол и получили прочную территорию.]");
    System.out.println(
        ";W[rq]C[Белые допустили серьезную ошибку, потеряв 1.3 очка. Этот ход позволил черным укрепить свою позицию в углу.]");
    System.out.println(";B[pp])");
    System.out.println();
    System.out.println(
        "AI Comments would be generated automatically for moves with score changes >= "
            + threshold
            + " points");
  }

  private static void simulateMove(
      int moveNum,
      String color,
      String coord,
      double prevScore,
      double curScore,
      double delta,
      double threshold) {
    boolean shouldGenerateComment = delta >= threshold;

    System.out.printf(
        "Move %d: %s[%s] - Score: %.1f -> %.1f (Δ%.1f) %s\n",
        moveNum,
        color,
        coord,
        prevScore,
        curScore,
        delta,
        shouldGenerateComment ? "→ AI COMMENT" : "");

    if (shouldGenerateComment) {
      String sampleComment = generateSampleComment(color, delta, "ru");
      System.out.println("    Comment: " + sampleComment);
    }
  }

  private static String generateSampleComment(String color, double delta, String lang) {
    String player = "B".equals(color) ? "Черные" : "Белые";
    if (delta > 0) {
      return String.format(
          "%s сделали отличный ход, улучшив позицию на %.1f очка. "
              + "Этот ход демонстрирует правильное понимание позиции.",
          player, delta);
    } else {
      return String.format(
          "%s допустили ошибку, потеряв %.1f очка. "
              + "Следует быть более осторожным в подобных позициях.",
          player, Math.abs(delta));
    }
  }
}
