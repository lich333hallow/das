package util;

import io.papermc.paperweight.testplugin.TestPlugin;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import object.GCrawl;
import object.IGCrawl;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.swing.text.html.parser.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class EntityUtil implements IEntityUtil{
    private final TestPlugin testPlugin;
    private Field entityManager = null;

    public EntityUtil(TestPlugin scp){
        this.testPlugin = scp;
    }

    @Override
    public void setEntityLocation(Entity entity, Location location) {
        try {
            Method getHandle = entity.getClass().getMethod("getHandle");
            Object serverEntity = getHandle.invoke(entity);
            Method setPositionRotationMethod = serverEntity.getClass().getMethod("setPositionRotationMethod", double.class, double.class, double.class, float.class, float.class);
            setPositionRotationMethod.invoke(serverEntity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } catch (Throwable e){
            testPlugin.getLogger().log(Level.SEVERE, "Could not set entity location", e);
        }
    }

    private boolean spawnEntity(net.minecraft.world.entity.Entity entity) {
        if(entityManager != null) {
            try {
                PersistentEntitySectionManager<net.minecraft.world.entity.Entity> entityLookup = (PersistentEntitySectionManager<net.minecraft.world.entity.Entity>) entityManager.get(entity.level().getWorld().getHandle());
                return entityLookup.addNewEntity(entity);
            } catch(Throwable e) { testPlugin.getLogger().log(Level.SEVERE, "Could not spawn entity", e); }
            return false;
        }
        try {
            LevelEntityGetter<net.minecraft.world.entity.Entity> levelEntityGetter = entity.level().getEntities();
            return (boolean) levelEntityGetter.getClass().getMethod("addNewEntity", net.minecraft.world.entity.Entity.class).invoke(levelEntityGetter, entity);
        } catch(Throwable e) { testPlugin.getLogger().log(Level.SEVERE, "Could not spawn entity", e); }
        return false;
    }

    @Override
    public IGCrawl createCrawl(Player player) {
      return new GCrawl(player);
    }
}
