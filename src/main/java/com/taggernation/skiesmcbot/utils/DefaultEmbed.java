package com.taggernation.skiesmcbot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Date;

public class DefaultEmbed {

    public static void setDefault(EmbedBuilder embedBuilder, MessageReceivedEvent event) {
        embedBuilder.setAuthor(event.getAuthor().getName(), null, event.getAuthor().getAvatarUrl());
        embedBuilder.setTimestamp(new Date().toInstant());
        embedBuilder.setColor(0x2F3136);
        embedBuilder.setFooter(event.getGuild().getName(), event.getGuild().getIconUrl());
    }
}
