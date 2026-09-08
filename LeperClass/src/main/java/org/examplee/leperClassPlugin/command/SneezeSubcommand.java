package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.Compat;

public final class SneezeSubcommand implements Subcommand {
   private final LeperClassPlugin plugin;
   private final Map<UUID, Long> cooldown = new ConcurrentHashMap<>();

   public SneezeSubcommand(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public String name() {
      return "sneeze";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (!sender.hasPermission("leper.sneeze")) {
         this.plugin.msg.error(sender, "Нет прав: leper.sneeze");
         return true;
      } else {
         Player source;
         if (args.length >= 2) {
            source = Bukkit.getPlayerExact(args[1]);
            if (source == null) {
               this.plugin.msg.error(sender, "Игрок не найден: " + args[1]);
               return true;
            }
         } else {
            if (!(sender instanceof Player p)) {
               this.plugin.msg.warn(sender, "Использование: /leper sneeze <player>");
               return true;
            }

            source = p;
         }

         long now = System.currentTimeMillis();
         this.cooldown.entrySet().removeIf(en -> now - en.getValue() >= this.plugin.settings.sneezeCooldownMs);
         long last = this.cooldown.getOrDefault(source.getUniqueId(), 0L);
         if (now - last < this.plugin.settings.sneezeCooldownMs) {
            long sec = (this.plugin.settings.sneezeCooldownMs - (now - last)) / 1000L;
            this.plugin.msg.warn(sender, "Чих перезаряжается: " + sec + " сек.");
            return true;
         } else {
            this.cooldown.put(source.getUniqueId(), now);
            Snowball sneeze = (Snowball)source.launchProjectile(Snowball.class);
            sneeze.setVelocity(source.getEyeLocation().getDirection().normalize().multiply(this.plugin.settings.sneezeVelocity));
            sneeze.getPersistentDataContainer().set(this.plugin.keys.sneezeProjectileKey, PersistentDataType.BYTE, (byte)1);
            sneeze.setTicksLived(Math.max(1, 120 - this.plugin.settings.sneezeMaxDistance * 2));
            source.getWorld().playSound(source.getLocation(), Compat.soundFirst("ENTITY_PANDA_SNEEZE", "ENTITY_SLIME_SQUISH"), 1.0F, 1.0F);
            this.plugin.msg.ok(sender, source.getName() + " чихнул.");
            this.plugin.log.info(sender.getName() + " triggered sneeze for " + source.getName());
            return true;
         }
      }
   }

   @Override
   public List<String> tab(CommandSender sender, String[] args) {
      if (args.length != 2) {
         return List.of();
      } else {
         List<String> out = new ArrayList<>();
         Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
         return out;
      }
   }
}
