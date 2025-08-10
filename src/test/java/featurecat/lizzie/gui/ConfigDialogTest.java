package featurecat.lizzie.gui;

import static org.junit.Assert.*;

import java.util.ResourceBundle;
import org.junit.Test;

public class ConfigDialogTest {

  @Test
  public void testAiCommentLocalizationStrings() {
    try {
      ResourceBundle resourceBundle = ResourceBundle.getBundle("l10n.DisplayStrings");

      // Test that our new localization strings exist
      String enableAiCommentsLabel =
          resourceBundle.getString("LizzieConfig.title.enableAiComments");
      assertNotNull("Enable AI comments label should exist", enableAiCommentsLabel);
      assertEquals(
          "Enable AI comments label should be correct",
          "Enable AI comments for key moments",
          enableAiCommentsLabel);

      String thresholdLabel =
          resourceBundle.getString("LizzieConfig.title.aiCommentScoreMeanThreshold");
      assertNotNull("ScoreMean threshold label should exist", thresholdLabel);
      assertEquals(
          "ScoreMean threshold label should be correct",
          "ScoreMean threshold for comments (points)",
          thresholdLabel);

    } catch (Exception e) {
      fail("Resource bundle should contain AI comment strings: " + e.getMessage());
    }
  }
}
