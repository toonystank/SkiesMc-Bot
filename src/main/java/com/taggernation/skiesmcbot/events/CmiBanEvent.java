package com.taggernation.skiesmcbot.events;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.events.CMIPlayerBanEvent;
import com.taggernation.taggernationlib.config.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Date;
import java.util.Objects;
import java.util.Timer;

public class CmiBanEvent implements Listener {

    private final ConfigManager mainConfig;
    private final JDA jda;
    public CmiBanEvent(ConfigManager config, JDA jda) {
        this.mainConfig = config;
        this.jda = jda;
    }

    @EventHandler
    public void onPlayerBan(CMIPlayerBanEvent event) {
        String logChannel = mainConfig.getString("Logs.banchannel");
        if (logChannel != null) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTimestamp(new Date().toInstant());
            embedBuilder.setTitle(":hammer: Player Banned");
            embedBuilder.addField("**Banned Player**", "`" + CMI.getInstance().getPlayerManager().getUser(event.getBanned()).getName() + "`", true);
            embedBuilder.addField("**Banned By**", "`" + event.getBannedBy().getName() + "`", true);
            embedBuilder.addField("**Reason**", "```" + event.getReason() + "```", false);
            embedBuilder.setColor(0x2F3136);
            Objects.requireNonNull(jda.getTextChannelById(logChannel)).sendMessageEmbeds(embedBuilder.build()).queue();

        }


    }
}
