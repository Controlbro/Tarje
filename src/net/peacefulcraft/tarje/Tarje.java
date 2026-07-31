package net.peacefulcraft.tarje;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.peacefulcraft.tarje.commands.Tarjadmin;
import net.peacefulcraft.tarje.commands.TarjeAdminTabCompleter;
import net.peacefulcraft.tarje.commands.ShopCommand;
import net.peacefulcraft.tarje.commands.ShopCommandTabCompleter;
import net.peacefulcraft.tarje.config.Configuration;
import net.peacefulcraft.tarje.listeners.InventoryClickListener;
import net.peacefulcraft.tarje.listeners.InventoryCloseListener;
import net.peacefulcraft.tarje.shop.SellMenu;
import net.peacefulcraft.tarje.shop.IndexMenu;
import net.peacefulcraft.tarje.shop.ShopMenu;
public class Tarje extends JavaPlugin {
  
  public static final String messagingPrefix = ChatColor.GREEN + "[" + ChatColor.BLUE + "PCN" + ChatColor.GREEN + "]" + ChatColor.GRAY;

  private static Tarje _this;
    public static Tarje _this() { return _this; }

  private Configuration configuration;
    public Configuration getConfiguration() { return configuration; }

  private Economy economyService;
    public Economy getEconomyService() { return economyService; }

  private IndexMenu indexShop;
    public IndexMenu getIndexShop() { return indexShop; }

  private HashMap<String, ShopMenu> shops;
    public boolean shopExists(String name) { return shops.containsKey(name); }
    public ShopMenu getShop(String name) { return shops.get(name); }
    public int getNumberShopsConfigured() { return shops.size(); }
    public Map<String, ShopMenu> getShops() { return Collections.unmodifiableMap(shops); }
    public void registerShop(ShopMenu shop) {
      shops.put(shop.getConfig().getShopName(), shop);
      generateIndexShop();
    }
    public void removeShop(String shopName) {
      shops.remove(shopName);
    }

  private SellMenu sellMenu;
    public SellMenu getSellMenu() { return sellMenu; }

  private HashMap<Material, Double> purchasableItemIndex;
    public Map<Material, Double> getPurchasableItemIndex() { return Collections.unmodifiableMap(purchasableItemIndex); }
    public Double getItemPurchasePrice(Material item) { return purchasableItemIndex.get(item); }
    public boolean isItemPurchasable(Material item) { return purchasableItemIndex.containsKey(item); }
    public void putPurchasableItemIntoIndex(Material item, Double price) { purchasableItemIndex.put(item, price); }

  private HashMap<Material, Double> sellableItemIndex;
    public Map<Material, Double> getSellableItemIndex() { return Collections.unmodifiableMap(sellableItemIndex); }
    public boolean isItemSellable(Material item) { return sellableItemIndex.containsKey(item); }
    public Double getSellableItemPrice(Material item) { return sellableItemIndex.get(item); }
    public void putSellableItemIndex(Material item, Double price) { sellableItemIndex.put(item, price); }

  /**
   * Called when Bukkit server enables the plguin
   * For improved reload behavior, use this as if it was the class constructor
   */
  public void onEnable() {
    if (!this.setupDependencies()) {
      this.getServer().getPluginManager().disablePlugin(this);
    }

    this._this = this;
    this.shops = new HashMap<String, ShopMenu>();
    this.sellMenu = new SellMenu();
    this.purchasableItemIndex = new HashMap<Material, Double>();
    this.sellableItemIndex = new HashMap<Material, Double>();

    // Save default config if one does not exist, load the configuration into memory
    this.saveDefaultConfig();
    this.configuration = new Configuration(this.getConfig());

    this.setupCommands();
    this.setupEventListeners();
  }

    private void generateIndexShop() {
      if (this.indexShop != null) this.indexShop.closeAllInventoryViews();
      this.indexShop = new IndexMenu(this.shops);
    }

  public void logDebug(String message) {
    if (configuration == null || configuration.isDebugEnabled()) {
      this.getServer().getLogger().log(Level.INFO, message);
    }
  }
  
  public void logWarning(String message) {
    this.getServer().getLogger().log(Level.WARNING, message);
  }

  public void logSevere(String message) { 
    this.getServer().getLogger().log(Level.SEVERE, message);
  }

  public BukkitTask synchronize(Runnable task) {
    return this.getServer().getScheduler().runTask(this, task);
  }

  public void reloadPlugin() {
    if (this.indexShop != null) this.indexShop.closeAllInventoryViews();
    this.shops.values().forEach((shopMenu) -> {
      shopMenu.closeAllInventoryViews();
    });

    this.reloadConfig();
    this.shops = new HashMap<String, ShopMenu>();
    this.purchasableItemIndex = new HashMap<Material, Double>();
    this.sellableItemIndex = new HashMap<Material, Double>();
    this.configuration = new Configuration(this.getConfig());
    this.generateIndexShop();
  }

  /**
   * Called whenever Bukkit server disableds the plugin
   * For improved reload behavior, try to reset the plugin to it's initaial state here.
   */
  public void onDisable () {
    this.getServer().getScheduler().cancelTasks(this);
  }

    private boolean setupDependencies() {
      return this.setupDependencyVault();
    }
    
      private boolean setupDependencyVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
          this.logSevere("This plugin requires Vault to function and will now disable itself because Vault is not installed on this server.");
          return false;
        }
        this.economyService = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        return true;
      }

    private void setupCommands() {
      ShopCommand shopCommands = new ShopCommand();
      this.getCommand("buy").setExecutor(shopCommands);
      this.getCommand("buy").setTabCompleter(new ShopCommandTabCompleter());
      this.getCommand("sell").setExecutor(shopCommands);
      this.getCommand("tarjeadmin").setExecutor(new Tarjadmin());
      this.getCommand("tarjeadmin").setTabCompleter(new TarjeAdminTabCompleter());
    }

    private void setupEventListeners() {
      this.getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
      this.getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
    }
}
