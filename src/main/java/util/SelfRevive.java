package util;

import io.papermc.paperweight.testplugin.TestPlugin;
import object.IGCrawl;
import object.SStopReason;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static util.utilsFunctions.formatSeconds;

public class SelfRevive {
  private final Player player;
  private final TestPlugin testPlugin;
  private BukkitTask timer;
  private BossBar bossBar;
  private int secondsRemaining;
  private boolean paused = false;

  public SelfRevive(Player player, TestPlugin testPlugin) {
    this.player = player;
    this.testPlugin = testPlugin;
  }

  public int getRemainingSeconds() {
    return secondsRemaining;
  }

  public Player getReviver(){
    return player;
  }

  public void createTimer(int initialSeconds) {
    this.secondsRemaining = initialSeconds;
    this.paused = false;

    bossBar = Bukkit.createBossBar(
      ChatColor.GOLD + "Воскрешение через: " + formatSeconds(secondsRemaining) + " сек",
      BarColor.YELLOW,
      BarStyle.SOLID
    );
    bossBar.setProgress((double) secondsRemaining / testPlugin.getReviveListener().selfReviveSeconds);
    bossBar.addPlayer(player);

    timer = new BukkitRunnable() {
      @Override
      public void run() {
        if (paused) return;

        secondsRemaining--;
        bossBar.setTitle(ChatColor.GOLD + "Воскрешение через: " + formatSeconds(secondsRemaining) + " сек");
        bossBar.setProgress((double) secondsRemaining / testPlugin.getReviveListener().selfReviveSeconds);

        if (secondsRemaining <= 0) {
          completeRevival();
        }
      }
    }.runTaskTimer(testPlugin, 20L, 20L);
  }

  public void pauseTimer() {
    this.paused = true;
    if (bossBar != null) {
      bossBar.setTitle(ChatColor.GOLD + "Воскрешение приостановлено");
      bossBar.setColor(BarColor.WHITE);
    }
  }

  public void resumeTimer() {
    this.paused = false;
    if (bossBar != null) {
      bossBar.setColor(BarColor.YELLOW);
    }
  }

  private void completeRevival() {
    IGCrawl crawlPlayer = testPlugin.getCrawlService().getCrawlByPlayer(player);
    testPlugin.getCrawlService().stopCrawl(crawlPlayer, SStopReason.GET_UP);
    player.removePotionEffect(PotionEffectType.BLINDNESS);
    player.sendTitle(ChatColor.GREEN  + "Вы успешно воскресли", "", 0, 60, 0);
    player.setHealth(20.0);
    deleteTimer();
    testPlugin.getPlayerListener().removeDeathPlayer(player);
    testPlugin.getReviveListener().removeReviverProgress(player.getUniqueId());
  }

  public void deleteTimer() {
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
    if (bossBar != null) {
      bossBar.removeAll();
      bossBar = null;
    }
  }
}
