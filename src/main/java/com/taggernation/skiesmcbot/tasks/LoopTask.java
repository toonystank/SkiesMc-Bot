package com.taggernation.skiesmcbot.tasks;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("unused")
public class LoopTask extends BukkitRunnable {

    private boolean TaskRunning = false;
    private MessageChannel channelID;
    private String message;
    public boolean isTaskRunning() {
        return TaskRunning;
    }
    public LoopTask(SkiesMCBOT instance) {
    }
    public void setTaskRunning(boolean taskRunning) {
        TaskRunning = taskRunning;
    }

    public void setChannelID(MessageChannel channelID) {
        this.channelID = channelID;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        if (TaskRunning) {
            channelID.sendMessage(message).queue();
        }
    }
}
