package eventplanner.view;

import java.awt.*;

public class Theme {

    // Paleta principal
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

    // Categorias
    public static Color getCategoryColor(String hex) {
        try { return Color.decode(hex); } catch (Exception e) { return ACCENT; }
    }

    // Tipografia
    public static Font fontBold(int size)    { return new Font("Segoe UI", Font.BOLD, size); }
    public static Font fontRegular(int size) { return new Font("Segoe UI", Font.PLAIN, size); }
    public static Font fontItalic(int size)  { return new Font("Segoe UI", Font.ITALIC, size); }

    // Bordas arredondadas (helper para painéis)
    public static int RADIUS = 12;

    private Theme() {}
}