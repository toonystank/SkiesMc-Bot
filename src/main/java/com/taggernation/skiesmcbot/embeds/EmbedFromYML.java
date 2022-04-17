package com.taggernation.skiesmcbot.embeds;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.skiesmcbot.commands.HelpEmbed;
import com.taggernation.skiesmcbot.tasks.LoopTask;
import com.taggernation.taggernationlib.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.Set;

public class EmbedFromYML {

    private final String command;
    private final String title;
    private final String author;
    private final List<String> description;
    private final String color;
    private final Set<String> fields;
    private final String footer;
    private final String thumbnail;
    private final String authorName;
    private final String authorIconURL;
    private final String authorURL;

    private final String footerText;
    private final String footerIconURL;

    private final EmbedBuilder embedBuilder = new EmbedBuilder();
    private MessageEmbed messageEmbed;
    private final JDA jda;
    private LoopTask loopTask;
    private Config config;

    public EmbedFromYML(Config config, JDA jda, LoopTask loopTask) {
        this.jda = jda;
        this.config = config;
        this.loopTask = loopTask;
        this.command = config.getString("command");
        this.title = config.getString("title");
        this.author = config.getString("author");
        this.authorName = config.getString("author.name");
        this.authorIconURL = config.getString("author.icon");
        this.authorURL = config.getString("author.url");
        this.description = config.getStringList("description");
        this.color = config.getString("color");
        this.fields = config.getConfig().getConfigurationSection("fields").getKeys(false);
        this.footer = config.getString("footer");
        this.footerText = config.getString("footer.text");
        this.footerIconURL = config.getString("footer.icon");
        this.thumbnail = config.getString("thumbnail");
    }
    /**
     * get the command from yml
     * @return String command from yml
     */
    public String getCommand() {
        return command;
    }
    /**
     * Finalize the embed and embed command
     */
    public EmbedFromYML build() {
        if (title != null) {
            embedBuilder.setTitle(title);
        }
        if (author != null) {
            embedBuilder.setAuthor(authorName, authorURL, authorIconURL);
        }
        if (description != null) {
            embedBuilder.setDescription(String.join("\n", description));
        }
        if (color != null) {
            embedBuilder.setColor(Integer.parseInt(color));
        }
        if (fields != null) {
            fields.forEach(field -> {
                embedBuilder.addField(config.getString("fields." + field + ".name"), config.getString("fields." + field + ".value"), config.getBoolean( "fields." + field + ".inline"));
                    });
        }else {
            SkiesMCBOT.getInstance().getLogger().info("No fields found");
        }
        if (footer != null) {
            embedBuilder.setFooter(footerText, footerIconURL);
        }
        if (thumbnail != null) {
            embedBuilder.setThumbnail(thumbnail);
        }
        messageEmbed = embedBuilder.build();
        if (command != null) {
            jda.addEventListener(new Commands(this));
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
}
