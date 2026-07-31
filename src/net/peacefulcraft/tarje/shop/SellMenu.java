package net.peacefulcraft.tarje.shop;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import net.peacefulcraft.tarje.Tarje;

public class SellMenu {

  private HashMap<Player, InventoryView> openViews;

  public SellMenu() {
    this.openViews = new HashMap<Player, InventoryView>();
  }

  /**
 * Open the shop inventory for a player
 * @param p The player to open the inventory for
 */
  public void openMenu(Player p) {
    this.openViews.put(p, p.openInventory(Bukkit.getServer().createInventory(null, 45, "Sell Items")));
  }

  /** Sell the stack held in the player's main hand. */
  public void sellHand(Player player) {
    ItemStack heldItem = player.getInventory().getItemInMainHand();
    if (heldItem.getType() == Material.AIR || heldItem.getAmount() == 0) {
      player.sendMessage(Tarje.messagingPrefix + " You are not holding an item to sell.");
      return;
    }
    if (!Tarje._this().isItemSellable(heldItem.getType())) {
      player.sendMessage(Tarje.messagingPrefix + " " + heldItem.getType() + " is not sellable.");
      return;
    }

    int amount = heldItem.getAmount();
    Material material = heldItem.getType();
    double payment = Tarje._this().getSellableItemPrice(material) * amount;
    player.getInventory().setItemInMainHand(null);
    Tarje._this().getEconomyService().depositPlayer(player, payment);
    player.sendMessage(Tarje.messagingPrefix + " You sold " + material + " (" + amount + ") for $" + payment);
  }

  /**
   * Don't let players move items into the sell inventory if they're not sellable
   * @param ev
   */
  public void onInventoryClick(InventoryClickEvent ev) {
    if (ev.getCurrentItem() == null) { return; }
    if (!this.openViews.containsKey((Player) ev.getView().getPlayer())) { return; }

    if (!Tarje._this().isItemSellable(ev.getCurrentItem().getType())) {
      ev.setCancelled(true);
      ((Player) ev.getView().getPlayer()).sendMessage(Tarje.messagingPrefix + ev.getCurrentItem().getType() + " is not sellable.");
    }
  }

  public void onClose(InventoryCloseEvent ev) {
    Player p = (Player) ev.getPlayer();
    Inventory inventory = ev.getInventory();
    if(!this.openViews.containsKey(p)) { return; }
    this.openViews.remove(p);

    Map<Material, Integer> soldItems = new LinkedHashMap<Material, Integer>();
    double moneyDue = 0.0;
    for (ItemStack item : inventory.getContents()) {
      if (item == null || item.getType() == Material.AIR) { continue; }

      if (Tarje._this().isItemSellable(item.getType())) {
        Tarje._this().logDebug(item.getType() + " is sellable for " + Tarje._this().getSellableItemPrice(item.getType()));
        moneyDue += Tarje._this().getSellableItemPrice(item.getType()) * item.getAmount();
        soldItems.merge(item.getType(), item.getAmount(), Integer::sum);
      } else {
        p.sendMessage(Tarje.messagingPrefix + item.getType() + " is not sellable.");
        p.getInventory().addItem(item);
      }
    }

    if (moneyDue > 0) {
      p.sendMessage(Tarje.messagingPrefix + " You sold " + formatSoldItems(soldItems) + " for $" + moneyDue);
      Tarje._this().getEconomyService().depositPlayer(p, moneyDue);
    }
  }
  
  private String formatSoldItems(Map<Material, Integer> soldItems) {
    StringBuilder confirmationItems = new StringBuilder();
    for (Map.Entry<Material, Integer> soldItem : soldItems.entrySet()) {
      if (confirmationItems.length() > 0) {
        confirmationItems.append(", ");
      }

      confirmationItems.append(soldItem.getKey()).append(" (").append(soldItem.getValue()).append(")");
    }
    return confirmationItems.toString();
  }
}
