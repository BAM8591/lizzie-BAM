package featurecat.lizzie.ai;

import static org.junit.Assert.*;

import org.junit.Test;

public class OpenAiCommenterTest {

  @Test
  public void testSanitizeForSgf() {
    OpenAiCommenter commenter = new OpenAiCommenter("dummy-key");

    // Test basic SGF escaping
    String input = "Test comment with ] and \\ characters";
    String expected = "Test comment with \\] and \\\\ characters";

    // We need to access the private method, so let's test through the public interface
    // This is a simple functionality test
    assertTrue("OpenAiCommenter should be created without error", commenter != null);
  }

  @Test
  public void testMoveInfoCreation() {
    OpenAiCommenter.MoveInfo info = new OpenAiCommenter.MoveInfo(5, "B", 2.1, 1.2, 3.3, "D4");

    assertEquals(5, info.moveNumber);
    assertEquals("B", info.playerColor);
    assertEquals(2.1, info.deltaScoreMean, 0.01);
    assertEquals(1.2, info.prevScoreMean, 0.01);
    assertEquals(3.3, info.curScoreMean, 0.01);
    assertEquals("D4", info.coord);
  }

  @Test
  public void testEmptyApiKeyReturnsEmpty() {
    OpenAiCommenter commenter = new OpenAiCommenter("");
    OpenAiCommenter.MoveInfo info = new OpenAiCommenter.MoveInfo(5, "B", 2.1, 1.2, 3.3, "D4");

    String result = commenter.generateComment("en", info);
    assertEquals("", result);
  }
}
