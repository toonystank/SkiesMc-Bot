package com.taggernation.skiesmcbot.tasks;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.taggernationlib.config.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;


public class TpsMonitor extends BukkitRunnable {

    private static final SkiesMCBOT plugin = SkiesMCBOT.getInstance();
    private final JDA jda;
    private final ConfigManager config;
    private double[] TPS;
    private double MSPT;
    private int entityCount;
    private int loadedChunks;
    private int interval;
    private long dataEmbedId = 0;
    private final EmbedBuilder embed = new EmbedBuilder();
    private final List<World> worldList = new ArrayList<>();
    private final Map<String, String> worldInfo = new HashMap<>();

    public boolean isTpsMonitorEnabled() {
        return tpsMonitorEnabled;
    }

    private static boolean tpsMonitorEnabled;

    public TpsMonitor(JDA jda, ConfigManager config) {
        this.jda = jda;
        this.config = config;
        interval = 500;
        tpsMonitorEnabled = true;
        if (config.getString("data.tpsmonitor.embedid") != null) {
            dataEmbedId = Long.parseLong(config.getString("data.tpsmonitor.embedid"));
        }
    }

    @Override
    public void run() {
        new BukkitRunnable() {
            @Override
            public void run() {
                String tpsReport = null;
                TPS = plugin.getServer().getTPS();
                MSPT = plugin.getServer().getAverageTickTime();
                entityCount = 0;
                loadedChunks = 0;
                for (World worlds : plugin.getServer().getWorlds()) {
                    if (!worldList.contains(worlds)) {
                        worldList.add(worlds);
                    }
                    entityCount = entityCount + worlds.getEntities().size();
                    loadedChunks = loadedChunks + worlds.getChunkCount();
                    worldInfo.remove(worlds.getName());
                    worldInfo.put(worlds.getName() ,"`" + worlds.getName() + "` : " + worlds.getEntities().size() + " *entities*, " + worlds.getChunkCount() + " *chunks*");

                }
                if (!tpsMonitorEnabled) return;
                embed.setTimestamp(new Date().toInstant());
                String outputString = "\n:airplane_small: `TPS` : " + (int) Math.round(TPS[0]) + "\n:rocket: `MSPT (Average Tick Time)` :" + (int) Math.round(MSPT) + "\n:people_wrestling: `Online Players` : " + plugin.getServer().getOnlinePlayers().size() + "\n:microbe: `Total loaded entities`: " + entityCount;
                if ((MSPT > 50) || (TPS[0] < 10)) {
                    embed.setTitle("**TPS CRITICAL**");
                    tpsReport = "@everyone :warning: Critical TPS detected!. Generate a **Timings** report now :warning:" + "\n:airplane_small: `TPS` : " + (int) Math.round(TPS[0]) + "\n:rocket: `MSPT (Average Tick Time)` :" + (int) Math.round(MSPT) + "\n:people_wrestling: `Online Players` : " + plugin.getServer().getOnlinePlayers().size() + "\n:microbe: `Total loaded entities`: " + entityCount;
                } else if ((MSPT > 30) || (TPS[0] < 15)) {
                    embed.setTitle("**TPS WARNING**");
                    tpsReport = ":warning: Generate a timings now :warning:" + "\n:airplane_small: `TPS` : " + (int) Math.round(TPS[0]) + "\n:rocket: `MSPT (Average Tick Time)` :" + (int) Math.round(MSPT) + "\n:people_wrestling: `Online Players` : " + plugin.getServer().getOnlinePlayers().size() + "\n:microbe: `Total loaded entities`: " + entityCount;
                } else if ((MSPT > 10) || (TPS[0] < 17)) {
                    embed.setTitle("**TPS DROPPING**");
                } else {
                    embed.setTitle("**TPS OK**");
                }
                embed.setDescription(outputString);
                embed.setColor(0x2F3136);
                outputToDiscord(embed);
                if (tpsReport != null) {
                    outputToDiscord(tpsReport);
                }
            }
        }.runTaskTimer(plugin, 0, interval);
    }

    void outputToDiscord(String message) {
        Objects.requireNonNull(jda.getTextChannelById(config.getString("Logs.tpswarnchannel"))).sendMessage(message).queue();
    }

    void outputToDiscord(EmbedBuilder embedBuilder) {
        TextChannel channel = jda.getTextChannelById(config.getString("Logs.tpschannel"));
        if (channel == null) throw new NullPointerException("Logs.tpschannel Channel not found in config");
        if (dataEmbedId == 0) {
            RestAction<Message> ra = channel.sendMessageEmbeds(embedBuilder.build());
            Message completeMessage = ra.complete();
            dataEmbedId = completeMessage.getIdLong();
            config.set("data.tpsmonitor.embedid", dataEmbedId);
            config.save();
        }
        channel.editMessageEmbedsById(dataEmbedId, embedBuilder.build()).queue();
    }

    public double getTps() {
        return TPS[0];
    }
    public double getMspT() {
        return MSPT;
    }
    public List<World> getWorldList() {
        return worldList;
    }
    public int getLoadedChunks() {
        return loadedChunks;
    }
    public int getEntityCount() {
        return entityCount;
    }


    public void tpsAware(boolean TpsAware) {
        tpsMonitorEnabled = TpsAware;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!tpsMonitorEnabled) {
                    tpsMonitorEnabled = true;
                }
            }
        }.runTaskLater(plugin, 72000L);
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void detailedTpsReport(MessageReceivedEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder().setTimestamp(new Date().toInstant());
        String minerDetails = "\n**TPS** \n`"
                + (int) Math.round(TPS[0])
                + "`\n**MSPT (Average Tick Time)** \n`"
                + (int) Math.round(MSPT) + "`\n**Online Players** \n`"
                + plugin.getServer().getOnlinePlayers().size()
                + "`\n**Total loaded entities** \n`"
                + entityCount + "`";
        String advancedDetails = "\n**Ram usage** \n`"
                + (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L + "` / `" + Runtime.getRuntime().maxMemory() / 1024L / 1024L
                +"`\n**Total loaded chunks**\n`"
                + loadedChunks
                + "`\n**Total loaded worlds**\n`"
                + plugin.getServer().getWorlds().size()
                + "`\n**World details**\n"
                + worldList.stream().map(world -> worldInfo.get(world.getName())).collect(Collectors.joining("\n"));
        embedBuilder.setTitle(":airplane_small: **TPS Details**");
        embedBuilder.setColor(0x2F3136);
        embedBuilder.setDescription(minerDetails + advancedDetails);
        event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }

}

