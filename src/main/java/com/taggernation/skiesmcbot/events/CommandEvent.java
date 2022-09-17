package com.taggernation.skiesmcbot.events;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.skiesmcbot.commands.*;
import com.taggernation.skiesmcbot.tasks.LoopTask;
import com.taggernation.skiesmcbot.tasks.TpsMonitor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "tps":
                if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) return;
                if (Objects.requireNonNull(event.getOption("start")).getAsBoolean()) {
                    tpsMonitor.tpsAware(true);
                    event.getChannel().sendMessage("Tps monitoring started.").queue();
                }
                if (Objects.requireNonNull(event.getOption("stop")).getAsBoolean()) {
                    tpsMonitor.tpsAware(false);
                    event.getChannel().sendMessage("Tps monitoring stopped.").queue();
                }
                if (Objects.requireNonNull(event.getOption("status")).getAsBoolean())
                    tpsMonitor.detailedTpsReport(event.getChannel());
                if (event.getOption("setinterval") != null) {
                    tpsMonitor.setInterval(Objects.requireNonNull(event.getOption("setinterval")).getAsInt());
                    event.getChannel().sendMessage("Tps monitoring interval set to " + Objects.requireNonNull(event.getOption("setinterval")).getAsInt() + " ticks.").queue();

                }
            case "looptext":
                new LoopText(loopTask).loopCommand(event);
                break;
            case "help":
                event.getChannel().sendMessageEmbeds(helpEmbed.getEmbed(event)).queue();
            case "playerinfo":
            case "profile":
                try {
                    event.deferReply().queue();
                    new PlayerInfo(event).sendPlayerInfo();
                } catch (IOException | InvalidConfigurationException e) {
                    e.printStackTrace();
                }
            case "playerlist":
            case "players":
            case "list":
                new PlayerList().sendPlayerList(event);
            default:
                break;
        }
    }
    public void onCommand(@NotNull MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");
        switch (args[0]) {
            case "say" -> new Say().sayCommand(event);
            case "leaderboard", "status", "top" -> leaderBoard(event);
            default -> {
            }
        }
    }
    public void leaderBoard( MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");
        /* if (args[1].equalsIgnoreCase("duels")) {
                new DuelStatus(SkiesMCBOT.duels).statusCommand(event, args);
            }*/
    }
}
