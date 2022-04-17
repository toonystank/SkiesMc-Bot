package com.taggernation.skiesmcbot.tasks;

import com.taggernation.skiesmcbot.SkiesMCBOT;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.scheduler.BukkitRunnable;

public class LoopTask extends BukkitRunnable {

    private SkiesMCBOT instance;
    private boolean TaskRunning = false;
    private TextChannel channelID;
    private String message;
    public boolean isTaskRunning() {
        return TaskRunning;
    }
    public LoopTask(SkiesMCBOT instance) {
        this.instance = instance;
    }
    public void setTaskRunning(boolean taskRunning) {
        TaskRunning = taskRunning;
    }

    public void setChannelID(TextChannel channelID) {
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
