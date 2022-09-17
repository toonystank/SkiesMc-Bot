package com.taggernation.skiesmcbot.utils;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Date;

public class DefaultEmbed {

    public static void setDefault(EmbedBuilder embedBuilder, String avatarURL, String name, String guildName, String guildURL) {
        embedBuilder.setAuthor(name, null, avatarURL);
        embedBuilder.setTimestamp(new Date().toInstant());
        embedBuilder.setColor(0x2F3136);
        embedBuilder.setFooter(guildName, guildURL);
    }
}
