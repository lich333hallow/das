package sevices;

import io.papermc.paperweight.testplugin.TestPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigService {
  public String L_LANG;
  public boolean L_CLIENT_LANG;
  public boolean CHECK_FOR_UPDATE;
  public boolean DEBUG;
  public boolean GET_UP_DAMAGE;
  public boolean GET_UP_SNEAK;
  public boolean GET_UP_RETURN;
  public boolean GET_UP_BREAK;
  public boolean ALLOW_UNSAFE;
  public boolean SAME_BLOCK_REST;
  public boolean CENTER_BLOCK;
  public boolean CUSTOM_MESSAGE;
  public final HashMap<Material, Double> S_SITMATERIALS = new HashMap<>();
  public boolean S_BOTTOM_PART_ONLY;
  public boolean S_EMPTY_HAND_ONLY;
  public double S_MAX_DISTANCE;
  public boolean S_DEFAULT_SIT_MODE;
  public boolean PS_ALLOW_SIT;
  public boolean PS_ALLOW_SIT_NPC;
  public long PS_MAX_STACK;
  public boolean PS_SNEAK_EJECTS;
  public boolean PS_BOTTOM_RETURN;
  public boolean PS_EMPTY_HAND_ONLY;
  public double PS_MAX_DISTANCE;
  public boolean PS_DEFAULT_SIT_MODE;
  public boolean P_INTERACT;
  public boolean P_LAY_REST;
  public boolean P_LAY_SNORING_SOUNDS;
  public boolean P_LAY_SNORING_NIGHT_ONLY;
  public boolean P_LAY_NIGHT_SKIP;
  public boolean C_GET_UP_SNEAK;
  public boolean C_DOUBLE_SNEAK;
  public boolean C_DEFAULT_CRAWL_MODE;
  public boolean TRUSTED_REGION_ONLY;
  public List<String> WORLDBLACKLIST = new ArrayList<>();
  public List<String> WORLDWHITELIST = new ArrayList<>();
  public final List<Material> MATERIALBLACKLIST = new ArrayList<>();
  public List<String> COMMANDBLACKLIST = new ArrayList<>();
  public boolean ENHANCED_COMPATIBILITY;
  public List<String> FEATUREFLAGS = new ArrayList<>();

  private final TestPlugin testPlugin;

  public ConfigService(TestPlugin testPlugin) {
    this.testPlugin = testPlugin;
    try {
        File configFile = new File(testPlugin.getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        InputStream configSteam = testPlugin.getResource("config.yml");
        if(configSteam != null) {
          FileConfiguration configSteamConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(configSteam, StandardCharsets.UTF_8));
          if(!config.getKeys(true).equals(configSteamConfig.getKeys(true))) {
            config.setDefaults(configSteamConfig);
            YamlConfigurationOptions options = (YamlConfigurationOptions) config.options();
            options.parseComments(true).copyDefaults(true).width(500);
            config.loadFromString(config.saveToString());
            for(String comments : config.getKeys(true)) config.setComments(comments, configSteamConfig.getComments(comments));
            config.save(configFile);
          }
        } else testPlugin.saveDefaultConfig();
    } catch(Throwable e) { testPlugin.saveDefaultConfig(); }

    reload();
  }

  public void reload() {
    testPlugin.reloadConfig();

    L_LANG = testPlugin.getConfig().getString("Lang.lang", "en_us").toLowerCase();
    L_CLIENT_LANG = testPlugin.getConfig().getBoolean("Lang.client-lang", true);

    CHECK_FOR_UPDATE = testPlugin.getConfig().getBoolean("Options.check-for-update", true);
    DEBUG = testPlugin.getConfig().getBoolean("Options.debug", false);
    GET_UP_DAMAGE = testPlugin.getConfig().getBoolean("Options.get-up-damage", false);
    GET_UP_SNEAK = testPlugin.getConfig().getBoolean("Options.get-up-sneak", true);
    GET_UP_RETURN = testPlugin.getConfig().getBoolean("Options.get-up-return", false);
    GET_UP_BREAK = testPlugin.getConfig().getBoolean("Options.get-up-break", true);
    ALLOW_UNSAFE = testPlugin.getConfig().getBoolean("Options.allow-unsafe", false);
    SAME_BLOCK_REST = testPlugin.getConfig().getBoolean("Options.same-block-rest", false);
    CENTER_BLOCK = testPlugin.getConfig().getBoolean("Options.center-block", true);
    CUSTOM_MESSAGE = testPlugin.getConfig().getBoolean("Options.custom-message", true);

    S_SITMATERIALS.clear();
    for(String material : testPlugin.getConfig().getStringList("Options.Sit.SitMaterials")) {
      try {
        String[] materialAndOffset = material.split(";");
        if(materialAndOffset[0].startsWith("#")) {
          for(Material tagMaterial : Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(materialAndOffset[0].substring(1).toLowerCase()), Material.class).getValues()) S_SITMATERIALS.put(tagMaterial, materialAndOffset.length > 1 ? Double.parseDouble(materialAndOffset[1]) : 0d);
          continue;
        }
        S_SITMATERIALS.put(materialAndOffset[0].equalsIgnoreCase("*") ? Material.AIR : Material.valueOf(materialAndOffset[0].toUpperCase()), materialAndOffset.length > 1 ? Double.parseDouble(materialAndOffset[1]) : 0d);
      } catch(Throwable ignored) { }
    }
    S_BOTTOM_PART_ONLY = testPlugin.getConfig().getBoolean("Options.Sit.bottom-part-only", true);
    S_EMPTY_HAND_ONLY = testPlugin.getConfig().getBoolean("Options.Sit.empty-hand-only", true);
    S_MAX_DISTANCE = testPlugin.getConfig().getDouble("Options.Sit.max-distance", 0d);
    S_DEFAULT_SIT_MODE = testPlugin.getConfig().getBoolean("Options.Sit.default-sit-mode", true);

    PS_ALLOW_SIT = testPlugin.getConfig().getBoolean("Options.PlayerSit.allow-sit", true);
    PS_ALLOW_SIT_NPC = testPlugin.getConfig().getBoolean("Options.PlayerSit.allow-sit-npc", true);
    PS_MAX_STACK = testPlugin.getConfig().getLong("Options.PlayerSit.max-stack", 0);
    PS_SNEAK_EJECTS = testPlugin.getConfig().getBoolean("Options.PlayerSit.sneak-ejects", true);
    PS_BOTTOM_RETURN = testPlugin.getConfig().getBoolean("Options.PlayerSit.bottom-return", false);
    PS_EMPTY_HAND_ONLY = testPlugin.getConfig().getBoolean("Options.PlayerSit.empty-hand-only", true);
    PS_MAX_DISTANCE = testPlugin.getConfig().getDouble("Options.PlayerSit.max-distance", 0d);
    PS_DEFAULT_SIT_MODE = testPlugin.getConfig().getBoolean("Options.PlayerSit.default-sit-mode", true);

    P_INTERACT = testPlugin.getConfig().getBoolean("Options.Pose.interact", false);
    P_LAY_REST = testPlugin.getConfig().getBoolean("Options.Pose.lay-rest", true);
    P_LAY_SNORING_SOUNDS = testPlugin.getConfig().getBoolean("Options.Pose.lay-snoring-sounds", false);
    P_LAY_SNORING_NIGHT_ONLY = testPlugin.getConfig().getBoolean("Options.Pose.lay-snoring-night-only", true);
    P_LAY_NIGHT_SKIP = testPlugin.getConfig().getBoolean("Options.Pose.lay-night-skip", true);

    C_GET_UP_SNEAK = testPlugin.getConfig().getBoolean("Options.Crawl.get-up-sneak", true);
    C_DOUBLE_SNEAK = testPlugin.getConfig().getBoolean("Options.Crawl.double-sneak", false);
    C_DEFAULT_CRAWL_MODE = testPlugin.getConfig().getBoolean("Options.Crawl.default-crawl-mode", true);

    TRUSTED_REGION_ONLY = testPlugin.getConfig().getBoolean("Options.trusted-region-only", false);
    WORLDBLACKLIST = testPlugin.getConfig().getStringList("Options.WorldBlacklist");
    WORLDWHITELIST = testPlugin.getConfig().getStringList("Options.WorldWhitelist");
    MATERIALBLACKLIST.clear();
    for(String material : testPlugin.getConfig().getStringList("Options.MaterialBlacklist")) {
      try {
        if(material.startsWith("#")) MATERIALBLACKLIST.addAll(Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(material.substring(1).toLowerCase()), Material.class).getValues());
        else MATERIALBLACKLIST.add(Material.valueOf(material.toUpperCase()));
      } catch(Throwable ignored) { }
    }
    COMMANDBLACKLIST = testPlugin.getConfig().getStringList("Options.CommandBlacklist");
    ENHANCED_COMPATIBILITY = testPlugin.getConfig().getBoolean("Options.enhanced-compatibility", false);
    FEATUREFLAGS = testPlugin.getConfig().getStringList("Options.FeatureFlags");
  }
}
