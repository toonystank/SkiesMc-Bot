package com.taggernation.skiesmcbot.events;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.bukkit.Bukkit;

import java.util.Objects;

public class MemberJoinAndLeave {

    public static void onMemberJoin(GuildMemberJoinEvent event) {
        Bukkit.getLogger().info("member Joined");
        Objects.requireNonNull(event.getMember().getGuild().getTextChannelById("877444527978328074"), "Text Channel with id 877444527978328074 not found").sendMessage("Welcome " + event.getMember().getAsMention() + " to the server!").queue();
    }

}
