package com.taggernation.skiesmcbot.commands;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.skiesmcbot.utils.DefaultEmbed;
import com.taggernation.skiesmcbot.utils.Placeholder;
import com.taggernation.taggernationlib.config.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class PlayerInfo {

    private final SlashCommandInteractionEvent event;
    public PlayerInfo( SlashCommandInteractionEvent event) {
        this.event = event;
    }

    ConfigManager playerData = SkiesMCBOT.getInstance().getPlayerData();

    public void sendPlayerInfo() throws IOException, InvalidConfigurationException {
        playerData.reload();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String player = Objects.requireNonNull(event.getOption("player")).getAsString();
        DefaultEmbed.setDefault(embedBuilder, Objects.requireNonNull(event.getMember()).getAvatarUrl(), event.getMember().getEffectiveName(), Objects.requireNonNull(event.getGuild()).getName(), event.getGuild().getIconUrl());

        if (!playerData.getConfig().contains("players." + player.toUpperCase(Locale.ROOT))) {
            embedBuilder.setTitle("Error");
            embedBuilder.setDescription("Player not found. Join once to record your data.");
            event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
            return;
        }
        embedBuilder.setTitle(":stars: `" + player + "`'s Profile");
        embedBuilder.setDescription("Player found. Here is their information.");
        embedBuilder.addField(":person_bouncing_ball: **Player statistics**", "player in-game statistics", false);
        embedBuilder.addField("Nick", formatFields(DataType.NICK, player), true);
        embedBuilder.addField("Rank ", formatFields(DataType.RANK, player), true);
        embedBuilder.addField("Balance ", formatFields(DataType.BALANCE, player), true);
        embedBuilder.addField("Level ", formatFields(DataType.LEVEL, player), true);
        embedBuilder.addField("K/D Ratio ", formatFields(DataType.KDR, player) + " ", true);
        embedBuilder.addField("Coins ", formatFields(DataType.COINS, player), true);
        embedBuilder.addField("Claim blocks ", formatFields(DataType.CLAIM_BLOCKS, player), true);
        if (Bukkit.getPlayer(player) != null && Objects.requireNonNull(Bukkit.getPlayer(player)).isOnline()) {
            int playerPing = Objects.requireNonNull(Bukkit.getPlayer(player)).getPing();
            embedBuilder.addField("Ping", playerPing + "ms", true);
        }
        embedBuilder.setThumbnail("https://cravatar.eu/helmhead/" + formatFields(DataType.NAME, player) + ".png");
        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
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
            case LEVEL:
                if (player != null && player.isOnline()) {
                    return Placeholder.replace("%clv_player_level%", player);
                }
                return playerData.getString("players." + playerName + ".level");
            case COINS:
                if (player != null && player.isOnline()) {
                    return Placeholder.replace("%tmtokens_get_tokens_fixed%", player);
                }
                return playerData.getString("players." + playerName + ".coins");
            case CLAIM_BLOCKS:
                if (player != null && player.isOnline()) {
                    return Placeholder.replace("%lands_land_chunks%`/`%lands_land_chunks_max%", player);
                }
                return playerData.getString("players." + playerName + ".claim_blocks");
            case BALANCE:
                if (player != null && player.isOnline()) {
                    return Placeholder.replace(SkiesMCBOT.getInstance().essentials.getOfflineUser(playerName).getMoney().toPlainString(), player);
                }
                return playerData.getString("players." + playerName + ".balance");
            case RANK:
                if (player != null && player.isOnline()) {
                    return Placeholder.replace("%vault_rank%", player);
                }
                return playerData.getString("players." + playerName + ".rank");
            case NICK:
                if (player != null && player.isOnline()) {
                    return Placeholder.replace(SkiesMCBOT.getInstance().essentials.getOfflineUser(playerName).getNick(), player);
                }
                return playerData.getString("players." + playerName + ".nick");
            case NAME:
                if (player != null && player.isOnline()) {
                    return player.getName();
                }
                return playerData.getString("players." + playerName + ".name");
            default:
                return null;
        }
    }
    private enum DataType {
        RANK,
        BALANCE,
        KILLS,
        LEVEL,
        DEATHS,
        KDR,
        COINS,
        CLAIM_BLOCKS,
        NICK,
        NAME
    }
}
