package com.taggernation.skiesmcbot.commands;

import com.taggernation.skiesmcbot.utils.DefaultEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlayerList {

    public void sendPlayerList(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        DefaultEmbed.setDefault(embed, Objects.requireNonNull(event.getMember()).getAvatarUrl(), event.getMember().getEffectiveName(), Objects.requireNonNull(event.getGuild()).getName(), event.getGuild().getIconUrl());
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
        event.replyEmbeds(embed.build()).queue();
    }
}
