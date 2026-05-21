import cli.TextInterface;
import data.MenuRepository;
import ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

// A single Scanner is shared between the mode chooser and the text
// interface; opening a second Scanner on System.in would swallow buffered
// input and leave the menu reading an empty stream.

/**
 * Application entry point. Lets the user choose between the graphical
 * interface and the text interface, then loads (or seeds) the shared data
 * file before launching the chosen mode.
 *
 * <p>Usage:</p>
 * <pre>
 *   java com.koi.ram.Main          # interactive mode chooser
 *   java com.koi.ram.Main --gui    # force GUI
 *   java com.koi.ram.Main --text   # force text interface
 * </pre>
 */
public final class Main {

    private static final Path DATA_FILE =
            Paths.get("data", "menu.csv");

    private Main() {
    }

    public static void main(String[] args) {
        MenuRepository repo = new MenuRepository();
        try {
            int skipped = repo.load(DATA_FILE);
            if (repo.size() == 0) {
                repo.seedSampleData();
                repo.save(DATA_FILE);
                System.out.println("No data file found — seeded "
                        + repo.size() + " sample dishes.");
            } else if (skipped > 0) {
                System.out.println("Loaded menu (" + skipped
                        + " malformed line(s) skipped).");
            }
        } catch (IOException ex) {
            System.out.println("Could not read data file, using sample menu: "
                    + ex.getMessage());
            repo.seedSampleData();
        }

        Scanner sharedIn = new Scanner(System.in);
        String mode = resolveMode(args, sharedIn);
        if ("text".equals(mode)) {
            new TextInterface(repo, DATA_FILE, sharedIn).run();
        } else {
            launchGui(repo);
        }
    }

    /** Decides which interface to start from CLI flags or a prompt. */
    private static String resolveMode(String[] args, Scanner sharedIn) {
        for (String a : args) {
            if ("--gui".equalsIgnoreCase(a)) {
                return "gui";
            }
            if ("--text".equalsIgnoreCase(a) || "--tbi".equalsIgnoreCase(a)) {
                return "text";
            }
        }
        System.out.println("\n" +
                "Choose your interface:\n" +
                "  1) Graphical User Interface (GUI)\n" +
                "  2) Text-Based Interface (TBI)");
        System.out.print("Enter 1 or 2: ");
        String choice = sharedIn.hasNextLine()
                ? sharedIn.nextLine().trim() : "1";
        return "2".equals(choice) ? "text" : "gui";
    }

    private static void launchGui(MenuRepository repo) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to the cross-platform look and feel silently.
        }
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame(repo, DATA_FILE).setVisible(true);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null,
                        "Failed to start GUI: " + ex.getMessage(),
                        "Fatal error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}