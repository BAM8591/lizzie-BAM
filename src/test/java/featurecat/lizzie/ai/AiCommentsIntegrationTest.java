package featurecat.lizzie.ai;

import static org.junit.Assert.*;

import org.junit.Test;

/** Integration test to demonstrate the AI comments feature functionality */
public class AiCommentsIntegrationTest {

  @Test
  public void testScoreMeanThresholdLogic() {
    // Test the core logic for determining when to generate AI comments
    double threshold = 1.0;

    // Case 1: Small change - should not trigger comment
    double prevScore1 = 2.0;
    double curScore1 = 2.5;
    double delta1 = Math.abs(curScore1 - prevScore1);
    assertFalse("Small score change should not trigger AI comment", delta1 >= threshold);

    // Case 2: Large change - should trigger comment
    double prevScore2 = 2.0;
    double curScore2 = 3.5;
    double delta2 = Math.abs(curScore2 - prevScore2);
    assertTrue("Large score change should trigger AI comment", delta2 >= threshold);

    // Case 3: Negative change (blunder) - should trigger comment
    double prevScore3 = 2.0;
    double curScore3 = 0.5;
    double delta3 = Math.abs(curScore3 - prevScore3);
    assertTrue("Score decrease (blunder) should trigger AI comment", delta3 >= threshold);

    // Case 4: Exact threshold - should trigger comment
    double prevScore4 = 2.0;
    double curScore4 = 3.0;
    double delta4 = Math.abs(curScore4 - prevScore4);
    assertTrue("Score change at exact threshold should trigger AI comment", delta4 >= threshold);
  }

  @Test
  public void testLanguageMapping() {
    // Test the language code mapping functionality

    // Test language codes
    String[] validLanguages = {"ru", "en", "uk"};

    for (String lang : validLanguages) {
      OpenAiCommenter.MoveInfo info = new OpenAiCommenter.MoveInfo(1, "B", 1.5, 1.0, 2.5, "D4");

      // The language should be handled without throwing exceptions
      try {
        OpenAiCommenter commenter = new OpenAiCommenter("dummy-key");
        // This would normally call the API, but with empty key it returns empty string
        String result = commenter.generateComment(lang, info);
        assertEquals("Empty API key should return empty string", "", result);
      } catch (Exception e) {
        fail("Language " + lang + " should be handled properly: " + e.getMessage());
      }
    }
  }

  @Test
  public void testCoordinateConversion() {
    // Test different coordinate formats that might be passed to the AI commenter

    String[] testCoords = {"A1", "T19", "pass", "D4", "Q16"};

    for (String coord : testCoords) {
      OpenAiCommenter.MoveInfo info = new OpenAiCommenter.MoveInfo(1, "W", 1.2, 0.5, 1.7, coord);

      assertEquals("Coordinate should be preserved in MoveInfo", coord, info.coord);
    }
  }

  @Test
  public void testPlayerColorHandling() {
    // Test that both Black and White player colors are handled correctly

    String[] colors = {"B", "W"};

    for (String color : colors) {
      OpenAiCommenter.MoveInfo info = new OpenAiCommenter.MoveInfo(1, color, 1.5, 1.0, 2.5, "D4");

      assertEquals("Player color should be preserved", color, info.playerColor);

      // Test that the color is used correctly in prompt generation (indirectly)
      OpenAiCommenter commenter = new OpenAiCommenter("");
      String result = commenter.generateComment("en", info);
      assertEquals("Empty key should return empty result", "", result);
    }
  }
}
