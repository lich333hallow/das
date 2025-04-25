package api.events;

import object.IGCrawl;
import object.SStopReason;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class
PrePlayerStopCrawlEvent extends PlayerEvent implements Cancellable {
    private final IGCrawl crawl;
    private final SStopReason reason;
    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();

    public PrePlayerStopCrawlEvent(@NotNull IGCrawl crawl, @NotNull SStopReason reason) {
        super(crawl.getPlayer());
        this.crawl = crawl;
        this.reason = reason;
    }

    public @NotNull IGCrawl getCrawl() { return crawl; }

    public @NotNull SStopReason getReason() { return reason; }

    @Override
    public boolean isCancelled() { return cancel; }

    @Override
    public void setCancelled(boolean cancelled) { cancel = cancelled; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }
}
