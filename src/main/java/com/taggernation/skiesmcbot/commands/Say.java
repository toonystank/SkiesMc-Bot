package com.taggernation.skiesmcbot.commands;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.skiesmcbot.utils.DefaultEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Say {

    public void sayCommand(@NotNull MessageReceivedEvent event) {
        Bukkit.getLogger().info("Say command executed.");
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) return;
        event.getMessage().delete().queue();
        EmbedBuilder embed = new EmbedBuilder();
        DefaultEmbed.setDefault(embed, event.getAuthor().getAvatarUrl(), event.getAuthor().getName(), event.getGuild().getName(), event.getGuild().getIconUrl());

        String[] args = event.getMessage().getContentRaw().split(" ");
        MessageChannel channel;
        String message = event.getMessage().getContentRaw();
        message = message.replaceFirst("!say", "");
        try {
            channel = SkiesMCBOT.getJda().getTextChannelById(args[1]);
            message = message.replaceFirst(args[1], "");
        } catch (NumberFormatException e) {
            channel = event.getChannel();
        }
        if (channel == null) {
            channel = event.getChannel();
        }
        if (!event.getMessage().getAttachments().isEmpty()) {
            embed.setImage(event.getMessage().getAttachments().get(0).getUrl());
        }
        embed.setDescription(message);
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
