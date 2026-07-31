package net.peacefulcraft.tarje.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class ShopCommandTabCompleter implements TabCompleter {

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (command.getName().equalsIgnoreCase("sell") && args.length == 1
        && "hand".startsWith(args[0].toLowerCase())) {
      return Collections.singletonList("hand");
    }
    return Collections.emptyList();
  }

}
