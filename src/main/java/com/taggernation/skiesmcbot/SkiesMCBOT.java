package com.taggernation.skiesmcbot;

import com.taggernation.skiesmcbot.commands.Commands;
import com.taggernation.skiesmcbot.commands.HelpEmbed;
import com.taggernation.skiesmcbot.commands.Test;
import com.taggernation.skiesmcbot.embeds.EmbedFromYML;
import com.taggernation.skiesmcbot.events.PlayerJoinAndLeave;
import com.taggernation.skiesmcbot.tasks.LoopTask;
import com.taggernation.taggernationlib.TaggerNationLib;
import com.taggernation.taggernationlib.config.Config;
import com.taggernation.taggernationlib.placeholder.Placeholder;
import me.realized.duels.api.Duels;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

public final class SkiesMCBOT extends JavaPlugin {

    public static JDA getJda() {
        return jda;
    }

    private static JDA jda;
    private Config mainConfig;

    public Config getPlayerData() {
        return playerData;
    }

    private Config playerData;
    private Placeholder placeholder;
    private List<Config> embedConfigs = new ArrayList<>();
    private List<EmbedFromYML> embedFromYMLList = new ArrayList<>();
    public static Duels duels;

    public LoopTask getLoopTask() {
        return loopTask;
    }

    public LoopTask loopTask;
    public static SkiesMCBOT getInstance() {
        return instance;
    }

    private static SkiesMCBOT instance;

    @Override
    public void onEnable() {
        loopTask = new LoopTask(this);
        loopTask.runTaskTimer(this, 0, 40);
        instance = this;
        placeholder = TaggerNationLib.papiHook;
        duelsExist();
        this.getLogger().info("Starting up...");
        mainConfig = new Config(this, "config.yml", false, true);
        try {
            this.getLogger().info("Starting JDA...");
            jda = JDABuilder.createDefault(mainConfig.getString("token")).build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
        jda.addEventListener(new Commands());
        jda.addEventListener(new Test());
        for (String embeds: mainConfig.getStringList("embeds")) {
            this.getLogger().info("Loading embed: " + embeds);
            Config config = new Config(this, embeds, false, false);
            embedConfigs.add(config);
            EmbedFromYML embedFromYML = new EmbedFromYML(config, jda, loopTask).build();
            embedFromYMLList.add(embedFromYML);
        }
        playerData = new Config(this, "playerdata.yml", false, false);
        this.getServer().getPluginManager().registerEvents(new PlayerJoinAndLeave(playerData,placeholder), this);
        this.getLogger().info("A Plugin by Edward from taggernation.com");
        this.getLogger().info("Started up!");
        HelpEmbed.addCommand("!profile <player>" + "- Gets information about a player");
        HelpEmbed.addCommand("!playerlist" + "- Get the List of online players \n > **Command allies** : `!list` && `!players`");
        HelpEmbed.addCommand("!leaderboard duels" + "- Get the leaderboard of duels \n > **Command allies** : `!status duels` && `!top duels`");
    }

    @Override
    public void onDisable() {
        jda.shutdown();
        this.getServer().getScheduler().cancelTasks(this);
        // Plugin shutdown logic
    }
    public void duelsExist() {
        if (instance.getServer().getPluginManager().getPlugin("Duels") != null) {
            duels = (Duels) Bukkit.getServer().getPluginManager().getPlugin("Duels");
        } else {
            duels = null;
        }
    }
}
