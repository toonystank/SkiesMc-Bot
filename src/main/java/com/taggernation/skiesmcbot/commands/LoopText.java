package com.taggernation.skiesmcbot.commands;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import com.taggernation.skiesmcbot.tasks.LoopTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;

import java.util.Objects;

public class LoopText {
    LoopTask loopTask;

    public LoopText(LoopTask loopTask) {
        this.loopTask = loopTask;
    }
    public void loopCommand(MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_SERVER)) return;
        if (args.length > 1) {
            TextChannel channel;
            String text;
            try {
                channel = SkiesMCBOT.getJda().getTextChannelById(args[1]);
                text = event.getMessage().getContentRaw().replace(args[1], "");
            } catch (NumberFormatException e) {
                channel = event.getTextChannel();
            }
            if (channel == null) {
                channel = event.getTextChannel();

            }
            text = event.getMessage().getContentRaw().replace("!looptext ", "");
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
