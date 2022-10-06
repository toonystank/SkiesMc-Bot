package com.taggernation.skiesmcbot.events;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.skiesmcbot.utils.Placeholder;
import com.taggernation.taggernationlib.config.ConfigManager;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;

public class PlayerJoinAndLeave implements Listener {

    private final ConfigManager playerData;
    public PlayerJoinAndLeave(ConfigManager playerData) {
        this.playerData = playerData;
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
        playerDataConfig.set("players." + playerName + ".nick", Placeholder.replace(player.getName(), player));
        playerDataConfig.set("players." + playerName + ".nick", Placeholder.replace(SkiesMCBOT.getInstance().essentials.getOfflineUser(playerName).getNick(), player));
        playerDataConfig.set("players." + playerName + ".UUID", player.getUniqueId().toString());
        playerDataConfig.set("players." + playerName + ".rank", Placeholder.replace("%vault_rank%", player));
        playerDataConfig.set("players." + playerName + ".level", Placeholder.replace("%clv_player_level%", player));
        playerDataConfig.set("players." + playerName + ".balance", Placeholder.replace(SkiesMCBOT.getInstance().essentials.getOfflineUser(playerName).getMoney().toPlainString(), player));
        playerDataConfig.set("players." + playerName + ".kills", player.getStatistic(Statistic.PLAYER_KILLS));
        playerDataConfig.set("players." + playerName + ".deaths", player.getStatistic(Statistic.DEATHS));
        if ((player.getStatistic(Statistic.DEATHS) == 0) || (player.getStatistic(Statistic.PLAYER_KILLS) == 0)) {
            playerDataConfig.set("players." + playerName + ".kdr", 0);
        }else {
            playerDataConfig.set("players." + playerName + ".kdr", (double) player.getStatistic(Statistic.PLAYER_KILLS) / (double) player.getStatistic(Statistic.DEATHS));
        }
        playerDataConfig.set("players." + playerName + ".coins", Placeholder.replace("%tmtokens_get_tokens_fixed%", player));
        playerDataConfig.set("players." + playerName + ".claim_blocks", Placeholder.replace("%lands_land_chunks%`/`%lands_land_chunks_max%", player));
        playerData.save();
    }
}
