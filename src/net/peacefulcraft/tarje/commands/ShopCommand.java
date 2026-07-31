package net.peacefulcraft.tarje.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.peacefulcraft.tarje.Tarje;

public class ShopCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Tarje.messagingPrefix + "GUIShops are only openable by players");
      return true;
    }

    if (label.equalsIgnoreCase("shop") || label.equalsIgnoreCase("buy")) {
      if (Tarje._this().getNumberShopsConfigured() == 0) {
        sender.sendMessage(Tarje.messagingPrefix + "No shops configured to open.");
        return true;
      }

      Tarje._this().getIndexShop().openShop(((Player) sender));
    } else if (label.equalsIgnoreCase("sell") || label.equalsIgnoreCase("vender")) {
      if (args.length == 1 && args[0].equalsIgnoreCase("hand")) {
        Tarje._this().getSellMenu().sellHand((Player) sender);
      } else if (args.length == 0) {
        Tarje._this().getSellMenu().openMenu((Player) sender);
      } else {
        sender.sendMessage(Tarje.messagingPrefix + " Usage: /" + label + " [hand]");
      }
    }
    
    return true;
  } 
}
