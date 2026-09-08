package org.examplee.palePlugin.util;

import net.md_5.bungee.api.ChatColor;

public final class Msg {
   private Msg() {
   }

   private static String rgb(String hex) {
      try {
         return ChatColor.of(hex).toString();
      } catch (Throwable var2) {
         return org.bukkit.ChatColor.GREEN.toString();
      }
   }

   public static String g(String s) {
      return rgb("#39FF14") + s + org.bukkit.ChatColor.RESET;
   }
}
