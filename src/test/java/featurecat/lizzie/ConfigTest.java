package featurecat.lizzie;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

public class ConfigTest {

  @Test
  public void testAiCommentConfigDefaults() {
    try {
      Config config = new Config();

      // Test default values
      assertFalse("AI comments should be disabled by default", config.enableAiComments);
      assertEquals(
          "Default ScoreMean threshold should be 1.0",
          1.0,
          config.aiCommentScoreMeanThreshold,
          0.001);

      // Test that the config contains the UI settings
      JSONObject uiConfig = config.uiConfig;
      assertFalse(
          "UI config should have enable-ai-comments set to false",
          uiConfig.optBoolean("enable-ai-comments", true));
      assertEquals(
          "UI config should have ai-comment-scoremean-threshold set to 1.0",
          1.0,
          uiConfig.optDouble("ai-comment-scoremean-threshold", 0.0),
          0.001);

    } catch (Exception e) {
      fail("Config initialization should not throw exception: " + e.getMessage());
    }
  }
}
