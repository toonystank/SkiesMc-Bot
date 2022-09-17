package com.taggernation.skiesmcbot.utils;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.skiesmcbot.commands.HelpEmbed;
import com.taggernation.taggernationlib.config.ConfigManager;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class EmbedFromYML {

    private String command;
    private String commandDescription;
    private String channel;
    private TriggerMode type;
    private String message;
    private String joinRank = null;

    private String title;
    private List<String> description;
    private Set<String> fieldsData = new HashSet<>();
    private ThumbnailType thumbnail = null;
    private String authorName;
    private String authorIconURL;
    private String authorURL;
    private String attachmentURL;

    private String footerText;
    private String footerIconURL;

    private EmbedBuilder embedBuilder = new EmbedBuilder();
    private MessageEmbed messageEmbed;
    private final JDA jda;
    private final ConfigManager config;

    public EmbedFromYML(ConfigManager config, JDA jda) {
        this.jda = jda;
        this.config = config;
        initialize();
        Bukkit.getLogger().info("EmbedFromYaml loaded");
    }

    /**
     * get the channel
     * @return String channel
     */
    public String getChannel() {
        return channel;
    }
    /**
     * get the config
     * @return ConfigManager config
     */
    public ConfigManager getConfig() {
        return config;
    }
    /**
     * get the jda
     * @return JDA jda
     */
    public JDA getJDA() {
        return jda;
    }
    /**
     * get the command from yml
     * @return String command from yml
     */
    public String getCommand() {
        return command;
    }
    /**
     * get the message from yml
     * @return String message from yml
     */
    public String getMessage() {
        return message;
    }
    /**
     * get the type from yml
     * @return String type from yml
     */
    public TriggerMode getType() {
        return type;
    }
    /**
     * Finalize the embed and embed command
     */
    public EmbedFromYML build(@Nullable Member member) {
        if (title != null) {
            embedBuilder.setTitle(formatMessage(title, member));
        }
        try {
            embedBuilder.setAuthor(formatMessage(authorName, member), authorURL, formatMessage(authorIconURL, member));
        }catch (IllegalArgumentException ignored) {
        }
        if (attachmentURL != null) {
            embedBuilder.setImage(attachmentURL);
        }
        if (footerText != null) {
            embedBuilder.setFooter(formatMessage(footerText,member),formatMessage(footerIconURL,member));
        }
        if (description != null) {
            embedBuilder.setDescription(String.join("\n", formatDescription(member)));
        }
        embedBuilder.setColor(0x2F3136);
        if (!fieldsData.isEmpty()) {
            fieldsData.forEach(field -> embedBuilder.addField(
                    formatMessage(config.getString("fields-data." + field + ".name"), member)
                    ,formatMessage(config.getString("fields-data." + field + ".value"),member)
                    ,config.getBoolean( "fields-data." + field + ".inline")));
        }else {
            SkiesMCBOT.getInstance().getLogger().info("No fields found");
        }
        embedBuilder.setFooter(footerText, footerIconURL);
        if (thumbnail != null) {
            if (thumbnail == ThumbnailType.URL) {
                embedBuilder.setThumbnail(config.getString("thumbnailurl"));
            }
        }else {
            if (member != null) {
                embedBuilder.setThumbnail(member.getUser().getAvatarUrl());
            }
        }
        messageEmbed = embedBuilder.build();
        return this;
    }
    public void demolish() {
        embedBuilder.clear();
        embedBuilder = new EmbedBuilder();
        initialize();
    }
    public void initialize() {
        this.type = TriggerMode.valueOf(config.getString("trigger.event.type"));
        switch (type) {
            case COMMAND -> {
                this.command = config.getString("trigger.event.command");
                this.commandDescription = config.getString("trigger.event.description");
                jda.upsertCommand(this.command, this.commandDescription).queue();
            }
            case LEAVE, JOIN -> {
                this.channel = config.getString("trigger.event.channel");
                this.joinRank = config.getString("trigger.event.rank");

            }
            case MESSAGE -> this.message = config.getString("trigger.event.message").toLowerCase(Locale.ROOT);
        }

        this.title = config.getString("title");
        this.authorName = config.getString("author.name");
        this.authorIconURL = config.getString("author.icon");
        this.authorURL = config.getString("author.url");
        this.description = config.getStringList("description");
        this.attachmentURL = config.getString("attachment");

        boolean fields = config.getBoolean("fields");
        if (fields) {
            this.fieldsData = Objects.requireNonNull(config.getConfig().getConfigurationSection("fields-data")).getKeys(false);
        }

        this.footerText = config.getString("footer.text");
        this.footerIconURL = config.getString("footer.icon");
        try {
            this.thumbnail = ThumbnailType.valueOf(config.getString("thumbnail"));
        }catch (IllegalArgumentException | NullPointerException ignored) {
        }
    }
    public EmbedFromYML registerEvents() {
        if (type == TriggerMode.COMMAND) {
            SkiesMCBOT.getInstance().getLogger().info("Registered command: " + command);
            if (!HelpEmbed.getCommands().contains(command)) {
                HelpEmbed.addCommand(command);
            }
        }
        return this;
    }
    /**
     * Send the embed to the channel
     * @param channelID the channel id
     */
    public void sendEmbed(String channelID) {
        if (messageEmbed == null) {
            return;
        }
        TextChannel textChannel = jda.getTextChannelById(channelID);
        if (textChannel == null) {
            return;
        }
        textChannel.sendMessageEmbeds(messageEmbed).queue();
    }
    /**
     * get the embed
     * return "unknown command" if the embed is not build
     */
    public MessageEmbed getEmbed() {
        if (messageEmbed == null) {
            embedBuilder.setTitle("Unknown Command");
            embedBuilder.setDescription("Please use `!help` to see a list of commands.");
            return embedBuilder.build();
        }
        return messageEmbed;
    }
    public List<String> formatDescription(@Nullable Member member) {
        if (member == null) {
            return description;
        }
        this.description = description.stream().map(s -> formatMessage(s, member)).collect(Collectors.toList());
        return this.description;
    }
    public String formatMessage(String message, @Nullable Member member) {
        if (message == null) return null;
        if (member == null) {
            return message;
        }
        if (message.contains("{user}")) return message.replace("{user}", member.getUser().getName());
        if (message.contains("{useravatar}")) return member.getAvatarUrl();
        if (message.contains("{membercount}")) return message.replace("{membercount}", String.valueOf(member.getGuild().getMemberCount()));
        return message;
    }

    private void joinAndLeave() {
        TextChannel textChannel = getJDA().getTextChannelById(getChannel());
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(getEmbed()).queue();
            demolish();
        }else {
            throw new IllegalArgumentException("Provided channel ID dose not exist for join/leave event in " + getConfig().getFile().getName());
        }
    }
    private void addRole(Member member) {
        Role role = getJDA().getRoleById(this.joinRank);
        if (role != null) {
            member.getGuild().addRoleToMember(member, role).queue();
        }
    }
    public void sendEmbed(GuildMemberJoinEvent event) {
        if (getType() == EmbedFromYML.TriggerMode.JOIN) {
            build(event.getMember());
            joinAndLeave();
            addRole(event.getMember());
            demolish();
        }
    }
    public void sendEmbed(GuildMemberRemoveEvent event) {
        if (getType() == EmbedFromYML.TriggerMode.LEAVE) {
            build(event.getMember());
            joinAndLeave();
            demolish();
        }
    }
    public void sendEmbed(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if ((getType() == TriggerMode.JOIN) || (getType() == TriggerMode.LEAVE)) return;
        if (getType() == TriggerMode.MESSAGE) {
            if (!message.equals(event.getMessage().getContentRaw())) return;
            build(event.getMember());
            if (!event.getMessage().getContentRaw().toLowerCase(Locale.ROOT).contains(getMessage())) {
                return;
            }
            event.getChannel().sendMessageEmbeds(getEmbed()).queue();
            demolish();
        }
    }
    public void sendEmbed(SlashCommandInteractionEvent event) {
        if (getType() == TriggerMode.COMMAND) {
            if (!event.getName().equals(this.command)) return;
            build(event.getMember());
            event.replyEmbeds(getEmbed()).queue();
            demolish();
        }
    }
    public enum TriggerMode{
        COMMAND,
        JOIN,
        LEAVE,
        MESSAGE
    }
    public enum ThumbnailType{
        URL
    }
}
