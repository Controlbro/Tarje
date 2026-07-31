package net.peacefulcraft.tarje.config;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import net.peacefulcraft.tarje.Tarje;
import net.peacefulcraft.tarje.shop.ShopMenu;

public class Configuration {
  private FileConfiguration c;

  private HashMap<String, ShopMenu> configuredShops;
    public Map<String, ShopMenu> getConfiguredShops() { return Collections.unmodifiableMap(configuredShops); }
  private HashMap<String, ShopMenu> enabledShops;
    public Map<String, ShopMenu> getEnabledShops() { return Collections.unmodifiableMap(enabledShops); }

  public Configuration(FileConfiguration c) {
    this.c = c;
    configuredShops = new HashMap<String, ShopMenu>();
    enabledShops = new HashMap<String, ShopMenu>();

    this.loadDebugEnaled();

    /**
     * Load the default plugin configration and use it's values as fallbacks if user-supplied configuration is incomplete.
     * This will also copy the default values for any missing configuration directives into the user's configuration.
     */
    c.options().copyDefaults(true);
    saveConfiguration();

    loadShopConfigurations();
  }

  private boolean debugEnabled;
  private void loadDebugEnaled() { debugEnabled = c.getBoolean("debug"); }
  public boolean isDebugEnabled() { return debugEnabled; }
    public void setDebugEnabled(boolean v) {
      // Avoid blocking disk work if we can
      if (v != debugEnabled) {
        debugEnabled = v;
        c.set("debug", v);
        saveConfiguration();
      }
    }

  public void saveConfiguration() { Tarje._this().saveConfig(); }

  private void loadShopConfigurations() {
    File shopDataDir = new File(Tarje._this().getDataFolder().toPath() + "/shops");
    String[] shopFileNames = shopDataDir.list(new YAMLFileFilter());
    if(shopFileNames == null || shopFileNames.length == 0) {
      Tarje._this().logWarning("No shop configurations found in Tarje/shops");
      return;
    }
    for(String shopName : shopFileNames) {
      if (configuredShops.containsKey(shopName)) {
        Tarje._this().logSevere("Attempted to load shop " + shopName + ", but a shop with that name already exists. Shop names must be unique.");
        continue;
      }
      ShopMenu configuredShop = new ShopMenu(new ShopConfiguration(shopName.replaceAll(".yml", "")));
      configuredShops.put(shopName, configuredShop);
      if (configuredShop.isEnabled()) {
        enabledShops.put(shopName, configuredShop);
        Tarje._this().registerShop(configuredShop);
      }
    }
  }
}
