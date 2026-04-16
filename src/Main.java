import javax.swing.SwingUtilities;
import resumematcher.DatabaseInitializer;
import resumematcher.ResumeMatcherUI;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            ResumeMatcherUI ui = new ResumeMatcherUI();
            ui.setVisible(true);
        });
    }
}
