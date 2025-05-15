package io.papermc.paperweight.testplugin;

import db.SQLDataBaseConnector;
import event.PlayerListener;
import event.ReviveListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import sevices.ConfigService;
import sevices.CrawlService;
import sevices.TaskService;
import sevices.VerisonService;
import util.EntityUtil;
import util.IEntityUtil;


@DefaultQualifier(NonNull.class)
  public final class TestPlugin extends JavaPlugin implements Listener {
  private SQLDataBaseConnector dataBase;
  private static TestPlugin testPlugin;
  private VerisonService verisonService;
  private IEntityUtil entityUtil;
  private CrawlService crawlService;
  private TaskService taskService;
  private ConfigService configService;
  public boolean supportsTaskFeature = false;
  private PlayerListener playerListener;
  private ReviveListener reviveListener;

  public static TestPlugin getIntstance(){
    return testPlugin;
  }

  public IEntityUtil getEntityUtil() {
    return entityUtil;
  }

  public VerisonService getVerisonService(){
    return this.verisonService;
  }

  public CrawlService getCrawlService() {
    return this.crawlService;
  }

  public boolean supportsTaskFeature(){
    return this.supportsTaskFeature;
  }

  public TaskService getTaskService(){
    return taskService;
  }

  public ConfigService getConfigService(){
    return configService;
  }

  public PlayerListener getPlayerListener(){
    return playerListener;
  }

  public ReviveListener getReviveListener(){
    return reviveListener;
  }

  @Override
  public void onLoad() {
    testPlugin = this;

    configService = new ConfigService(this);

    verisonService = new VerisonService(this);
    crawlService = new CrawlService(this);

    playerListener = new PlayerListener(this);
    reviveListener = new ReviveListener(this);

    taskService = new TaskService(this);
  }

  @Override
  public void onEnable(){

    entityUtil = verisonService.isNewerOrVersion(18, 0) ? (IEntityUtil)
      verisonService.getPackageObjectInstance("util.EntityUtil", this) :
      new EntityUtil(this);
    Bukkit.getPluginManager().registerEvents(this.playerListener, this);
    Bukkit.getPluginManager().registerEvents(this.reviveListener, this);
  }

}
