package org.examplee.leperClassPlugin.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public final class TextUtil {
    public static Color C_GRAY;
    public static Color C_GREEN;
    public static Color C_RED;
    public static Color C_PURPLE;
    public static Color C_BLUE;
    public static String PREFIX;

    private TextUtil() {
    }

    public static String gGreenGray(String t) {
        return gradientMulti(t, new Color[]{C_GREEN, C_GRAY});
    }

    public static String gRedGray(String t) {
        return gradientMulti(t, new Color[]{C_RED, C_GRAY});
    }

    public static String gPurpleGray(String t) {
        return gradientMulti(t, new Color[]{C_PURPLE, C_GRAY});
    }

    public static String gPurpleGreen(String t) {
        return gradientMulti(t, new Color[]{C_PURPLE, C_GREEN});
    }

    public static String gBlueGray(String t) {
        return gradientMulti(t, new Color[]{C_BLUE, C_GRAY});
    }

    public static String ui(String msg) {
        return PREFIX + msg;
    }

    public static String loreHint(String msg) {
        return ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + msg;
    }

    public static String loreWarn(String msg) {
        return ChatColor.DARK_RED + "• " + ChatColor.RED + msg;
    }

    public static String gradientMulti(String text, Color[] colors) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (colors == null || colors.length < 2) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        int n = text.length();
        for (int i = 0; i < n; i++) {
            double t = n == 1 ? 0 : (double) i / (n - 1);
            double scaled = t * (colors.length - 1);
            int idx = Math.min(colors.length - 2, (int) scaled);
            double local = scaled - idx;
            Color a = colors[idx];
            Color b = colors[idx + 1];
            int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * local);
            int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * local);
            int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * local);
            sb.append(legacyHex(r, g, bl)).append(text.charAt(i));
        }
        return sb.toString();
    }

    public static String legacyHex(int r, int g, int b) {
        String hex = String.format("%02x%02x%02x", clamp(r), clamp(g), clamp(b));
        StringBuilder sb = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            sb.append('§').append(c);
        }
        return sb.toString();
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    static {
        C_GRAY = Color.fromRGB(138, 138, 138);
        C_GREEN = Color.fromRGB(57, 255, 20);
        C_RED = Color.fromRGB(255, 43, 43);
        C_PURPLE = Color.fromRGB(176, 0, 255);
        C_BLUE = Color.fromRGB(43, 229, 255);
        PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.LIGHT_PURPLE + "Leper" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
    }
}
