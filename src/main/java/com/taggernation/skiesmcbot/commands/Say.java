package com.taggernation.skiesmcbot.commands;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.skiesmcbot.utils.DefaultEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Say {

    public void sayCommand(@NotNull MessageReceivedEvent event) {
        if (!(event.getChannel() instanceof TextChannel)) return;
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) return;
        event.getMessage().delete().queue();
        EmbedBuilder embed = new EmbedBuilder();
        DefaultEmbed.setDefault(embed,event);

        String[] args = event.getMessage().getContentRaw().split(" ");
        TextChannel channel;
        String message = event.getMessage().getContentRaw();
        message = message.replaceFirst("!say", "");
        try {
            channel = SkiesMCBOT.getJda().getTextChannelById(args[1]);
            message = message.replaceFirst(args[1], "");
        } catch (NumberFormatException e) {
            channel = event.getTextChannel();
        }
        if (channel == null) {
            channel = event.getTextChannel();
        }

        embed.setDescription(message);
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
