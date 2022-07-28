package com.taggernation.skiesmcbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HelpEmbed {
    private final EmbedBuilder embedBuilder = new EmbedBuilder();

    private static final List<String> commands = new ArrayList<>();
    private static final List<String> formattedCommands = new ArrayList<>();

    public static void addCommand(String command) {
        commands.add(command);
    }
    public static List<String> getCommands() {
        return commands;
    }
    public MessageEmbed getEmbed(MessageReceivedEvent event) {
        embedBuilder.setTitle("Help :tools:");
        embedBuilder.setDescription("Here is a list of commands I can help you with.");
        int i = 0;
        for (String command : commands) {
            i++;
            formattedCommands.add("`"+ i + "`) " + command);
        }
        embedBuilder.addField("`User commands`", String.join("\n", formattedCommands), false);
        embedBuilder.addField("`Admin commands`", "`1` ) " + "!say [ChannelID] <Message> - _Send message as Bot_"+ "\n`2` ) " + "!looptext [channelID] <message> - _Loop the provided message as bot_" + "\n`3` ) "+ "!test [text] - _Test command for upcoming features_" + "\n`4` ) !tpsaware `<start|stop|status|setinterval> [interval]` - _Stops TPS log for 1 hour_", false);
        embedBuilder.setAuthor(event.getAuthor().getName(), null, event.getAuthor().getAvatarUrl());
        embedBuilder.setFooter("Command Options [] = optional, <> = required", null);
        embedBuilder.setTimestamp(new Date().toInstant());
        return embedBuilder.build();
    }
}
