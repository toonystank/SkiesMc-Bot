package com.taggernation.skiesmcbot;

import com.taggernation.skiesmcbot.utils.DefaultEmbed;
import com.taggernation.taggernationlib.config.ConfigManager;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.*;

public class Meme extends ConfigManager {

    @Getter
    private final ConfigManager memeData;
    private boolean isAttachment = false;
    private final Map<User, MemeData> userMemeDataMap = new HashMap<>();
    private Map<String, Integer> sortedUpVoteMap = new HashMap<>();
    private Map<String, Integer> sortedDownVoteMap = new HashMap<>();
    private Map<String, Integer> sortedNoReactionMap = new HashMap<>();
    private final JDA jda;

    public Meme(Plugin plugin, JDA jda) throws IOException {
        super(plugin, "meme.yml", false, true);
        this.memeData = new ConfigManager(plugin, "memedata.yml", "data", false, false);
        this.jda = jda;
        jda.upsertCommand(
                this.getString("leaderboard.command"), this.getString("leaderboard.description"))
                        .addOptions(
                                new OptionData(OptionType.STRING, "type", "The type of leaderboard", false)
                                        .addChoice("Up vote", "upvote")
                                        .addChoice("Down vote", "downvote")
                                        .addChoice("No reaction", "noreaction"))
                .queue();
        jda.upsertCommand(
                this.getString("memeinfo.command"), this.getString("memeinfo.description"))
                .addOption(OptionType.USER,"username", "Username of the user to get info", true)
                .queue();
        setUserMemeDataMap();
        updateLeaderBoardInfo();
    }

