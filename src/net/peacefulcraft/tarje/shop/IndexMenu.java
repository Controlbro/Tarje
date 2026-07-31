package net.peacefulcraft.tarje.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.peacefulcraft.tarje.Tarje;
import net.peacefulcraft.tarje.listeners.InventoryClickListener;

/** The paginated list of enabled shops. */
public class IndexMenu {
  public static final String TITLE = "Server Shops";
  private static final int PAGE_SIZE = 54;
  private static final int SHOPS_PER_PAGE = 45;

  private final List<Inventory> pages = new ArrayList<>();
  private final Map<Player, InventoryView> activeViews = new HashMap<>();

  public IndexMenu(Map<String, ShopMenu> shops) {
    List<Map.Entry<String, ShopMenu>> entries = new ArrayList<>(shops.entrySet());
    int pageCount = Math.max(1, (entries.size() + SHOPS_PER_PAGE - 1) / SHOPS_PER_PAGE);
    for (int page = 0; page < pageCount; page++) {
      String title = pageCount == 1 ? TITLE : TITLE + " (" + (page + 1) + "/" + pageCount + ")";
      Inventory inventory = Tarje._this().getServer().createInventory(null,
          pageCount == 1 ? Math.max(9, ((entries.size() + 8) / 9) * 9) : PAGE_SIZE, title);
      int start = page * SHOPS_PER_PAGE;
      for (int i = start; i < Math.min(start + SHOPS_PER_PAGE, entries.size()); i++) {
        Map.Entry<String, ShopMenu> shop = entries.get(i);
        ItemStack icon = new ItemStack(shop.getValue().getConfig().getDisplayItem());
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(shop.getKey());
        icon.setItemMeta(meta);
        inventory.setItem(i - start, icon);
      }
      if (pageCount > 1) {
        if (page > 0) inventory.setItem(45, navigationItem("Previous Page"));
        if (page + 1 < pageCount) inventory.setItem(53, navigationItem("Next Page"));
      }
      pages.add(inventory);
    }
  }

  private ItemStack navigationItem(String name) {
    ItemStack item = new ItemStack(Material.ARROW);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(name);
    item.setItemMeta(meta);
    return item;
  }

  public static boolean isIndexTitle(String title) { return title.equals(TITLE) || title.startsWith(TITLE + " ("); }
  public int getShopSize() { return pages.get(0).getSize(); }
  public void openShop(Player player) { openPage(player, 0); }

  private void openPage(Player player, int page) {
    activeViews.put(player, player.openInventory(pages.get(page)));
  }

  public void onClick(Player player, ItemStack clickedItem) {
    if (clickedItem == null || clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta()) return;
    String name = clickedItem.getItemMeta().getDisplayName();
    int currentPage = pages.indexOf(player.getOpenInventory().getTopInventory());
    if (name.equals("Previous Page") || name.equals("Next Page")) {
      int nextPage = currentPage + (name.equals("Next Page") ? 1 : -1);
      InventoryClickListener.quietNextClose(player);
      player.closeInventory();
      Tarje._this().synchronize(() -> openPage(player, nextPage));
      return;
    }
    ShopMenu shop = Tarje._this().getShop(name);
    if (shop == null) return;
    InventoryClickListener.quietNextClose(player);
    player.closeInventory();
    Tarje._this().synchronize(() -> shop.openShop(player));
  }

  public void onInventoryClosed(Player player) { activeViews.remove(player); }

  public void closeAllInventoryViews() {
    new ArrayList<>(activeViews.keySet()).forEach(Player::closeInventory);
    activeViews.clear();
  }
}
