package com.taggernation.skiesmcbot.events;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.skiesmcbot.commands.*;
import com.taggernation.skiesmcbot.tasks.LoopTask;
import com.taggernation.skiesmcbot.tasks.TpsMonitor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class CommandEvent {

    private final LoopTask loopTask;
    private final TpsMonitor tpsMonitor;
    HelpEmbed helpEmbed;
    public CommandEvent(LoopTask loopTask, TpsMonitor tpsMonitor) {
        this.loopTask = loopTask;
        this.tpsMonitor = tpsMonitor;
        helpEmbed = new HelpEmbed();

    }

    public void onCommand(@NotNull MessageReceivedEvent event) {
        Bukkit.getLogger().info(event.getMessage().getContentRaw());
        switch (event.getMessage().getContentRaw().toLowerCase().split(" ")[0]) {
            case "!tps":
                if (event.getAuthor().isBot()) return;
                if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) return;
                if (event.getMessage().getContentRaw().split(" ").length > 1) {
                    switch (event.getMessage().getContentRaw().split(" ")[1]) {
                        case "start":
                            tpsMonitor.tpsAware(true);
                            event.getChannel().sendMessage("Tps monitoring started.").queue();
                            break;
                        case "stop":
                            tpsMonitor.tpsAware(false);
                            event.getChannel().sendMessage("Tps monitoring stopped.").queue();
                            break;
                        case "status":
                            tpsMonitor.detailedTpsReport(event);
                            break;
                        case "setinterval":
                            if (event.getMessage().getContentRaw().split(" ").length > 2) {
                                tpsMonitor.setInterval(Integer.parseInt(event.getMessage().getContentRaw().split(" ")[2]));
                                event.getChannel().sendMessage("Tps monitoring interval set to " + event.getMessage().getContentRaw().split(" ")[2] + " ticks.").queue();
                                return;
                            }
                            return;
                    }
                }
                return;
            case "!looptext":
                new LoopText(loopTask).loopCommand(event);
                break;
            case "!say":
                new Say().sayCommand(event);
                break;
            case "!help":
                event.getChannel().sendMessageEmbeds(helpEmbed.getEmbed(event)).queue();
                break;
            case "!profile":
                try {
                    new PlayerInfo(SkiesMCBOT.duels, event).sendPlayerInfo();
                } catch (IOException | InvalidConfigurationException e) {
                    e.printStackTrace();
                }
                break;
            case "!leaderboard":
            case "!status":
            case "!top":
                leaderBoard(event);
                break;
            case "!playerlist":
            case "!players":
            case "!list":
                new PlayerList().sendPlayerList(event);
            default:
                break;
        }
    }
    public void leaderBoard( MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");
        /* if (args[1].equalsIgnoreCase("duels")) {
                new DuelStatus(SkiesMCBOT.duels).statusCommand(event, args);
            }*/
    }
}
