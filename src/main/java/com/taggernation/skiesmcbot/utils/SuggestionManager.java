package com.taggernation.skiesmcbot.utils;

import com.taggernation.taggernationlib.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import org.bukkit.Bukkit;

import java.util.Date;
import java.util.Objects;

public class SuggestionManager {

    private final Config Suggestions;
    private final JDA jda;

    public SuggestionManager(Config Suggestions, JDA jda) {
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
    public void changeSuggestionStatus(EmbedBuilder embed, TextChannel channel, MessageReaction reaction, String messageID, Member member) {
        if (isSuggestionExist(messageID)) {
            String suggestionAuthorID = Suggestions.getString("suggestions." + messageID + ".AUTHOR");
            User suggestionAuthorName = jda.retrieveUserById(suggestionAuthorID).complete();
            if (reaction.getReactionEmote().getName().equals("✅")) {
                reaction.removeReaction(member.getUser()).queue();
                embed.setTitle("✅" +  suggestionAuthorName.getName() + "'s Suggestion ➝ Accepted").setDescription(getData(messageID,DataType.TEXT));
                checkEmbed(embed, channel, messageID, member,SuggestionStatus.ACCEPTED);
                Suggestions.set("suggestions." + messageID + ".STATUS", SuggestionStatus.ACCEPTED.toString());
            }
            if (reaction.getReactionEmote().getName().equals("❌")) {
                reaction.removeReaction(member.getUser()).queue();
                embed.setTitle("❌" +  suggestionAuthorName.getName() + "'s Suggestion ➝ Rejected").setDescription(getData(messageID,DataType.TEXT));
                checkEmbed(embed, channel, messageID, member,SuggestionStatus.DENIED);
                Suggestions.set("suggestions." + messageID + ".STATUS", SuggestionStatus.DENIED.toString());
            }
        }
    }
    public void checkEmbed(EmbedBuilder embed, TextChannel channel, String messageID, Member member, SuggestionStatus status) {
        if (!member.hasPermission(Permission.MANAGE_CHANNEL)) return;
        if (status == SuggestionStatus.ACCEPTED) {
            embed.setAuthor("Accepted by " + member.getUser().getName(), null, member.getUser().getAvatarUrl());
        }else if (status == SuggestionStatus.DENIED) {
            embed.setAuthor("Rejected by " + member.getUser().getName(), null, member.getUser().getAvatarUrl());
        }
        embed.setTimestamp(new Date().toInstant());
        embed.setFooter(channel.getGuild().getName(), channel.getGuild().getIconUrl());
        channel.editMessageEmbedsById(messageID, embed.build()).queue();
    }


    public boolean isSuggestionExist(String messageID) {
        return Suggestions.get("suggestions." + messageID) != null;
    }
    public void sendSuggestionEmbed(EmbedBuilder embed, TextChannel channel, String suggestion, String author) {
        embed.setTitle(":scroll: New Suggestion");
        embed.setDescription(suggestion);
        RestAction<Message> ra = channel.sendMessageEmbeds(embed.build());
        Message message = ra.complete();
        message.addReaction("⬆️").queue();
        message.addReaction("⬇️").queue();
        message.addReaction("✅").queue();
        message.addReaction("❌").queue();
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
