package org.examplee.leperClassPlugin.command;

import java.util.List;
import org.bukkit.command.CommandSender;

public interface Subcommand {
   String name();

   boolean execute(CommandSender var1, String[] var2);

   default List<String> tab(CommandSender sender, String[] args) {
      return List.of();
   }
}
