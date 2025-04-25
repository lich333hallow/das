package util;

import object.IGCrawl;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.swing.text.html.parser.Entity;

public interface IEntityUtil {

    void setEntityLocation(Entity entity, Location location);

    IGCrawl createCrawl(Player player);
}
