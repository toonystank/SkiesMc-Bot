package com.taggernation.skiesmcbot.commands;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.skiesmcbot.tasks.LoopTask;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Commands extends ListenerAdapter {

    private final LoopTask loopTask;
    public Commands(LoopTask loopTask) {
        this.loopTask = loopTask;
    }
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        switch (event.getMessage().getContentRaw().toLowerCase().split(" ")[0]) {
            case "!looptext":
                new LoopText(loopTask).loopCommand(event);
                break;
            case "!say":
                new Say().sayCommand(event);
                break;
            case "!help":
                event.getChannel().sendMessageEmbeds(new HelpEmbed().getEmbed(event)).queue();
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
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("duels")) {
                new DuelStatus(SkiesMCBOT.duels).statusCommand(event, args);
            }
        }
    }
}
