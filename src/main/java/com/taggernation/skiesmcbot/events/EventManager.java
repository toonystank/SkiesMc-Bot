package com.taggernation.skiesmcbot.events;

import com.taggernation.skiesmcbot.Meme;
import com.taggernation.skiesmcbot.WebHookChat;
import com.taggernation.skiesmcbot.utils.EmbedFromYML;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
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
    private final Meme meme;
    private final List<EmbedFromYML> embeds;
    private final WebHookChat webHookChat;

    public EventManager(SuggestionEvent suggestion, CommandEvent commandEvent, AnnouncementEvent announcementEvent, List<EmbedFromYML> embeds, Meme meme, WebHookChat webHookChat) {
        this.suggestion = suggestion;
        this.command = commandEvent;
        this.announcement = announcementEvent;
        this.embeds = embeds;
        this.meme = meme;
        this.webHookChat = webHookChat;
        Bukkit.getLogger().info("EventManager initialized.");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        command.onCommand(event);
        embeds.forEach(embed -> embed.sendEmbed(event));
        meme.onInfo(event);
        meme.onLeaderBoard(event);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        command.onCommand(event);
        try {
            suggestion.onSuggestionSend(event);
            announcement.onAnnouncementMessage(event);
            embeds.forEach(embed -> embed.sendEmbed(event));
            meme.onMeme(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Long channelID = webHookChat.getOtherChannelID();
        if (channelID.equals(event.getChannel().getIdLong())) {
            webHookChat.sendWebHook(event.getMessage().getContentRaw(), event.getAuthor().getAvatarUrl(), event.getAuthor().getName(), false, false);
        }
    }
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (Objects.requireNonNull(event.getUser()).isBot()) return;
        try {
            suggestion.onSuggestionReactionAdd(event);
            meme.onReact(event);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        meme.onUnReact(event);
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
