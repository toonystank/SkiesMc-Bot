package com.taggernation.skiesmcbot.events;

import com.taggernation.skiesmcbot.utils.DefaultEmbed;
import com.taggernation.skiesmcbot.utils.SuggestionManager;
import com.taggernation.taggernationlib.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SuggestionEvent extends ListenerAdapter {

    private final Config config;
    private final SuggestionManager suggestionManager;
    public SuggestionEvent(Config config, SuggestionManager suggestionManager) {
        this.config = config;
        this.suggestionManager = suggestionManager;
    }
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!(event.getChannel() instanceof TextChannel)) return;
        if (!event.getTextChannel().getId().equals(config.getString("suggestion_channel"))) return;
        if (event.getAuthor().isBot()) {
            return;
        }
        event.getMessage().delete().queue();
        EmbedBuilder embed = new EmbedBuilder();
        DefaultEmbed.setDefault(embed,event);
        suggestionManager.sendSuggestionEmbed(embed, event.getTextChannel(), event.getMessage().getContentRaw(), event.getAuthor().getId());
    }
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!(event.getChannel() instanceof TextChannel)) return;
        if (!(event.getChannel() instanceof TextChannel)) return;
        if (!event.getTextChannel().getId().equals(config.getString("suggestion_channel"))) return;
        User user = event.getUser();
        assert user != null;
        if (user.isBot()) return;
        EmbedBuilder embed = new EmbedBuilder();
        suggestionManager.changeSuggestionStatus(embed, event.getTextChannel(),event.getReaction(),event.getMessageId(),event.getMember());

    }
}
