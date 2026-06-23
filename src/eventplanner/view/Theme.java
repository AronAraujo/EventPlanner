package eventplanner.view;

import java.awt.*;

/**
 * Central visual style class: colors and fonts used across the entire
 * interface. Keeping everything here (instead of scattering colors/fonts
 * across each screen) makes it easier to change the whole UI by editing
 * only this file.
 *
 * All methods are static and the constructor is private because this
 * class is never meant to be instantiated (it is just a "constants holder").
 */
public class Theme {

    // Main color palette used throughout the interface
    public static final Color BG_PRIMARY    = new Color(0xF7F8FC);
    public static final Color BG_SIDEBAR    = new Color(0x1E2A3A);
    public static final Color BG_CARD       = Color.WHITE;
    public static final Color ACCENT        = new Color(0x4A90D9);
    public static final Color ACCENT_LIGHT  = new Color(0xE8F1FB);
    public static final Color TEXT_DARK     = new Color(0x1A202C);
    public static final Color TEXT_MID      = new Color(0x718096);
    public static final Color TEXT_LIGHT    = new Color(0xA0AEC0);
    public static final Color BORDER        = new Color(0xE2E8F0);
    public static final Color TODAY_BG      = new Color(0x4A90D9);
    public static final Color TODAY_FG      = Color.WHITE;
    public static final Color HOVER_BG      = new Color(0xEBF4FF);
    public static final Color SELECTED_BG   = new Color(0x4A90D9);
    public static final Color SELECTED_FG   = Color.WHITE;

    /**
     * Converts a category hex color string (e.g. "#4A90D9",
     * stored in Event.Category enum) into a Java Color object.
     * If the value is invalid for any reason, the default color
     * (ACCENT) is used instead of crashing the program.
     */
    public static Color getCategoryColor(String hex) {
        try { return Color.decode(hex); } catch (Exception e) { return ACCENT; }
    }

    // Default fonts used throughout the interface (bold, regular, italic)
    public static Font fontBold(int size)    { return new Font("Segoe UI", Font.BOLD, size); }
    public static Font fontRegular(int size) { return new Font("Segoe UI", Font.PLAIN, size); }
    public static Font fontItalic(int size)  { return new Font("Segoe UI", Font.ITALIC, size); }

    // Default corner radius used for rounded panels
    public static int RADIUS = 12;

    // private constructor: this class is only a constants container,
    // it makes no sense to instantiate a Theme object
    private Theme() {}
}