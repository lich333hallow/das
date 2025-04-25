package sevices;

import api.events.PlayerCrawlEvent;
import api.events.PlayerStopCrawlEvent;
import api.events.PrePlayerCrawlEvent;
import api.events.PrePlayerStopCrawlEvent;
import io.papermc.paperweight.testplugin.TestPlugin;
import object.IGCrawl;
import object.SStopReason;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CrawlService {

    private final TestPlugin testPlugin;
    private final boolean available;
    private final HashMap<UUID, IGCrawl> crawls = new HashMap<>();
    private int crawlUsageCount = 0;
    private long crawlUsageNanoTime = 0;

    public CrawlService(TestPlugin testPlugin){
        this.testPlugin = testPlugin;
        available = testPlugin.getVerisonService().isNewerOrVersion(18, 0);
    }

    public boolean isAvailable() {
        return available;
    }

    public HashMap<UUID, IGCrawl> getCrawls() {
        return crawls;
    }

    public IGCrawl getCrawlByPlayer(Player player){
        return crawls.get(player.getUniqueId());
    }

    public boolean isPlayerCrawling(Player player){
        return crawls.containsKey(player.getUniqueId());
    }

    public void removeAllCrawls() {
        for(IGCrawl crawl: new ArrayList<>(crawls.values())) stopCrawl(crawl, SStopReason.PLUGIN);
    }

    public IGCrawl startCrawl(Player player){
        PrePlayerCrawlEvent prePlayerCrawlEvent = new PrePlayerCrawlEvent(player);
        Bukkit.getPluginManager().callEvent(prePlayerCrawlEvent);
        if(prePlayerCrawlEvent.isCancelled()) return null;

        System.out.println(player);
        System.out.println(testPlugin.getEntityUtil());

        IGCrawl crawl = testPlugin.getEntityUtil().createCrawl(player);
        System.out.println(player);
        crawl.start();
        crawls.put(player.getUniqueId(), crawl);
        crawlUsageCount++;
        Bukkit.getPluginManager().callEvent(new PlayerCrawlEvent(crawl));

        return crawl;
    }

    public boolean stopCrawl(IGCrawl crawl, SStopReason sStopReason){
        PrePlayerStopCrawlEvent prePlayerStopCrawlEvent = new PrePlayerStopCrawlEvent(crawl, sStopReason);
        Bukkit.getPluginManager().callEvent(prePlayerStopCrawlEvent);
        if(prePlayerStopCrawlEvent.isCancelled() && sStopReason.isCancellable()) return false;

        crawls.remove(crawl.getPlayer().getUniqueId());
        crawl.stop();
        Bukkit.getPluginManager().callEvent(new PlayerStopCrawlEvent(crawl, sStopReason));
        crawlUsageNanoTime += crawl.getLifetimeInNanoSeconds();

        return true;
    }

    public int getCrawlUsageCount() {
        return crawlUsageCount;
    }

    public long getCrawlUsageTimeInSeconds() {
        return crawlUsageNanoTime / 1_000_000_000;
    }

    public void resetCrawlUsageStats() {
        crawlUsageCount = 0;
        crawlUsageNanoTime = 0;
    }
}
