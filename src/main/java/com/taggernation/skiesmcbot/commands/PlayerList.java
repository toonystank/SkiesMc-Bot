package com.taggernation.skiesmcbot.commands;

import com.taggernation.skiesmcbot.utils.DefaultEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerList {

    public void sendPlayerList(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        DefaultEmbed.setDefault(embed,event);
        embed.setTitle(":scroll: Player List" + " (" + Bukkit.getOnlinePlayers().size() + ")");
        List<String> players = new ArrayList<>();
        if (Bukkit.getOnlinePlayers().size() == 0) {
            players.add("No players online");
        }else {
            int i = 0;
            for (Player s : Bukkit.getOnlinePlayers()) {
                i++;
                players.add("**" + i + "**) "+ "`" + s.getName() + "`" + " - " + s.getPing() + "ms");
            }
        }
        embed.setDescription(String.join("\n", players));
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}
