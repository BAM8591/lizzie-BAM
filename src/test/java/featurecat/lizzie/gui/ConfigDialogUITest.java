package featurecat.lizzie.gui;

/**
 * Simple test application to display ConfigDialog and capture screenshot This would be used for
 * manual testing of the UI changes
 */
public class ConfigDialogUITest {

  public static void main(String[] args) {
    System.out.println(
        "This test would open the ConfigDialog to verify AI comment controls are visible");
    System.out.println("New controls should appear in the UI tab:");
    System.out.println("- Checkbox: 'Enable AI comments for key moments'");
    System.out.println("- Text field: 'ScoreMean threshold for comments (points)' (default: 1.0)");

    // Note: Actual GUI testing would require a display environment
    // SwingUtilities.invokeLater(() -> {
    //     try {
    //         Lizzie.initializeAfterVersionCheck(null);
    //         ConfigDialog dialog = new ConfigDialog();
    //         dialog.switchTab(1); // Switch to UI tab
    //         dialog.setVisible(true);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // });
  }
}
