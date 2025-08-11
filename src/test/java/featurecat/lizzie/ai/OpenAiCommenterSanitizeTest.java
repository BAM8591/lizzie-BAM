package featurecat.lizzie.ai;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import org.junit.Test;

public class OpenAiCommenterSanitizeTest {

  @Test
  public void testSgfSanitization() throws Exception {
    // Test the sanitizeForSgf method using reflection since it's private
    OpenAiCommenter commenter = new OpenAiCommenter("test-key");

    // Get the private method
    Method sanitizeMethod = OpenAiCommenter.class.getDeclaredMethod("sanitizeForSgf", String.class);
    sanitizeMethod.setAccessible(true);

    // Test basic escaping
    String input1 = "Test comment with ] bracket";
    String result1 = (String) sanitizeMethod.invoke(null, input1);
    assertEquals("Should escape ] brackets", "Test comment with \\] bracket", result1);

    // Test backslash escaping
    String input2 = "Test comment with \\ backslash";
    String result2 = (String) sanitizeMethod.invoke(null, input2);
    assertEquals("Should escape backslashes", "Test comment with \\\\ backslash", result2);

    // Test both escaping
    String input3 = "Test ] and \\ both";
    String result3 = (String) sanitizeMethod.invoke(null, input3);
    assertEquals("Should escape both ] and \\", "Test \\] and \\\\ both", result3);

    // Test newline handling
    String input4 = "Line 1\nLine 2\rLine 3";
    String result4 = (String) sanitizeMethod.invoke(null, input4);
    assertEquals("Should replace newlines with spaces", "Line 1 Line 2 Line 3", result4);

    // Test null input
    String result5 = (String) sanitizeMethod.invoke(null, (String) null);
    assertEquals("Should handle null input", "", result5);

    // Test empty input
    String result6 = (String) sanitizeMethod.invoke(null, "");
    assertEquals("Should handle empty input", "", result6);
  }

  @Test
  public void testPromptGeneration() throws Exception {
    // Test the prompt generation methods
    OpenAiCommenter commenter = new OpenAiCommenter("test-key");

    // Get the private methods
    Method systemPromptMethod =
        OpenAiCommenter.class.getDeclaredMethod("systemPrompt", String.class);
    systemPromptMethod.setAccessible(true);

    Method userPromptMethod =
        OpenAiCommenter.class.getDeclaredMethod(
            "userPrompt", String.class, OpenAiCommenter.MoveInfo.class);
    userPromptMethod.setAccessible(true);

    // Test system prompts for different languages
    String ruSystemPrompt = (String) systemPromptMethod.invoke(null, "ru");
    String enSystemPrompt = (String) systemPromptMethod.invoke(null, "en");
    String ukSystemPrompt = (String) systemPromptMethod.invoke(null, "uk");

    assertTrue("Russian system prompt should contain Russian text", ruSystemPrompt.contains("Вы"));
    assertTrue("English system prompt should contain English text", enSystemPrompt.contains("You"));
    assertTrue(
        "Ukrainian system prompt should contain Ukrainian text", ukSystemPrompt.contains("Ви"));

    // Test user prompt generation
    OpenAiCommenter.MoveInfo info = new OpenAiCommenter.MoveInfo(5, "B", 2.1, 1.2, 3.3, "D4");

    String ruUserPrompt = (String) userPromptMethod.invoke(null, "ru", info);
    String enUserPrompt = (String) userPromptMethod.invoke(null, "en", info);
    String ukUserPrompt = (String) userPromptMethod.invoke(null, "uk", info);

    assertTrue("Russian user prompt should contain move number", ruUserPrompt.contains("5"));
    assertTrue("English user prompt should contain move number", enUserPrompt.contains("5"));
    assertTrue("Ukrainian user prompt should contain move number", ukUserPrompt.contains("5"));

    assertTrue("Russian user prompt should contain delta", ruUserPrompt.contains("2.1"));
    assertTrue("English user prompt should contain delta", enUserPrompt.contains("2.1"));
    assertTrue("Ukrainian user prompt should contain delta", ukUserPrompt.contains("2.1"));
  }
}
