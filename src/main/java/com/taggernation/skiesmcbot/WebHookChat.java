package com.taggernation.skiesmcbot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.taggernation.skiesmcbot.events.CommandEvent;
import com.taggernation.skiesmcbot.utils.EmbedFromYML;
import com.taggernation.skiesmcbot.utils.Placeholder;
import com.taggernation.taggernationlib.config.ConfigManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class WebHookChat extends ListenerAdapter implements Listener {

    private final Plugin plugin;
    private JDA jda;
    private final JDA otherJDA;
    private final ConfigManager configManager;
    private final Long webHookID;
    private final Long otherWebHookID;
    private final Long guildID;
    private final Long channelID;
    @Getter
    private final Long otherChannelID;
    private final String messageFormat;
    private final String joinFormat;
    private final String leaveFormat;
    private final Set<String> commands;
    private WebhookClient client;
    private WebhookClient otherClient;
    private final List<EmbedFromYML> embeds;
    private final CommandEvent command;
    private Guild guild;
    private TextChannel channel;
    private TextChannel otherChannel;

    public WebHookChat(Plugin plugin, List<EmbedFromYML> embeds, CommandEvent command, JDA otherJDA) throws IOException {
        configManager = new ConfigManager(plugin, "discordHook.yml", false, true);
        this.plugin = plugin;
        this.embeds = embeds;
        this.command = command;
        this.webHookID = configManager.getConfig().getLong("hookid");
        this.otherWebHookID = configManager.getConfig().getLong("otherhookid");
        this.guildID = configManager.getConfig().getLong("guild");
        Bukkit.getLogger().info("Guild ID: " + guildID);
        channelID = configManager.getConfig().getLong("channelID");
        otherChannelID = configManager.getConfig().getLong("otherChannelID");
        Bukkit.getLogger().info("Channel ID: " + channelID);
        this.messageFormat = configManager.getString("messageFormat");
        this.joinFormat = configManager.getString("joinFormat");
        this.leaveFormat = configManager.getString("leaveFormat");
        this.commands = Objects.requireNonNull(configManager.getConfig().getConfigurationSection("commands")).getKeys(false);
        this.otherJDA = otherJDA;
        loadBOT();
    }
    void loadBOT() {
        Bukkit.getLogger().info("Starting WebHook JDA...");
        jda = JDABuilder.createDefault(configManager.getString("token")).enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT).build();
        jda.addEventListener(this);
        jda.updateCommands().addCommands(
                        Commands.slash("playerlist", "List all online players")
                        , Commands.slash("list", "List all online players")
                        , Commands.slash("players", "List all online players")
                        , Commands.slash("playerinfo", "Get info about a player")
                                .addOption(OptionType.STRING, "player", "provide a valid player name", true)
                        , Commands.slash("profile", "Get info about a player")
                                .addOption(OptionType.STRING, "player", "provide a valid player name", true))
                .queue();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            guild = jda.getGuildById(guildID);
            this.channel = jda.getTextChannelById(channelID);
            if (channel == null) throw new IllegalArgumentException("Channel id null somehow");
            loadWebHook(jda, webHookID, channel, false);
            if (otherJDA != null) {
                otherChannel = otherJDA.getTextChannelById(otherChannelID);
                if (otherChannel == null) throw new IllegalArgumentException("Channel id null somehow: other channel");
                loadWebHook(otherJDA, otherWebHookID, otherChannel, true);
            }
        }, 20L);
    }
    void loadWebHook(JDA jda, Long webHookID, TextChannel channel, boolean other) {
        Bukkit.getLogger().info("Creating webhook");
        try {
            Webhook webhook;
            try {
                webhook = jda.retrieveWebhookById(webHookID).complete(true);
            } catch (ErrorResponseException e) {
                webhook = channel.createWebhook("SkiesBot-Webhook").complete();
                webHookID = webhook.getIdLong();
                if (otherJDA != null) configManager.set("otherhookid", webHookID);
                else configManager.set("hookid", webHookID);
                configManager.save();
            }
            WebhookClientBuilder builder = new WebhookClientBuilder(webhook.getUrl()); // or id, token
            builder.setThreadFactory((job) -> {
                Thread thread = new Thread(job);
                thread.setName("SkiesBot-Webhook");
                thread.setDaemon(true);
                return thread;
            });
            builder.setWait(true);
            if (other) {
                otherClient = builder.build();
                return;
            }
            client = builder.build();
        } catch (RateLimitedException e) {
            e.printStackTrace();
        }
    }
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (jda == null) {
            if (otherJDA == null) return;

        }
        if (commands.isEmpty()) return;
        if (!event.getChannel().equals(channel)) return;
        if (!event.getGuild().equals(guild)) return;
        Bukkit.getOnlinePlayers().forEach(player -> Audience.audience(player).sendMessage(formatMessage(
                messageFormat
                , player
                , event.getMessage().getContentStripped(), Objects.requireNonNull(event.getMember()))
        ));
        sendWebHook(event.getMessage().getContentRaw(), event.getAuthor().getAvatarUrl(), event.getAuthor().getName(), false, true);
        for (String command : commands) {
            if (!event.getMessage().getContentRaw().startsWith(configManager.getString("commands." + command + ".command")))
                return;
            event.getMessage().delete().queue();
            event.getChannel().sendMessage(configManager.getString("commands." + command + ".message")).queue();
            return;
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        command.onCommand(event);
        embeds.forEach(embed -> embed.sendEmbed(event));
    }

    public Component formatMessage(String format, Player player, String actualMessage, @Nullable Member member) {
        if (format.contains("{message}")) format = format.replace("{message}", actualMessage);
        if (member != null) {
            Role role = member.getRoles().get(0);
            format = format.replace("{discordrank}", role.getName());
            format = format.replace("{discordname}", member.getEffectiveName());
        }
        if (format.contains("{online}")) format = format.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));
        format = Placeholder.replace(format, player);
        return MiniMessage.miniMessage().deserialize(format);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        sendJoinAndLeave(event.getPlayer(), true);

    }
    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {
        sendJoinAndLeave(event.getPlayer(), false);

    }
    void sendJoinAndLeave(Player player, boolean join) {
        if (jda == null) return;
        String message;
        if (join) message = this.joinFormat;
        else message = this.leaveFormat;
        message = message.replace("{playername}", player.getName());
        String finalMessage = message;
        if (channel == null) return;
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTimestamp(new Date().toInstant())
                .setColor(0x2F3136)
                .setAuthor(finalMessage, null, "https://cravatar.eu/helmhead/"
                        + player.getName()
                        + ".png")
                .build()).queue();
    }
    @EventHandler
    public void onMessageReceived(AsyncChatEvent event) {
        if (jda == null) return;
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        message = message.replace("<chat=" + event.getPlayer().getUniqueId() + ">", "");
        sendWebHook(message, "https://cravatar.eu/helmhead/" + event.getPlayer().getName() + ".png", event.getPlayer().getName(), false, false);
    }

    public void sendWebHook(String message, String url, String name, boolean embed, boolean other) {
        Bukkit.getLogger().info("Sending webhook: " + other);
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(name); // use this username
        builder.setAvatarUrl(url); // use this avatar
        builder.setContent(message); // add content
        if (other) {
            otherClient.send(builder.build());
            return;
        }
        client.send(builder.build());
        if (!embed) {
            return;
        }
        client.send( new WebhookEmbedBuilder()
                .setDescription("`" + message + "`")
                .setTimestamp(new Date().toInstant())
                .setColor(0x2F3136)
                .setFooter(new WebhookEmbed.EmbedFooter(guild.getName(), guild.getIconUrl()))
                .setAuthor(new WebhookEmbed.EmbedAuthor(name, null, "https://cravatar.eu/helmhead/"
                        + name
                        + ".png"))
                .build());
    }

}
