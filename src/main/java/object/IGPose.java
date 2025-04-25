package object;

import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

public interface IGPose {

    void spawn();

    void remove();

    Player getPlayer();

    Pose getPose();
}
