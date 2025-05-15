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

import static util.utilsFunctions.isHoldingHealItem;

public class Reviver {
  private final Player player1; // помогающий
  private final Player player2; // воскрешаемый
  private final TestPlugin testPlugin;
  private BukkitTask timer;
  private BossBar bossBar;
  private int secondsRemaining;

  private final int helpReviveSeconds = 5; // 5 секунд для помощи

  public Reviver(Player player1, Player player2, TestPlugin testPlugin) {
    this.player1 = player1;
    this.player2 = player2;
    this.secondsRemaining = helpReviveSeconds;
    this.testPlugin = testPlugin;
  }

  public Player getPlayer1() {
    return player1;
  }

  public Player getPlayer2() {
    return player2;
  }

  public void startTimer() {
    bossBar = Bukkit.createBossBar(
      ChatColor.GOLD + "Воскрешение: " + player2.getName(),
      BarColor.YELLOW,
      BarStyle.SOLID
    );
    bossBar.setProgress(1.0);
    bossBar.addPlayer(player1);

    timer = new BukkitRunnable() {
      @Override
      public void run() {
        secondsRemaining--;

        bossBar.setTitle(ChatColor.GOLD + "Воскрешение " + player2.getName() +
          ": " + secondsRemaining + " сек");
        bossBar.setProgress((double) secondsRemaining / helpReviveSeconds);

        if (player1.getLocation().distance(player2.getLocation()) > testPlugin.getReviveListener().checkRadius) {
          cancelTimer();
          return;
        }

        if (!isHoldingHealItem(player1)) {
          cancelTimer();
          return;
        }

        if (secondsRemaining <= 0) {
          completeRevival();
        }
      }
    }.runTaskTimer(testPlugin, 20L, 20L);
  }

  private void completeRevival() {
    player1.sendMessage(ChatColor.GREEN + "Вы воскресили " + player2.getName());
    IGCrawl crawlPlayer = testPlugin.getCrawlService().getCrawlByPlayer(player2);
    testPlugin.getCrawlService().stopCrawl(crawlPlayer, SStopReason.GET_UP);
    player2.removePotionEffect(PotionEffectType.BLINDNESS);
    player2.sendTitle(ChatColor.GREEN + "Вы воскресли", "", 0, 60, 0);
    player2.setHealth(20.0);

    testPlugin.getPlayerListener().removeDeathPlayer(player2);
    cancelTimer();

    this.testPlugin.getReviveListener().removeReviverProgress(player2.getUniqueId());
    SelfRevive selfRevive = this.testPlugin.getReviveListener().removeReviver(player2.getUniqueId());
    selfRevive.deleteTimer();
  }

  public void cancelTimer() {
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
    if (bossBar != null) {
      bossBar.removeAll();
      bossBar = null;
    }
    if (testPlugin.getReviveListener().containReviverProgress(player2.getUniqueId())) {
      testPlugin.getReviveListener().startRevivalProcess(player2);
    }
  }
}
