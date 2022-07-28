package com.taggernation.skiesmcbot;

import com.taggernation.skiesmcbot.commands.HelpEmbed;
import com.taggernation.skiesmcbot.commands.Test;
import com.taggernation.skiesmcbot.events.*;
import com.taggernation.skiesmcbot.tasks.TpsMonitor;
import com.taggernation.skiesmcbot.utils.EmbedFromYML;
import com.taggernation.skiesmcbot.tasks.LoopTask;
import com.taggernation.skiesmcbot.utils.Placeholder;
import com.taggernation.skiesmcbot.utils.SuggestionManager;
import com.taggernation.taggernationlib.config.ConfigManager;
import me.realized.duels.api.Duels;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SkiesMCBOT extends JavaPlugin {


    public static JDA getJda() {
        return jda;
    }

    private static JDA jda;

    public ConfigManager getPlayerData() {
        return playerData;
    }

    private ConfigManager playerData;

    public ConfigManager getSuggestionData() {
        return SuggestionData;
    }

    private ConfigManager SuggestionData;

    public List<EmbedFromYML> getEmbedFromYMLList() {
        return embedFromYMLList;
    }

    private final List<EmbedFromYML> embedFromYMLList = new ArrayList<>();
    public static Duels duels;

    public LoopTask getLoopTask() {
        return loopTask;
    }

    public LoopTask loopTask;

    public TpsMonitor getTpsMonitor() {
        return tpsMonitor;
    }

    public TpsMonitor tpsMonitor;
    public static SkiesMCBOT getInstance() {
        return instance;
    }

    private static SkiesMCBOT instance;

    @Override
    public void onEnable() {
        loopTask = new LoopTask(this);
        loopTask.runTaskTimer(this, 0, 40);
        instance = this;

        Placeholder placeholder = new Placeholder();
        duelsExist();

        this.getLogger().info("Starting up...");
        ConfigManager mainConfig;
        try {
            mainConfig = new ConfigManager(this, "config.yml", false, true);
            try {
                this.getLogger().info("Starting JDA...");
                jda = JDABuilder.createDefault(mainConfig.getString("token")).enableIntents(GatewayIntent.GUILD_MEMBERS).build();
            } catch (LoginException e) {
                e.printStackTrace();
                this.onDisable();
            }
            tpsMonitor = new TpsMonitor( jda, mainConfig);
            tpsMonitor.run();
            this.getServer().getPluginManager().registerEvents(new CmiBanEvent(mainConfig, jda), this);

            for (String embeds: mainConfig.getStringList("embeds")) {
                this.getLogger().info("Loading embed: " + embeds);
                ConfigManager config;
                try {
                    config = new ConfigManager(this, embeds,"Embeds", false, true);
                    EmbedFromYML embedFromYML = new EmbedFromYML(config, jda).registerEvents();
                    embedFromYMLList.add(embedFromYML);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                playerData = new ConfigManager(this, "playerdata.yml", false, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                SuggestionData = new ConfigManager(this, "Suggestions.yml", false, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            SuggestionManager sum = new SuggestionManager(SuggestionData,jda);
            CommandEvent ce = new CommandEvent(loopTask,tpsMonitor);
            SuggestionEvent se = new SuggestionEvent(mainConfig, sum);
            AnnouncementEvent ae = new AnnouncementEvent(mainConfig);
            jda.addEventListener(new EventManager(se, ce, ae, embedFromYMLList));
            jda.addEventListener(new Test());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.getServer().getPluginManager().registerEvents(new PlayerJoinAndLeave(playerData, placeholder), this);
        this.getLogger().info("A Plugin by Edward from taggernation.com");
        this.getLogger().info("Started up!");
        HelpEmbed.addCommand("!profile <player>" + "- Gets information about a player");
        HelpEmbed.addCommand("!playerlist" + "- Get the List of online players \n > **Command allies** : `!list` && `!players`");
        HelpEmbed.addCommand("!leaderboard duels" + "- Get the leaderboard of duels \n > **Command allies** : `!status duels` && `!top duels`");
    }

    @Override
    public void onDisable() {
        jda.cancelRequests();
        jda.shutdown();
        this.getServer().getScheduler().cancelTasks(this);
        this.getLogger().info("Shutting down...");
    }
    public void duelsExist() {
        if (instance.getServer().getPluginManager().getPlugin("Duels") != null) {
            duels = (Duels) Bukkit.getServer().getPluginManager().getPlugin("Duels");
        } else {
            duels = null;
        }
    }
}
