package event;

import io.papermc.paperweight.testplugin.TestPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import util.Reviver;
import util.SelfRevive;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static util.utilsFunctions.*;

public class ReviveListener implements Listener {

  public final double checkRadius = 1.5;
  public final int selfReviveSeconds = 5 * 60; // 5 минут для самостоятельного воскрешения

  private final Map<String, Reviver> proximityDataMap = new HashMap<>();
  private final TestPlugin testPlugin;
  private final Map<UUID, SelfRevive> reviverMap = new HashMap<>();
  private final Map<UUID, Integer> revivalProgressMap = new HashMap<>();

  public ReviveListener(TestPlugin testPlugin) {
    this.testPlugin = testPlugin;
  }

  public void startRevivalProcess(Player player) {
    if (reviverMap.containsKey(player.getUniqueId()) || !testPlugin.getPlayerListener().deathPlayer(player)) {
      return;
    }

    int initialSeconds = revivalProgressMap.getOrDefault(player.getUniqueId(), selfReviveSeconds);
    SelfRevive selfRevive = new SelfRevive(player, testPlugin);
    reviverMap.put(player.getUniqueId(), selfRevive);
    selfRevive.createTimer(initialSeconds);
  }

  public void removeReviverProgress(UUID uuid){
    revivalProgressMap.remove(uuid);
  }

  public boolean containReviverProgress(UUID uuid){
    return revivalProgressMap.containsKey(uuid);
  }

  public SelfRevive removeReviver(UUID uuid){
    return reviverMap.remove(uuid);
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;

    Player player = event.getPlayer();

    // Проверка ближайших игроков
    for (var entity : player.getWorld().getNearbyEntities(
      player.getLocation(),
      checkRadius,
      checkRadius,
      checkRadius,
      e -> e instanceof Player && !e.equals(player)
    )) {
      Player other = (Player) entity;
      String pairKey = getPairKey(player, other);
      Reviver data = proximityDataMap.get(pairKey);

      if (data == null) {
        if (testPlugin.getPlayerListener().deathPlayer(other) && isHoldingHealItem(player)) {
          data = new Reviver(player, other, testPlugin);
          proximityDataMap.put(pairKey, data);

          if (reviverMap.containsKey(other.getUniqueId())) {
            SelfRevive selfRevive = reviverMap.get(other.getUniqueId());
            revivalProgressMap.put(other.getUniqueId(), selfRevive.getRemainingSeconds());
            selfRevive.pauseTimer();
          }

          data.startTimer();
        }
      }
    }

    // Проверка игроков вне радиуса
    proximityDataMap.entrySet().removeIf(entry -> {
      if (entry.getKey().contains(player.getUniqueId().toString())) {
        Reviver data = entry.getValue();
        if (data.getPlayer1().getLocation().distance(data.getPlayer2().getLocation()) > checkRadius) {
          data.cancelTimer();

          if (!reviverMap.containsKey(data.getPlayer2().getUniqueId())) {
            startRevivalProcess(data.getPlayer2());
          } else {
            SelfRevive selfRevive = reviverMap.get(data.getPlayer2().getUniqueId());
            selfRevive.resumeTimer();
          }
          return true;
        }
      }
      return false;
    });
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();

    proximityDataMap.entrySet().removeIf(entry -> {
      if (entry.getKey().contains(playerId.toString())) {
        Reviver data = entry.getValue();
        data.cancelTimer();
        return true;
      }
      return false;
    });

    SelfRevive selfRevive = reviverMap.get(playerId);
    if (selfRevive != null) {
      revivalProgressMap.put(playerId, selfRevive.getRemainingSeconds());
      selfRevive.deleteTimer();
    }
  }

  @EventHandler
  public void PlayerJoinEvent(PlayerJoinEvent e){
    if(revivalProgressMap.containsKey(e.getPlayer().getUniqueId())){
      SelfRevive r = new SelfRevive(e.getPlayer(), testPlugin);
      reviverMap.put(e.getPlayer().getUniqueId(), r);
      testPlugin.getCrawlService().startCrawl(r.getReviver());
      r.createTimer(revivalProgressMap.get(r.getReviver().getUniqueId()));
    }
  }

}
