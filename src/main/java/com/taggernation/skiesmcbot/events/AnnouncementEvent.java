package com.taggernation.skiesmcbot.events;

import com.taggernation.skiesmcbot.utils.DefaultEmbed;
import com.taggernation.taggernationlib.config.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AnnouncementEvent {

    private final ConfigManager mainConfig;

    public AnnouncementEvent(ConfigManager mainConfig) {
        this.mainConfig = mainConfig;
    }

    public void onAnnouncementMessage(@NotNull MessageReceivedEvent event) throws MalformedURLException {
        if (!event.getChannel().getId().equals(mainConfig.getString("announcement.channel"))) return;
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR)) return;
        List<String> args = Arrays.asList(event.getMessage().getContentRaw().toLowerCase().split(" "));
        if (args.size() < 2) return;
        EmbedBuilder embed = new EmbedBuilder();
        DefaultEmbed.setDefault(embed, event.getAuthor().getAvatarUrl(), event.getAuthor().getName(), event.getGuild().getName(), event.getGuild().getIconUrl());
        event.getMessage().delete().queue();
        embed.setTitle(":speaking_head: Announcement");
        embed.setDescription(event.getMessage().getContentRaw());
        embed.setColor(0x2F3136);
        if (!event.getMessage().getAttachments().isEmpty()) {
            embed.setImage(event.getMessage().getAttachments().get(0).getUrl());
        }
        if (!args.contains("-nothumbnail")) {
            if (mainConfig.getString("announcement.thumbnail").equals("NONE")) {
                embed.setThumbnail("https://cdn.discordapp.com/attachments/958642856246009877/967472704439586886/Pngtreehand_drawn_red_speaker_megaphone_5415133.png");
            } else {
                embed.setThumbnail(mainConfig.getString("announcement.thumbnail"));
            }
        }
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}
