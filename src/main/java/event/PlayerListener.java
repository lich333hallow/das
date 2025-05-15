package event;

import io.papermc.paperweight.testplugin.TestPlugin;
import object.IGCrawl;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

  private final TestPlugin testPlugin;
  private final Map<UUID, IGCrawl> crawlers = new HashMap<>();

  public PlayerListener(TestPlugin testPlugin){
    this.testPlugin = testPlugin;
  }

  @EventHandler
  public void onPlayerDamage(EntityDamageEvent e){
    if(e.getEntity() instanceof Player player){
      if(player.getInventory().getItemInMainHand().getItemMeta() != null){
        if(player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("Heal")){
          e.setCancelled(true);
        }
      }

      double currHP = player.getHealth();
      if (currHP - e.getDamage() <= 1){
        IGCrawl igCrawl = testPlugin.getCrawlService().startCrawl(player);
        Location loc = player.getLocation();
        loc.setPitch(90); // 90 вниз, -90 вверх
        player.teleport(loc);
        crawlers.put(player.getUniqueId(), igCrawl);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 7000, 200));
        player.sendTitle("§0", "§0", 0, 200, 0);
        player.setHealth(1);
        e.setCancelled(true);

        testPlugin.getReviveListener().startRevivalProcess(player);
      }
    }
  }

  public boolean deathPlayer(Player player){
    return crawlers.containsKey(player.getUniqueId());
  }

  public void removeDeathPlayer(Player player) {
    crawlers.remove(player.getUniqueId());
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent e){
    if(testPlugin.getCrawlService().isPlayerCrawling(e.getPlayer()) && deathPlayer(e.getPlayer())){
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void PlayerInteractEvent(PlayerInteractEvent e){
    if(deathPlayer(e.getPlayer())){
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void PlayerItemConsumeEvent(PlayerItemConsumeEvent e){
    if(deathPlayer(e.getPlayer())){
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void PlayerDropItemEvent(PlayerDropItemEvent e){
    if(deathPlayer(e.getPlayer())){
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerHeal(EntityRegainHealthEvent e){
    if(e.getEntity() instanceof Player player){
      if(e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED ||
        e.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN){
        e.setCancelled(true);
      }
    }
  }
}
