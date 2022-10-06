package com.taggernation.skiesmcbot.utils;

import com.taggernation.taggernationlib.config.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.util.Date;

@SuppressWarnings("unused")
public class SuggestionManager {

    private final ConfigManager Suggestions;
    private final JDA jda;

    public SuggestionManager(ConfigManager Suggestions, JDA jda) {
        this.Suggestions = Suggestions;
        this.jda = jda;
    }

    public void saveToYML(String suggestion, String author, String messageID) {
        Suggestions.set("suggestions." + messageID + ".TEXT", suggestion);
        Suggestions.set("suggestions." + messageID + ".AUTHOR", author);
        Suggestions.set("suggestions." + messageID + ".STATUS", SuggestionStatus.PENDING.toString());
    }
    public void removeFromYML(String messageID) {
        if (isSuggestionExist(messageID)) {
            Suggestions.set("suggestions." + messageID, null);
        }
    }
    public String getData(String messageID, DataType dataType) {
        return Suggestions.getString("suggestions." + messageID + "." + dataType.toString());
    }
    public void changeSuggestionStatus(EmbedBuilder embed, Guild guild, MessageChannel channel, MessageReaction reaction, String messageID, Member member) {
        if (isSuggestionExist(messageID)) {
            String suggestionAuthorID = Suggestions.getString("suggestions." + messageID + ".AUTHOR");
            User suggestionAuthorName = jda.retrieveUserById(suggestionAuthorID).complete();
            if (reaction.getEmoji().getName().equals("✅")) {
                reaction.removeReaction(member.getUser()).queue();
                embed.setTitle(suggestionAuthorName.getName() + "'s Suggestion ➝ :stars: Accepted").setDescription(getData(messageID,DataType.TEXT));
                checkEmbed(embed, guild, channel, messageID, member,SuggestionStatus.ACCEPTED);
                Suggestions.set("suggestions." + messageID + ".STATUS", SuggestionStatus.ACCEPTED.toString());
            }
            if (reaction.getEmoji().getName().equals("❌")) {
                reaction.removeReaction(member.getUser()).queue();
                embed.setTitle("❌ " +  suggestionAuthorName.getName() + "'s Suggestion ➝ :scissors: Rejected").setDescription(getData(messageID,DataType.TEXT));
                checkEmbed(embed, guild, channel, messageID, member,SuggestionStatus.DENIED);
                Suggestions.set("suggestions." + messageID + ".STATUS", SuggestionStatus.DENIED.toString());
            }
        }
    }
    public void checkEmbed(EmbedBuilder embed,Guild guild, MessageChannel channel, String messageID, Member member, SuggestionStatus status) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) return;
        if (status == SuggestionStatus.ACCEPTED) {
            embed.setAuthor("Accepted by " + member.getUser().getName(), null, member.getUser().getAvatarUrl());
        }else if (status == SuggestionStatus.DENIED) {
            embed.setAuthor("Rejected by " + member.getUser().getName(), null, member.getUser().getAvatarUrl());
        }
        embed.setTimestamp(new Date().toInstant());
        embed.setFooter(guild.getName(), guild.getIconUrl());
        embed.setColor(Color.BITMASK);
        channel.editMessageEmbedsById(messageID, embed.build()).queue();
    }


    public boolean isSuggestionExist(String messageID) {
        return Suggestions.get("suggestions." + messageID) != null;
    }
    public void sendSuggestionEmbed(EmbedBuilder embed, MessageChannel channel, String suggestion, String author) {
        embed.setTitle(":rocket: New Suggestion");
        embed.setDescription(suggestion);
        RestAction<Message> ra = channel.sendMessageEmbeds(embed.build());
        Message message = ra.complete();
        message.addReaction(Emoji.fromFormatted("⬆️")).queue();
        message.addReaction(Emoji.fromFormatted("⬇️")).queue();
        message.addReaction(Emoji.fromFormatted("✅")).queue();
        message.addReaction(Emoji.fromFormatted("❌")).queue();
        saveToYML(suggestion, author, message.getId());
    }
    enum DataType {
        TEXT, AUTHOR, STATUS
    }
    enum SuggestionStatus {
        PENDING,
        ACCEPTED,
        DENIED
    }
}
