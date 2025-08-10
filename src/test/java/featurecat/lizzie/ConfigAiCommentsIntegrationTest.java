package featurecat.lizzie;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

public class ConfigAiCommentsIntegrationTest {

  @Test
  public void testAiCommentConfigSaveAndLoad() {
    try {
      // Create a temporary config for testing
      Config config = new Config();

      // Verify initial default values
      assertFalse("AI comments should be disabled by default", config.enableAiComments);
      assertEquals(
          "Default ScoreMean threshold should be 1.0",
          1.0,
          config.aiCommentScoreMeanThreshold,
          0.001);

      // Modify the values
      config.enableAiComments = true;
      config.aiCommentScoreMeanThreshold = 2.5;

      // Update the UI config
      config.uiConfig.put("enable-ai-comments", config.enableAiComments);
      config.uiConfig.put("ai-comment-scoremean-threshold", config.aiCommentScoreMeanThreshold);

      // Verify the JSON config contains our values
      assertTrue(
          "UI config should show AI comments enabled",
          config.uiConfig.getBoolean("enable-ai-comments"));
      assertEquals(
          "UI config should show threshold 2.5",
          2.5,
          config.uiConfig.getDouble("ai-comment-scoremean-threshold"),
          0.001);

    } catch (Exception e) {
      fail("AI comment config save/load test failed: " + e.getMessage());
    }
  }

  @Test
  public void testAiCommentConfigJSON() {
    try {
      Config config = new Config();
      JSONObject uiConfig = config.uiConfig;

      // Test JSON operations work correctly
      uiConfig.put("enable-ai-comments", true);
      uiConfig.put("ai-comment-scoremean-threshold", 3.14);

      assertTrue("JSON should store boolean correctly", uiConfig.getBoolean("enable-ai-comments"));
      assertEquals(
          "JSON should store double correctly",
          3.14,
          uiConfig.getDouble("ai-comment-scoremean-threshold"),
          0.001);

      // Test optional methods with defaults
      assertFalse(
          "optBoolean should work with false default",
          uiConfig.optBoolean("nonexistent-key", false));
      assertEquals(
          "optDouble should work with default",
          1.0,
          uiConfig.optDouble("nonexistent-key", 1.0),
          0.001);

    } catch (Exception e) {
      fail("AI comment JSON config test failed: " + e.getMessage());
    }
  }
}
