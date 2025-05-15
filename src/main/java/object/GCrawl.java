package object;

import io.papermc.paperweight.testplugin.TestPlugin;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Set;

public class GCrawl implements IGCrawl{
  private final TestPlugin testPlugin = TestPlugin.getIntstance();
  private final Player player;
  private final ServerPlayer serverPlayer;
  private boolean boxEntityExist = false;
  protected final BoxEntity boxEntity;
  private final Listener listener;
  private final Listener moveListener;
  private final Listener stopListener;
  private boolean finished = false;
  private final long spawnTime = System.nanoTime();

  public GCrawl(Player player){
    this.player = player;

    serverPlayer = ((CraftPlayer) player).getHandle();

    boxEntity = new BoxEntity(player.getLocation());

    listener = new Listener() {
      @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
      public void entityToggleSwimEvent(EntityToggleSwimEvent event) { if(event.getEntity() == player) event.setCancelled(true); }
    };

    moveListener = new Listener() {
      @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
      public void playerMoveEvent(PlayerMoveEvent e){
        if(e.isAsynchronous() || e.getPlayer() != player){
          return;
        }
        Location fromLocation = e.getFrom(), toLocation = e.getTo();
        if (fromLocation.getX() != toLocation.getX() || fromLocation.getY() != toLocation.getY() || fromLocation.getZ() != toLocation.getZ()) {
          tick(toLocation);
        }
      }
    };

    stopListener = new Listener() {

    };
  }

  private void tick(Location location){
    if(finished || !checkCrawlValid()) return;

    Location tickLocation = location.clone();
    Block locationBlock = tickLocation.getBlock();
    int blockSize = (int) ((tickLocation.getY() - tickLocation.getBlockY()) * 100);
    tickLocation.setY(tickLocation.getBlockY() + (blockSize >= 40 ? 2.49 : 1.49));
    Block aboveBlock = tickLocation.getBlock();
    boolean hasSolidBlackAbove = aboveBlock.getBoundingBox().contains(tickLocation.toVector()) && !aboveBlock.getCollisionShape().getBoundingBoxes().isEmpty();
    if(hasSolidBlackAbove) {
      destoryEntity();
      return;
    }

    Location playerLocation = location.clone();
    testPlugin.getTaskService().run(() -> {
      if(finished) return;

      int height = locationBlock.getBoundingBox().getHeight() >= 0.4 || playerLocation.getY() % 0.015625 == 0.0 ? (player.getFallDistance() > 0.7 ? 0 : blockSize) : 0;

      playerLocation.setY(playerLocation.getY() + (height >= 40 ? 1.5 : 0.5));

      boxEntity.setRawPeekAmount(height >= 40 ? 100 - height : 0);

      if(!boxEntityExist) {
        boxEntity.setPos(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
        serverPlayer.connection.send(new ClientboundAddEntityPacket(boxEntity.getId(), boxEntity.getUUID(), boxEntity.getX(), boxEntity.getY(), boxEntity.getZ(), boxEntity.getXRot(), boxEntity.getYRot(), boxEntity.getType(), 0, boxEntity.getDeltaMovement(), boxEntity.getYHeadRot()));
        boxEntityExist = true;
        serverPlayer.connection.send(new ClientboundSetEntityDataPacket(boxEntity.getId(), boxEntity.getEntityData().getNonDefaultValues()));
      } else {
        serverPlayer.connection.send(new ClientboundSetEntityDataPacket(boxEntity.getId(), boxEntity.getEntityData().getNonDefaultValues()));
        boxEntity.teleportTo(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
        serverPlayer.connection.send(new ClientboundTeleportEntityPacket(boxEntity.getId(), net.minecraft.world.entity.PositionMoveRotation.of(boxEntity), Set.of(), false));
      }
    }, true, playerLocation);
  }

  private boolean checkCrawlValid() {
    if(serverPlayer.isInWater() || player.isFlying()) {
      testPlugin.getCrawlService().stopCrawl(this, SStopReason.ENVIRONMENT);
      return false;
    }
    return true;
  }

  private void destoryEntity() {
    if(!boxEntityExist) return;
    serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(boxEntity.getId()));
    boxEntityExist = false;
  }

  @Override
  public void start() {
    player.setSwimming(true);

    Bukkit.getPluginManager().registerEvents(listener, testPlugin);

    testPlugin.getTaskService().runDelayed(() -> {
      Bukkit.getPluginManager().registerEvents(moveListener, testPlugin);
      if(testPlugin.getConfigService().C_GET_UP_SNEAK) Bukkit.getPluginManager().registerEvents(stopListener, testPlugin);
      tick(player.getLocation());
    }, false, player, 1);
  }

  @Override
  public void stop() {
    finished = true;
    HandlerList.unregisterAll(listener);
    HandlerList.unregisterAll(moveListener);
    HandlerList.unregisterAll(stopListener);

    player.setSwimming(false);

    destoryEntity();
  }

  @Override
  public Player getPlayer() {
    return player;
  }

  @Override
  public long getLifetimeInNanoSeconds() {
    return System.nanoTime() - spawnTime;
  }

  @Override
  public String toString() { return boxEntity.getUUID().toString(); }
}
