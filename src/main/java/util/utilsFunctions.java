package util;

import org.bukkit.entity.Player;

import java.util.UUID;

public class utilsFunctions {

  public static boolean isHoldingHealItem(Player player) {
    return player.getInventory().getItemInMainHand().getItemMeta() != null &&
      "Heal".equals(player.getInventory().getItemInMainHand().getItemMeta().getDisplayName());
  }

  public static String formatSeconds(int totalSeconds) {
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return minutes + ":" + String.format("%02d", seconds);
  }

  public static String getPairKey(Player p1, Player p2) {
    UUID id1 = p1.getUniqueId();
    UUID id2 = p2.getUniqueId();
    return id1.compareTo(id2) < 0 ? id1 + ":" + id2 : id2 + ":" + id1;
  }
}
