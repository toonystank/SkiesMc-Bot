package com.taggernation.skiesmcbot.events;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import com.taggernation.skiesmcbot.utils.DefaultEmbed;
import com.taggernation.skiesmcbot.utils.SuggestionManager;
import com.taggernation.taggernationlib.config.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class SuggestionEvent {

    private final ConfigManager mainConfig;
    private final SuggestionManager suggestionManager;
    public SuggestionEvent(ConfigManager config, SuggestionManager suggestionManager) {
        this.mainConfig = config;
        this.suggestionManager = suggestionManager;
    }

    public void onSuggestionSend(@NotNull MessageReceivedEvent event) throws IOException {
        if (!(event.getChannel() instanceof TextChannel)) return;
        if (!event.getTextChannel().getId().equals(mainConfig.getString("suggestion.channel"))) return;
        String[] args = event.getMessage().getContentRaw().split(" ");
        if (args.length < 2) return;
        event.getMessage().delete().queue();
        if (args[0].equalsIgnoreCase("!Message")) {
            if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) return;
            String message = event.getMessage().getContentRaw().replace("!message ", "");
            InputStream image = new URL(Objects.requireNonNull(event.getAuthor().getAvatarUrl())).openStream();
            Icon icon = Icon.from(image);
            Webhook webhook = ((TextChannel) event.getChannel()).createWebhook(event.getAuthor().getName()).setAvatar(icon).complete();
            try (JDAWebhookClient client = JDAWebhookClient.from(webhook)) {
                client.send(message);
            }
            return;
        }
        EmbedBuilder embed = new EmbedBuilder();
        DefaultEmbed.setDefault(embed, event.getAuthor().getAvatarUrl(), event.getAuthor().getName(), event.getGuild().getName(), event.getGuild().getIconUrl());
        addThumbnail(embed);
        suggestionManager.sendSuggestionEmbed(embed, event.getTextChannel(), event.getMessage().getContentRaw(), event.getAuthor().getId());
    }
    public void onSuggestionReactionAdd(@NotNull MessageReactionAddEvent event) throws MalformedURLException {
        if (!(event.getChannel() instanceof TextChannel)) return;
        if (!(event.getChannel() instanceof TextChannel)) return;
        if (!event.getTextChannel().getId().equals(mainConfig.getString("suggestion.channel"))) return;
        EmbedBuilder embed = new EmbedBuilder();
        addThumbnail(embed);
        suggestionManager.changeSuggestionStatus(embed, event.getTextChannel(),event.getReaction(),event.getMessageId(),event.getMember());
    }
    private void addThumbnail(EmbedBuilder embed) {
        if (mainConfig.getString("suggestion.thumbnail").equals("NONE")) {
            embed.setThumbnail("https://cdn.discordapp.com/attachments/958642856246009877/967472092364832819/Pngtreeillustration_of_rocket_for_start_6930738.png");
        } else {
            embed.setThumbnail(mainConfig.getString("suggestion.thumbnail"));
        }
    }
}
