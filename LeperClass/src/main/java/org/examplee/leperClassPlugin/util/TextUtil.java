package org.examplee.leperClassPlugin.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public final class TextUtil {
   public static final Color C_GRAY = Color.fromRGB(138, 138, 138);
   public static final Color C_GREEN = Color.fromRGB(57, 255, 20);
   public static final Color C_RED = Color.fromRGB(255, 43, 43);
   public static final Color C_PURPLE = Color.fromRGB(176, 0, 255);
   public static final Color C_BLUE = Color.fromRGB(43, 229, 255);
   public static final Color C_GOLD = Color.fromRGB(255, 190, 0);
   public static final Color C_YELLOW = Color.fromRGB(255, 244, 120);
   public static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.LIGHT_PURPLE + "Leper" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;

   private TextUtil() {
   }

   public static String gGreenGray(String t) {
      return gradientMulti(t, C_GREEN, C_GRAY);
   }

   public static String gRedGray(String t) {
      return gradientMulti(t, C_RED, C_GRAY);
   }

   public static String gPurpleGray(String t) {
      return gradientMulti(t, C_PURPLE, C_GRAY);
   }

   public static String gPurpleGreen(String t) {
      return gradientMulti(t, C_PURPLE, C_GREEN);
   }

   public static String gBlueGray(String t) {
      return gradientMulti(t, C_BLUE, C_GRAY);
   }

   public static String gGoldYellow(String t) {
      return gradientMulti(t, C_GOLD, C_YELLOW);
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

   public static String gradientMulti(String text, Color... colors) {
      if (text != null && !text.isEmpty()) {
         if (colors != null && colors.length >= 2) {
            int len = text.length();
            int segments = colors.length - 1;
            StringBuilder out = new StringBuilder(len * 14);

            for (int i = 0; i < len; i++) {
               double p = len == 1 ? 0.0 : (double)i / (double)(len - 1);
               double scaled = p * (double)segments;
               int seg = Math.min(segments - 1, Math.max(0, (int)Math.floor(scaled)));
               double t = scaled - (double)seg;
               Color a = colors[seg];
               Color b = colors[seg + 1];
               int r = (int)Math.round((double)a.getRed() + t * (double)(b.getRed() - a.getRed()));
               int g = (int)Math.round((double)a.getGreen() + t * (double)(b.getGreen() - a.getGreen()));
               int bl = (int)Math.round((double)a.getBlue() + t * (double)(b.getBlue() - a.getBlue()));
               out.append(legacyHex(r, g, bl)).append(text.charAt(i));
            }

            return out.toString();
         } else {
            return text;
         }
      } else {
         return "";
      }
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
}
