package com.taggernation.skiesmcbot.commands;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.skiesmcbot.utils.DefaultEmbed;
import com.taggernation.skiesmcbot.utils.Placeholder;
import com.taggernation.taggernationlib.config.ConfigManager;
import me.realized.duels.api.Duels;
import me.realized.duels.api.user.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class PlayerInfo {

    private final Duels duels;
    private final MessageReceivedEvent event;
    public PlayerInfo(Duels duels, MessageReceivedEvent event) {
        this.duels = duels;
        this.event = event;
    }

    ConfigManager playerData = SkiesMCBOT.getInstance().getPlayerData();
    Placeholder placeholder = new Placeholder();

    public void sendPlayerInfo() throws IOException, InvalidConfigurationException {
        playerData.reload();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String[] args = event.getMessage().getContentRaw().split(" ");
        DefaultEmbed.setDefault(embedBuilder, event);

        if (args.length > 1) {
            String player = args[1].toUpperCase(Locale.ROOT);
            if (!playerData.getConfig().contains("players." + player)) {
                embedBuilder.setTitle("Error");
                embedBuilder.setDescription("Player not found. Join once to record your data.");
                event.getTextChannel().sendMessageEmbeds(embedBuilder.build()).queue();
                return;
            }
            User duelsData = duels.getUserManager().get(UUID.fromString(formatFields(DataType.UUID,player)));
            embedBuilder.setTitle(":stars: `"+ player + "`'s Profile");
            embedBuilder.setDescription("Player found. Here is their information.");
            embedBuilder.addField(":person_bouncing_ball: **Player statistics**", "player in-game statistics", false);
            embedBuilder.addField("Nick", formatFields(DataType.NICK, player), true);
            embedBuilder.addField("Rank ", formatFields(DataType.RANK, player), true);
            embedBuilder.addField("Balance ", formatFields(DataType.BALANCE,player), true);
            embedBuilder.addField("K/D Ratio ", formatFields(DataType.KDR,player) + " ", true);
            embedBuilder.addField("Job points ", formatFields(DataType.JOBS_POINTS,player), true);
            embedBuilder.addField("Claim blocks ", formatFields(DataType.CLAIM_BLOCKS,player), true);
            if (Bukkit.getPlayer(player) != null) {
                int playerPing = Objects.requireNonNull(Bukkit.getPlayer(player)).getPing();
                embedBuilder.addField("Ping", playerPing + "ms", true);
            }
/*            if (duelsData != null) {
                embedBuilder.addField(":crossed_swords: **Duels statistics**", "Duels in-game statistics", false);
                embedBuilder.addField("Wins ", duelsData.getWins() + "", true);
                embedBuilder.addField("Losses ", duelsData.getLosses() + "", true);
            }else {
                embedBuilder.addField(":crossed_swords: **Duels statistics**", "Player dose not have any duels statistics", true);
            }*/
            embedBuilder.setThumbnail("https://crafatar.com/renders/body/" + formatFields(DataType.UUID,player) + "?overlay");

        }else {
            embedBuilder.setTitle("Error");
            embedBuilder.setDescription("Please specify a player.\n" + " Usage: `!playerinfo <player>`");
            return;
        }
        event.getTextChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public String formatFields(DataType dataType, String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        switch (dataType) {
            case KILLS:
                if (player != null && player.isOnline()) {
                    return player.getStatistic(Statistic.PLAYER_KILLS) + "";
                }
                return playerData.getString("players." + playerName + ".kills");
            case DEATHS:
                if (player != null && player.isOnline()) {
                    return player.getStatistic(Statistic.DEATHS) + "";
                }
                return playerData.getString("players." + playerName + ".deaths");
            case KDR:
                if (player != null && player.isOnline()) {
                    int kdr = player.getStatistic(Statistic.DEATHS) == 0 ? player.getStatistic(Statistic.PLAYER_KILLS) : player.getStatistic(Statistic.PLAYER_KILLS) / player.getStatistic(Statistic.DEATHS);
                    return kdr + "";
                }
                return playerData.getString("players." + playerName + ".kdr");
            case JOBS_POINTS:
                if (player != null && player.isOnline()) {
                    return placeholder.replace("%jobsr_user_points_fixed%", player);
                }
                return playerData.getString("players." + playerName + ".jobs_points");
            case CLAIM_BLOCKS:
                if (player != null && player.isOnline()) {
                    return placeholder.replace("%griefprevention_remainingclaims_formatted%", player);
                }
                return playerData.getString("players." + playerName + ".claim_blocks");
            case BALANCE:
                if (player != null && player.isOnline()) {
                    return placeholder.replace("%cmi_user_balance_formatted%", player);
                }
                return playerData.getString("players." + playerName + ".balance");
            case RANK:
                if (player != null && player.isOnline()) {
                    return placeholder.replace("%vault_rank%", player);
                }
                return playerData.getString("players." + playerName + ".rank");
            case NICK:
                if (player != null && player.isOnline()) {
                    return placeholder.replace("%cmi_user_cleannickname%", player);
                }
                return playerData.getString("players." + playerName + ".nick");
            case UUID:
                if (player != null && player.isOnline()) {
                    return player.getUniqueId().toString();
                }
                return playerData.getString("players." + playerName + ".UUID");
            default:
                return null;
        }
    }
    private enum DataType {
        RANK,
        BALANCE,
        KILLS,
        DEATHS,
        KDR,
        JOBS_POINTS,
        CLAIM_BLOCKS,
        NICK,
        UUID
    }
}
