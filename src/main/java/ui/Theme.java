package ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

/**
 * Central palette and typography for the GUI so every screen shares one
 * consistent, modern look without relying on any third-party theme library.
 */
public final class Theme {

    private Theme() {
    }

    public static final Color BG          = new Color(0xF4, 0xF6, 0xF8);
    public static final Color SURFACE     = Color.WHITE;
    public static final Color PRIMARY     = new Color(0x1E, 0x88, 0x5E); // green
    public static final Color PRIMARY_DK  = new Color(0x14, 0x65, 0x46);
    public static final Color ACCENT      = new Color(0xE5, 0x4B, 0x4B); // allergen red
    public static final Color TEXT        = new Color(0x21, 0x25, 0x2B);
    public static final Color MUTED       = new Color(0x6B, 0x72, 0x80);
    public static final Color ROW_ALT     = new Color(0xF0, 0xF6, 0xF3);
    public static final Color SELECTION   = new Color(0xC8, 0xE6, 0xD5);
    public static final Color HEADER_BG   = new Color(0x14, 0x65, 0x46);

    public static final Font H1   = new Font("SansSerif", Font.BOLD, 22);
    public static final Font H2   = new Font("SansSerif", Font.BOLD, 15);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font MONO = new Font("Monospaced", Font.PLAIN, 13);

    /** Builds a flat, coloured, rounded-feel button used across the GUI. */
    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(H2);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return b;
    }

    public static Border card() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xE2, 0xE8)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12));
    }
}
