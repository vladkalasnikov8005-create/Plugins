package org.examplee.palePlugin.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.examplee.palePlugin.PalePlugin;
import org.examplee.palePlugin.util.MathUtil;

public final class PaleSpreadCommand implements CommandExecutor, TabCompleter {
   private final PalePlugin plugin;

   public PaleSpreadCommand(PalePlugin plugin) {
      this.plugin = plugin;
   }

   private boolean isAdmin(CommandSender s) {
      return !(s instanceof Player) || s.hasPermission("pale.admin");
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!cmd.getName().equalsIgnoreCase("palespread")) {
         return true;
      } else if (args.length == 0) {
         sender.sendMessage(ChatColor.GRAY + "/palespread on|off|speed <1..5000>|info|map [r]|give <item> <player> [amount] [r/uses]|darkness ...|gui");
         return true;
      } else {
         String sub = args[0].toLowerCase(Locale.ROOT);
         switch (sub) {
            case "on":
               if (!this.isAdmin(sender)) {
                  sender.sendMessage(ChatColor.RED + "Нет прав: pale.admin");
                  return true;
               }

               this.plugin.spread.startRunning();
               sender.sendMessage(ChatColor.GREEN + "Разрастание ВКЛ.");
               break;
            case "off":
               if (!this.isAdmin(sender)) {
                  sender.sendMessage(ChatColor.RED + "Нет прав: pale.admin");
                  return true;
               }

               this.plugin.spread.stopRunning();
               sender.sendMessage(ChatColor.YELLOW + "Разрастание ВЫКЛ.");
               break;
            case "speed":
               if (!this.isAdmin(sender)) {
                  sender.sendMessage(ChatColor.RED + "Нет прав: pale.admin");
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage(ChatColor.GRAY + "Пример: /palespread speed 50");
                  return true;
               }

               try {
                  int v = Integer.parseInt(args[1]);
                  this.plugin.cfg.speedPerChunk = MathUtil.clamp(v, 1, 5000);
                  this.plugin.getConfig().set("spread.speedPerChunk", this.plugin.cfg.speedPerChunk);
                  this.plugin.saveConfig();
                  sender.sendMessage(ChatColor.GREEN + "speed=" + this.plugin.cfg.speedPerChunk);
               } catch (NumberFormatException var21) {
                  sender.sendMessage(ChatColor.RED + "Это не число.");
               }
               break;
            case "info":
               this.plugin.spread.sendInfo(sender);
               break;
            case "map":
               if (!(sender instanceof Player p)) {
                  sender.sendMessage("Только игрок.");
                  return true;
               }

               int var25 = this.plugin.cfg.mapItemDefaultRadiusChunks;
               if (args.length >= 2) {
                  try {
                     var25 = Integer.parseInt(args[1]);
                  } catch (Exception var20) {
                  }
               }

               var25 = MathUtil.clamp(var25, 1, this.plugin.cfg.mapMaxRadiusChunks);
               this.plugin.engine.sendMap(p, var25);
               break;
            case "give":
               if (!this.isAdmin(sender)) {
                  sender.sendMessage(ChatColor.RED + "Нет прав: pale.admin");
                  return true;
               }

               if (args.length < 3) {
                  sender.sendMessage(ChatColor.GRAY + "Пример: /palespread give salt Steve 2");
                  return true;
               }

               String what = args[1].toLowerCase(Locale.ROOT);
               String who = args[2];
               int amount = 1;
               if (args.length >= 4) {
                  try {
                     amount = Integer.parseInt(args[3]);
                  } catch (Exception var19) {
                  }
               }

               amount = MathUtil.clamp(amount, 1, 64);
               Player target = Bukkit.getPlayerExact(who);
               if (target == null) {
                  sender.sendMessage(ChatColor.RED + "Игрок не найден: " + who);
                  return true;
               }

               ItemStack item;
               switch (what) {
                  case "salt":
                     item = this.plugin.items.makeSalt(amount);
                     break;
                  case "holywater":
                     item = this.plugin.items.makeHolyWater(amount);
                     break;
                  case "ward":
                     item = this.plugin.items.makeWard(amount);
                     break;
                  case "greatward":
                     int chargeMin = 0;
                     if (args.length >= 5) {
                        try {
                           chargeMin = Integer.parseInt(args[4]);
                        } catch (Exception ignored) {
                        }
                     }

                     chargeMin = MathUtil.clamp(chargeMin, 0, this.plugin.cfg.greaterWardChargeMaxSec / 60);
                     item = this.plugin.items.makeGreaterWard(amount, chargeMin * 60);
                     break;
                  case "flint":
                     item = this.plugin.items.makePurifierFlint(amount);
                     break;
                  case "purge":
                     item = this.plugin.items.makeAdminPurgeWand(amount);
                     break;
                  case "map":
                     int r = this.plugin.cfg.mapItemDefaultRadiusChunks;
                     if (args.length >= 5) {
                        try {
                           r = Integer.parseInt(args[4]);
                        } catch (Exception var18) {
                        }
                     }

                     r = MathUtil.clamp(r, 1, this.plugin.cfg.mapMaxRadiusChunks);
                     item = this.plugin.items.makeInfectionMap(amount, r);
                     break;
                  case "wand":
                     int uses = this.plugin.cfg.infectWandUsesDefault;
                     if (args.length >= 5) {
                        try {
                           uses = Integer.parseInt(args[4]);
                        } catch (Exception var17) {
                        }
                     }

                     uses = MathUtil.clamp(uses, 1, 10000);
                     item = this.plugin.items.makeInfectWand(amount, uses);
                     break;
                  case "darkblock":
                     item = this.plugin.items.makeDarkBlock(amount, target);
                     break;
                  case "darkmap":
                     int dr = this.plugin.cfg.mapItemDefaultRadiusChunks;
                     if (args.length >= 5) {
                        try {
                           dr = Integer.parseInt(args[4]);
                        } catch (Exception ignored) {
                        }
                     }

                     dr = MathUtil.clamp(dr, 1, this.plugin.cfg.mapMaxRadiusChunks);
                     item = this.plugin.items.makeDarkMap(amount, dr);
                     break;
                  case "darkwand":
                     int dwr = 64; // по умолчанию — очистка всей тьмы в радиусе 64 чанков
                     if (args.length >= 5) {
                        try {
                           dwr = Integer.parseInt(args[4]);
                        } catch (Exception ignored) {
                        }
                     }

                     dwr = MathUtil.clamp(dwr, 1, 64);
                     item = this.plugin.items.makeDarkPurgeWand(amount, dwr);
                     break;
                  default:
                     sender.sendMessage(ChatColor.GRAY + "items: salt|holywater|ward|greatward|flint|purge|map|wand|darkblock|darkmap|darkwand");
                     return true;
               }

               this.plugin.items.giveOrDrop(target, item);
               sender.sendMessage(ChatColor.GREEN + "Выдано " + what + " -> " + target.getName() + " x" + amount);
               break;
            case "darkness":
            case "тьма":
               if (!this.isAdmin(sender)) {
                  sender.sendMessage(ChatColor.RED + "Нет прав: pale.admin");
                  return true;
               }

               this.handleDarkness(sender, args);
               break;
            case "gui":
               if (!(sender instanceof Player p)) {
                  sender.sendMessage("Только игрок.");
                  return true;
               }

               if (!this.isAdmin(sender)) {
                  sender.sendMessage(ChatColor.RED + "Нет прав: pale.admin");
                  return true;
               }

               p.openInventory(this.plugin.adminGui.build(p));
               break;
            default:
               sender.sendMessage(ChatColor.RED + "Неизвестно. /palespread");
         }

         return true;
      }
   }

   /** /palespread darkness on|off|speed <1-100>|growth <1-100>|info */
   private void handleDarkness(CommandSender sender, String[] args) {
      var d = this.plugin.darkness;
      if (d == null) {
         sender.sendMessage(ChatColor.RED + "Модуль тьмы не инициализирован.");
         return;
      }

      if (args.length < 2) {
         sender.sendMessage(ChatColor.GRAY + "/palespread darkness on|off|speed <1-1000>|growth <1-1000>|infectall on|off|info");
         return;
      }

      String op = args[1].toLowerCase(Locale.ROOT);
      switch (op) {
         case "on":
            d.enabled = true;
            this.plugin.getConfig().set("darkness.enabled", true);
            this.plugin.saveConfig();
            sender.sendMessage(ChatColor.DARK_GRAY + "☠ " + ChatColor.GRAY + "Тьма " + ChatColor.GREEN + "ПРОБУЖДЕНА" + ChatColor.GRAY + ".");
            break;
         case "off":
            d.enabled = false;
            this.plugin.getConfig().set("darkness.enabled", false);
            this.plugin.saveConfig();
            sender.sendMessage(ChatColor.DARK_GRAY + "☠ " + ChatColor.GRAY + "Тьма " + ChatColor.YELLOW + "УСЫПЛЕНА" + ChatColor.GRAY + " (блоки остались, эффекты и рост выключены).");
            break;
         case "speed": {
            int v = this.parseSpeed(sender, args, "Скорость заражения");
            if (v < 0) return;
            d.infectSpeed = v;
            this.plugin.getConfig().set("darkness.infectSpeed", v);
            this.plugin.saveConfig();
            sender.sendMessage(ChatColor.GRAY + "Скорость заражения тьмы: " + ChatColor.WHITE + v + ChatColor.DARK_GRAY + " /1000" + this.speedNote(v));
            break;
         }
         case "growth": {
            int v = this.parseSpeed(sender, args, "Скорость роста веток");
            if (v < 0) return;
            d.growthSpeed = v;
            this.plugin.getConfig().set("darkness.growthSpeed", v);
            this.plugin.saveConfig();
            sender.sendMessage(ChatColor.GRAY + "Скорость роста веток тьмы: " + ChatColor.WHITE + v + ChatColor.DARK_GRAY + " /1000" + this.speedNote(v));
            break;
         }
         case "infectall": {
            if (args.length >= 3 && args[2].equalsIgnoreCase("on")) {
               d.infectAll = true;
               this.plugin.getConfig().set("darkness.infectAll", true);
               this.plugin.saveConfig();
               sender.sendMessage(ChatColor.DARK_GRAY + "☠ " + ChatColor.DARK_RED + "РЕЖИМ АПОКАЛИПСИС ВКЛ." + ChatColor.GRAY
                     + " Тьма заражает ВСЕ блоки, кроме: обсидиана, бедрока, плачущего обсидиана, сундуков, бочек, всех хранилищ (шалкеры, эндер-сундук), редстоуна, поршней, наблюдателей, печей, раздатчиков и воронок.");
               break;
            }
            if (args.length >= 3 && args[2].equalsIgnoreCase("off")) {
               d.infectAll = false;
               this.plugin.getConfig().set("darkness.infectAll", false);
               this.plugin.saveConfig();
               sender.sendMessage(ChatColor.DARK_GRAY + "☠ " + ChatColor.GRAY + "Режим АПОКАЛИПСИС " + ChatColor.GREEN + "ВЫКЛ." + ChatColor.GRAY
                     + " Тьма снова заражает только траву/землю/деревья/листву и бледный лес.");
               break;
            }
            sender.sendMessage(ChatColor.GRAY + "Состояние: " + (d.infectAll ? ChatColor.DARK_RED + "ВКЛ (заражает всё, кроме чёрного списка)" : ChatColor.GREEN + "ВЫКЛ (только земля/деревья/бледный лес)"));
            sender.sendMessage(ChatColor.GRAY + "Использование: " + ChatColor.WHITE + "/palespread darkness infectall on|off");
            break;
         }
         case "info":
            sender.sendMessage(ChatColor.DARK_GRAY + "===== ☠ ТЬМА ☠ =====");
            sender.sendMessage(ChatColor.GRAY + "Состояние: " + (d.enabled ? ChatColor.GREEN + "активна" : ChatColor.YELLOW + "усыплена"));
            sender.sendMessage(ChatColor.GRAY + "Скорость заражения: " + ChatColor.WHITE + d.infectSpeed + ChatColor.DARK_GRAY + " /1000");
            sender.sendMessage(ChatColor.GRAY + "Скорость роста веток: " + ChatColor.WHITE + d.growthSpeed + ChatColor.DARK_GRAY + " /1000");
            sender.sendMessage(ChatColor.GRAY + "Режим «заражать всё»: " + (d.infectAll ? ChatColor.DARK_RED + "ВКЛ" : ChatColor.GREEN + "ВЫКЛ"));
            sender.sendMessage(ChatColor.GRAY + "Блоков тьмы: " + ChatColor.WHITE + d.totalBlocks());
            sender.sendMessage(ChatColor.GRAY + "Очищение: " + ChatColor.WHITE + "только святая вода" + ChatColor.GRAY + "; оберег замедляет x10.");
            break;
         default:
            sender.sendMessage(ChatColor.GRAY + "/palespread darkness on|off|speed <1-1000>|growth <1-1000>|infectall on|off|info");
      }
   }

   private String speedNote(int v) {
      if (v >= 1000) return ChatColor.DARK_RED + " (АПОКАЛИПСИС: x100 от сотки!)";
      if (v > 100) return ChatColor.RED + " (ТУРБО: x" + String.format(java.util.Locale.ROOT, "%.0f", Math.pow(v / 100.0, 2.0)) + " от сотки)";
      if (v >= 90) return ChatColor.DARK_RED + " (ОЧЕНЬ БЫСТРО!)";
      return "";
   }

   private int parseSpeed(CommandSender sender, String[] args, String label) {
      if (args.length < 3) {
         sender.sendMessage(ChatColor.GRAY + label + ": укажите число 1..1000.");
         return -1;
      }

      try {
         return MathUtil.clamp(Integer.parseInt(args[2]), 1, 1000);
      } catch (NumberFormatException e) {
         sender.sendMessage(ChatColor.RED + "Не число: " + args[2]);
         return -1;
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
      if (!cmd.getName().equalsIgnoreCase("palespread")) {
         return Collections.emptyList();
      } else if (args.length == 1) {
         return this.filter(args[0], List.of("on", "off", "speed", "info", "map", "give", "gui", "darkness"));
      } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
         return this.filter(args[1], List.of("salt", "holywater", "ward", "greatward", "flint", "purge", "map", "wand", "darkblock", "darkmap", "darkwand"));
      } else if (args.length == 2 && (args[0].equalsIgnoreCase("darkness") || args[0].equalsIgnoreCase("тьма"))) {
         return this.filter(args[1], List.of("on", "off", "speed", "growth", "infectall", "info"));
      } else if (args.length == 3 && (args[0].equalsIgnoreCase("darkness") || args[0].equalsIgnoreCase("тьма"))
            && (args[1].equalsIgnoreCase("speed") || args[1].equalsIgnoreCase("growth"))) {
         return this.filter(args[2], List.of("1", "10", "50", "100", "250", "500", "1000"));
      } else if (args.length == 3 && (args[0].equalsIgnoreCase("darkness") || args[0].equalsIgnoreCase("тьма"))
            && args[1].equalsIgnoreCase("infectall")) {
         return this.filter(args[2], List.of("on", "off"));
      } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
         List<String> names = new ArrayList<>();

         for (Player p : Bukkit.getOnlinePlayers()) {
            names.add(p.getName());
         }

         return this.filter(args[2], names);
      } else {
         return Collections.emptyList();
      }
   }

   private List<String> filter(String prefix, List<String> opts) {
      String p = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
      List<String> out = new ArrayList<>();

      for (String o : opts) {
         if (o.toLowerCase(Locale.ROOT).startsWith(p)) {
            out.add(o);
         }
      }

      return out;
   }
}
