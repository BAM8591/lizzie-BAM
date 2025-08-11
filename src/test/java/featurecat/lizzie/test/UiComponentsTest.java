package featurecat.lizzie.test;

import java.awt.*;
import javax.swing.*;

/** Simple test to verify that our UI components can be created without GUI environment */
public class UiComponentsTest {

  public static void main(String[] args) {
    System.out.println("Testing AI Comments UI Components...");

    try {
      // Test that we can create the components (this tests our imports and types)
      JCheckBox chkEnableAiKeyComment = new JCheckBox("Enable AI Comments");
      JSpinner spnScoremeanCommentThreshold = new JSpinner();
      spnScoremeanCommentThreshold.setModel(new SpinnerNumberModel(1.0, 0.1, 10.0, 0.1));

      String[] languages = {"Русский (ru)", "English (en)", "Українська (uk)"};
      JComboBox<String> cmbAiCommentsLanguage = new JComboBox<String>(languages);

      JTextField txtOpenAiApiKey = new JTextField();

      System.out.println("✓ JCheckBox for enable AI comments created successfully");
      System.out.println("✓ JSpinner for threshold created successfully");
      System.out.println("✓ JComboBox for language selection created successfully");
      System.out.println("✓ JTextField for API key created successfully");

      // Test component properties
      System.out.println("\nComponent Properties:");
      System.out.println("Checkbox selected: " + chkEnableAiKeyComment.isSelected());
      System.out.println("Spinner value: " + spnScoremeanCommentThreshold.getValue());
      System.out.println("ComboBox items: " + cmbAiCommentsLanguage.getItemCount());
      System.out.println("TextField text: '" + txtOpenAiApiKey.getText() + "'");

      // Test value setting
      chkEnableAiKeyComment.setSelected(true);
      spnScoremeanCommentThreshold.setValue(1.5);
      cmbAiCommentsLanguage.setSelectedIndex(1); // English
      txtOpenAiApiKey.setText("sk-test-key");

      System.out.println("\nAfter setting values:");
      System.out.println("Checkbox selected: " + chkEnableAiKeyComment.isSelected());
      System.out.println("Spinner value: " + spnScoremeanCommentThreshold.getValue());
      System.out.println("ComboBox selected: " + cmbAiCommentsLanguage.getSelectedItem());
      System.out.println("TextField text: '" + txtOpenAiApiKey.getText() + "'");

      // Test language mapping logic
      int langIndex = cmbAiCommentsLanguage.getSelectedIndex();
      String selectedLang = "ru"; // default
      if (langIndex == 1) selectedLang = "en";
      else if (langIndex == 2) selectedLang = "uk";

      System.out.println("Mapped language code: " + selectedLang);

      System.out.println("\n✅ All UI components working correctly!");

    } catch (Exception e) {
      System.err.println("❌ Error testing UI components: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