    public void onMeme(MessageReceivedEvent event) {
        if (!event.getChannel().getId().equals(this.getString("channel_id")))
            return;
        if (!event.getMessage().getAttachments().isEmpty())
            isAttachment = true;
        event.getMessage().delete().queue();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(formatPlaceholders(this.getString("title"), event.getChannel(), Objects.requireNonNull(event.getMember()).getUser()));
        if (isAttachment) {
            embed.setImage(event.getMessage().getAttachments().get(0).getUrl());
        }
        embed.setDescription(event.getMessage().getContentRaw());
        DefaultEmbed.setDefault(embed,event.getMember().getAvatarUrl(),event.getMember().getEffectiveName(),event.getGuild().getName(),event.getGuild().getIconUrl());
        addReaction(embed, event.getTextChannel(), event.getMember().getUser());
        this.isAttachment = false;
    }
    void addReaction(EmbedBuilder embed, TextChannel channel, User user) {
        RestAction<Message> ra = channel.sendMessageEmbeds(embed.build());
        Message message = ra.complete();
        MemeData data = new MemeData(SkiesMCBOT.getInstance(), this, jda, user);
        message.addReaction(this.getString("reactions.upvote")).queue();
        data.setData(MemeData.Reactions.UPVOTE, 0);
        message.addReaction(this.getString("reactions.noreaction")).queue();
        data.setData(MemeData.Reactions.NO_REACTION, 0);
        message.addReaction(this.getString("reactions.downvote")).queue();
        data.setData(MemeData.Reactions.DOWN_VOTE, 0);
        data.setData(MemeData.Reactions.EMBED_ID, message.getId());
        userMemeDataMap.put(user, data);
    }
    public void onReact(MessageReactionAddEvent event) {
        Bukkit.getLogger().info("react event");
        if (userMemeDataMap.containsKey(event.getUser())) {
            if (userMemeDataMap.get(event.getUser()).setUpdated().getEmbedIDS().contains(event.getMessageId())) {
                return;
            }
        }
        userMemeDataMap.forEach((user, memeData1) -> memeData1.setUpdated().getEmbedIDS().forEach(ids -> {
            if (!ids.equals(event.getMessageId())) return;
            if (event.getReactionEmote().getName().equals(this.getString("reactions.upvote")))
                memeData1.addReaction(MemeData.Reactions.UPVOTE);
            if (event.getReactionEmote().getName().equals(this.getString("reactions.downvote")))
                memeData1.addReaction(MemeData.Reactions.DOWN_VOTE);
            if (event.getReactionEmote().getName().equals(this.getString("reactions.noreaction")))
                memeData1.addReaction(MemeData.Reactions.NO_REACTION);
        }));

    }
    public void onUnReact(MessageReactionRemoveEvent event) {
        if (userMemeDataMap.containsKey(event.getUser())) {
            if (userMemeDataMap.get(event.getUser()).setUpdated().getEmbedIDS().contains(event.getMessageId())) {
                return;
            }
        }
        userMemeDataMap.forEach((user, memeData1) -> memeData1.setUpdated().getEmbedIDS().forEach(ids -> {
            if (!ids.equals(event.getMessageId())) return;
            if (event.getReactionEmote().getName().equals(this.getString("reactions.upvote")))
                memeData1.removeReaction(MemeData.Reactions.UPVOTE);
            if (event.getReactionEmote().getName().equals(this.getString("reactions.downvote")))
                memeData1.removeReaction(MemeData.Reactions.DOWN_VOTE);
            if (event.getReactionEmote().getName().equals(this.getString("reactions.noreaction")))
                memeData1.removeReaction(MemeData.Reactions.NO_REACTION);
        }));

    }
    public void onLeaderBoard(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(this.getString("leaderboard.command"))) return;
        event.deferReply().queue();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(formatPlaceholders(this.getString("leaderboard.title"), jda.getTextChannelById(this.getString("channel_id")), event.getUser()));
        DefaultEmbed.setDefault(embed, Objects.requireNonNull(event.getMember()).getAvatarUrl(), event.getMember().getEffectiveName(), Objects.requireNonNull(event.getGuild()).getName(), event.getGuild().getIconUrl());
        updateLeaderBoardInfo();
        List<String> stringList = new ArrayList<>();
        if (event.getOptions().isEmpty()) {
            String format = this.getString("leaderboard.format.default");
            this.sortedUpVoteMap.forEach((s, integer) -> {
                RestAction<User> restUser = jda.retrieveUserById(s);
                User user = restUser.complete();
                stringList.add(formatPlaceholders(format, event.getChannel(), user));
            });
            setFormatData(embed, stringList, event);
        }
        else if (Objects.requireNonNull(event.getOption("type")).getAsString().equals("upvote")) {
            String format = this.getString("leaderboard.format.upvote");
            this.sortedUpVoteMap.forEach((s, integer) -> processSortedList(stringList, s, format, event.getChannel()));
            setFormatData(embed, stringList, event);
        }
        else if (Objects.requireNonNull(event.getOption("type")).getAsString().equals("downvote")) {
            String format = this.getString("leaderboard.format.downvote");
            this.sortedDownVoteMap.forEach((s, integer) -> processSortedList(stringList, s, format, event.getChannel()));
            setFormatData(embed, stringList, event);
        }
        else if (Objects.requireNonNull(event.getOption("type")).getAsString().equals("noreaction")) {
            String format = this.getString("leaderboard.format.noreaction");
            this.sortedNoReactionMap.forEach((s, integer) -> processSortedList(stringList, s, format, event.getChannel()));
            setFormatData(embed, stringList, event);
        }
    }

    public void processSortedList(List<String> stringList, String s, String format, Channel channel) {
        RestAction<User> restUser = jda.retrieveUserById(s);
        User user = restUser.complete();
        stringList.add(formatPlaceholders(format, channel, user));
    }
    public void onInfo(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(this.getString("memeinfo.command"))) return;
        event.deferReply().queue();
        EmbedBuilder embed = new EmbedBuilder();
        DefaultEmbed.setDefault(embed, Objects.requireNonNull(event.getMember()).getAvatarUrl(),event.getMember().getEffectiveName(), Objects.requireNonNull(event.getGuild()).getName(),event.getGuild().getIconUrl());
        User user = Objects.requireNonNull(event.getOption("username")).getAsUser();

        if (!userMemeDataMap.containsKey(user)) {
            embed.setTitle("User not found");
            embed.setDescription("User dose not have any records");
            event.getHook().sendMessageEmbeds(embed.build()).queue();
            return;
        }
        embed.setTitle(formatPlaceholders(this.getString("memeinfo.format.title"),event.getChannel(), user));
        embed.setDescription(formatPlaceholders(
                        String.join("/n", this.getStringList("memeinfo.format.description")),
                        event.getChannel(),
                        user
                ));
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    public void setFormatData(EmbedBuilder embed, List<String> stringList, SlashCommandInteractionEvent event) {
        if (stringList.isEmpty()) {
            embed.setTitle("LeaderBoard failed to process");
            embed.setDescription("No users found");
            event.getHook().sendMessageEmbeds(embed.build()).queue();
            return;
        }
        embed.setDescription(String.join("\n", stringList));
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }
    public void updateLeaderBoardInfo() {
        final Map<String, Integer> upVoteMap = new HashMap<>();
        final Map<String, Integer> downVoteMap = new HashMap<>();
        final Map<String, Integer> noReactionMap = new HashMap<>();
        Objects.requireNonNull(memeData.getConfig().getConfigurationSection("data")).getKeys(false).forEach(ids -> {
            RestAction<User> restUser = jda.retrieveUserById(ids);
            User user = restUser.complete();
            if (user == null) {
                return;
            }
            MemeData memeData = new MemeData(SkiesMCBOT.getInstance(), this,jda, user);
            upVoteMap.put(user.getId(), memeData.setUpdated().getUpVotes());
            downVoteMap.put(user.getId(), memeData.setUpdated().getDownVotes());
            noReactionMap.put(user.getId(), memeData.setUpdated().getNoReactions());
        });
        this.sortedUpVoteMap = sortByValue(upVoteMap);
        this.sortedDownVoteMap = sortByValue(downVoteMap);
        this.sortedNoReactionMap = sortByValue(noReactionMap);
    }
    public void setUserMemeDataMap() {
        Objects.requireNonNull(memeData.getConfig().getConfigurationSection("data")).getKeys(false).forEach(ids -> {
            RestAction<User> restUser = jda.retrieveUserById(ids);
            User user = restUser.complete();
            MemeData data = new MemeData(SkiesMCBOT.getInstance(), this,jda, user);
            data.setMember(user);
            data.setUpdated();
            userMemeDataMap.put(jda.getUserById(ids), data);
        });
    }
    public static Map<String, Integer> sortByValue(Map<String, Integer> unsortedMap) {

        List<Map.Entry<String, Integer>> list =
                new LinkedList<>(unsortedMap.entrySet());

        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public String formatPlaceholders(String message, Channel channel, User user) {
        Bukkit.getLogger().info(message + " user " + user);
        if (user == null) {
            return message;
        }
        MemeData data = null;
        try {
            data = new MemeData(SkiesMCBOT.getInstance(),this,jda,user);
        } catch (Exception ignored) {}
        if (message.contains("{user}"))
            message = message.replace("{user}", user.getName());
        if (message.contains("{mentionchannel}"))
            message =  message.replace("{mentionchannel}", channel.getName());
        if (message.contains("{upvotes}")) {
            assert data != null;
            message =  message.replace("{upvotes}", data.setUpdated().getUpVotes() + "");
        }
        if (message.contains("{noreactions}")) {
            assert data != null;
            message =  message.replace("{noreactions}", data.setUpdated().getNoReactions() +"");
        }
        if (message.contains("{downvotes}")) {
            assert data != null;
            message =  message.replace("{downvotes}", data.setUpdated().getDownVotes() +"");
        }
        return message;
    }
}
