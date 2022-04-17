package com.taggernation.skiesmcbot.events;

import com.taggernation.taggernationlib.config.Config;
import com.taggernation.taggernationlib.placeholder.Placeholder;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;

public class PlayerJoinAndLeave implements Listener {

    private final Config playerData;
    private final Placeholder placeholder;
    public PlayerJoinAndLeave(Config playerData, Placeholder placeholder) {
        this.playerData = playerData;
        this.placeholder = placeholder;
    }
    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        saveData(player);
    }
    @EventHandler
    public void playerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        saveData(player);
    }
    public void saveData(Player player)  {
        FileConfiguration playerDataConfig = playerData.getConfig();
        String playerName = player.getName().toUpperCase(Locale.ROOT);
        playerDataConfig.set("players." + playerName + ".nick", placeholder.replace("%cmi_user_cleannickname%", player));
        playerDataConfig.set("players." + playerName + ".UUID", player.getUniqueId().toString());
        playerDataConfig.set("players." + playerName + ".rank", placeholder.replace("%vault_rank%", player));
        playerDataConfig.set("players." + playerName + ".balance", placeholder.replace("%cmi_user_balance_formatted%", player));
        playerDataConfig.set("players." + playerName + ".kills", player.getStatistic(Statistic.PLAYER_KILLS));
        playerDataConfig.set("players." + playerName + ".deaths", player.getStatistic(Statistic.DEATHS));
        playerDataConfig.set("players." + playerName + ".kdr", player.getStatistic(Statistic.DEATHS) == 0 ? player.getStatistic(Statistic.PLAYER_KILLS) : player.getStatistic(Statistic.PLAYER_KILLS) / player.getStatistic(Statistic.DEATHS));
        playerDataConfig.set("players." + playerName + ".jobs_points", placeholder.replace("%jobsr_user_points_fixed%", player));
        playerDataConfig.set("players." + playerName + ".claim_blocks", placeholder.replace("%griefprevention_remainingclaims_formatted%", player));
        playerData.save();
    }
}
