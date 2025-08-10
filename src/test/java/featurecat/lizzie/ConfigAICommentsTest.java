package featurecat.lizzie;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Test;

public class ConfigAICommentsTest {

  @Test
  public void testAICommentsConfigDefaults() throws IOException {
    // Test that AI comments configuration has correct default values
    Config config = new Config();

    assertFalse("AI comments should be disabled by default", config.enableAiCommentsForKeyMoves);
    assertEquals("OpenAI API key should be empty by default", "", config.openaiApiKey);
    assertEquals(
        "Score threshold should be 1.0 by default", 1.0, config.aiCommentsScoreThreshold, 0.001);
  }

  @Test
  public void testAICommentsConfigValues() throws IOException {
    // Test that we can set AI comments configuration values
    Config config = new Config();

    // Simulate setting values through UI
    config.enableAiCommentsForKeyMoves = true;
    config.openaiApiKey = "sk-test123";
    config.aiCommentsScoreThreshold = 2.5;

    assertTrue("AI comments should be enabled", config.enableAiCommentsForKeyMoves);
    assertEquals("API key should be set", "sk-test123", config.openaiApiKey);
    assertEquals("Score threshold should be set", 2.5, config.aiCommentsScoreThreshold, 0.001);
  }
}
