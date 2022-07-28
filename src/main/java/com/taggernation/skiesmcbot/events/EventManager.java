package com.taggernation.skiesmcbot.events;

import com.taggernation.skiesmcbot.utils.EmbedFromYML;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Objects;

public class EventManager extends ListenerAdapter {

    private final SuggestionEvent suggestion;
    private final CommandEvent command;
    private final AnnouncementEvent announcement;
    private final List<EmbedFromYML> embeds;

    public EventManager(SuggestionEvent suggestion, CommandEvent commandEvent, AnnouncementEvent announcementEvent, List<EmbedFromYML> embeds) {
        this.suggestion = suggestion;
        this.command = commandEvent;
        this.announcement = announcementEvent;
        this.embeds = embeds;
        Bukkit.getLogger().info("EventManager initialized.");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        command.onCommand(event);
        try {
            suggestion.onSuggestionSend(event);
            announcement.onAnnouncementMessage(event);
            embeds.forEach(embed -> embed.sendEmbed(event));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (Objects.requireNonNull(event.getUser()).isBot()) return;
        try {
            suggestion.onSuggestionReactionAdd(event);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        embeds.forEach(embed -> embed.sendEmbed(event));
    }
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        embeds.forEach(embed -> embed.sendEmbed(event));
    }
}
