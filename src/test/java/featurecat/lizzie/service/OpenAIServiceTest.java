package featurecat.lizzie.service;

import static org.junit.Assert.*;

import org.junit.Test;

public class OpenAIServiceTest {

  @Test
  public void testOpenAIServiceCreation() {
    // Test that OpenAIService can be created with API key
    OpenAIService service = new OpenAIService("test-api-key");
    assertNotNull("OpenAIService should be created successfully", service);
  }

  @Test
  public void testGenerateMoveCommentWithEmptyApiKey() {
    // Test that service throws IOException when API key is empty
    OpenAIService service = new OpenAIService("");

    try {
      service.generateMoveComment("черные B4", 1.5);
      fail("Should throw IOException for empty API key");
    } catch (Exception e) {
      assertTrue(
          "Should contain 'API key not configured' message",
          e.getMessage().contains("API key not configured"));
    }
  }

  @Test
  public void testGenerateMoveCommentWithNullApiKey() {
    // Test that service throws IOException when API key is null
    OpenAIService service = new OpenAIService(null);

    try {
      service.generateMoveComment("белые C16", 2.0);
      fail("Should throw IOException for null API key");
    } catch (Exception e) {
      assertTrue(
          "Should contain 'API key not configured' message",
          e.getMessage().contains("API key not configured"));
    }
  }

  @Test
  public void testTestApiKeyWithInvalidKey() {
    // Test API key validation with invalid key
    OpenAIService service = new OpenAIService("invalid-key");
    assertFalse("Invalid API key should return false", service.testApiKey());
  }
}
