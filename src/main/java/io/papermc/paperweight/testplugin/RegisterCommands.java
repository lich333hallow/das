package io.papermc.paperweight.testplugin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import object.IGCrawl;
import object.SStopReason;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("UnstableApiUsage")
public class RegisterCommands {
  private TestPlugin testPlugin;

  public RegisterCommands(TestPlugin testPlugin){
    this.testPlugin = testPlugin;
  }

  public void registry(LifecycleEventManager<Plugin> lifecycleManager){
    lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, (event -> {
      LiteralCommandNode<CommandSourceStack> cmd = Commands.literal("scp").then(Commands.literal("gcrawl")).executes(startCrawl).build();
      event.registrar().register(cmd);
    }));
  }

  public final Command<CommandSourceStack> startCrawl = (context -> {

    if(!(context.getSource().getSender() instanceof Player player)){
      return 1;
    }

    if(!(testPlugin.getCrawlService().isAvailable())){
      return 1;
    }
    IGCrawl crawl = testPlugin.getCrawlService().getCrawlByPlayer(player);
    if(crawl != null) {
      testPlugin.getCrawlService().stopCrawl(crawl, SStopReason.GET_UP);
      return 1;
    }
    testPlugin.getCrawlService().startCrawl(player);
    return 0;
  });
}
