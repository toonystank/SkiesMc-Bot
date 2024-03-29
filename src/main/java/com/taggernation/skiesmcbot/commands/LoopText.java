package com.taggernation.skiesmcbot.commands;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.skiesmcbot.tasks.LoopTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;

import java.util.Objects;

public class LoopText {
    LoopTask loopTask;

    public LoopText(LoopTask loopTask) {
        this.loopTask = loopTask;
    }
    public void loopCommand(SlashCommandInteractionEvent event) {
        String[] args = Objects.requireNonNull(event.getOption("text")).getAsString().split(" ");

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_SERVER)) return;
        if (args.length > 1) {
            MessageChannel channel;
            String text;
            try {
                channel = SkiesMCBOT.getJda().getTextChannelById(args[1]);
            } catch (NumberFormatException e) {
                channel = event.getChannel();
            }
            if (channel == null) {
                channel = event.getChannel();

            }
            text = Objects.requireNonNull(event.getOption("text")).getAsString();
            if (!loopTask.isTaskRunning()) {
                channel.sendMessage("started with text `" + text + "`").queue();
                loopTask.setTaskRunning(true);
                loopTask.setMessage(text);
                loopTask.setChannelID(channel);
                return;
            }
            channel.sendMessage("stopped").queue();
            loopTask.setTaskRunning(false);
        }else {
            loopTask.setTaskRunning(false);
        }
    }
}
