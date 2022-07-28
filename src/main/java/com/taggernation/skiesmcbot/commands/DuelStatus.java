package com.taggernation.skiesmcbot.commands;

import com.taggernation.skiesmcbot.utils.DefaultEmbed;
import me.realized.duels.api.Duels;
import me.realized.duels.api.user.UserManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class DuelStatus {

    private final Duels duels;
    public DuelStatus(Duels duels) {
        this.duels = duels;
    }
    public static String getStatus() {
        return "SkiesMCBot is currently online.";
    }

    private final List<String> wins = new ArrayList<>();
    private final List<String> losses = new ArrayList<>();

    public void statusCommand(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        DefaultEmbed.setDefault(embedBuilder, event);
        embedBuilder.setTitle(":1234: **Duels Leaderboard**");
        embedBuilder.setDescription("Duels leaderboard. Top 10 wins and losses.");

        UserManager.TopEntry topWins = duels.getUserManager().getTopWins();
        UserManager.TopEntry topLosses = duels.getUserManager().getTopLosses();
        int i = 0;
        if (topWins != null) {
            for (UserManager.TopData topData : topWins.getData()) {
                i++;
                wins.add("**" + i + "**) " + topData.getValue() + " - `" + topData.getName() + "`");
            }
            embedBuilder.addField(":crown: Wins", String.join("\n", wins), true);
        }
        i = 0;
        if (topLosses != null) {
            for (UserManager.TopData topData : topLosses.getData()) {
                i++;
                losses.add("**" + i + "**) " + topData.getValue() + " - `" + topData.getName() + "`");
            }
            embedBuilder.addField(":chart_with_downwards_trend:  Losses", String.join("\n", losses), true);
        }
        event.getTextChannel().sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }
