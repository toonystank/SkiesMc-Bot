package com.taggernation.skiesmcbot;

import lombok.Data;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;


@Data
public class MemeData {

    private Plugin plugin;
    private Meme meme;
    private JDA jda;

    private User member;

    private int upVotes;
    private int noReactions;
    private int downVotes;
    List<String> embedIDS = new ArrayList<>();

    public MemeData(Plugin plugin, Meme meme, JDA jda, User user){
        this.plugin = plugin;
        this.meme = meme;
        this.jda = jda;
        this.member = user;
        Bukkit.getLogger().info(this.member.toString());
    }

    public MemeData setUpdated() {
        this.upVotes = meme.getMemeData().getInt("data." + member.getId() + ".upvotes");
        this.downVotes = meme.getMemeData().getInt("data." + member.getId() + ".downvotes");
        this.noReactions = meme.getMemeData().getInt("data." + member.getId() + ".noreactions");
        this.embedIDS = meme.getMemeData().getStringList("data." + member.getId() + ".embedid");
        return this;
    }

    public void setUpVotes(int votes) {
        this.upVotes = votes;
        meme.getMemeData().set("data." + member.getId() + ".upvotes", votes);
        meme.getMemeData().save();
    }
    public void setNoReactions(int votes) {
        this.noReactions = votes;
        meme.getMemeData().set("data." + member.getId() + ".noreactions", votes);
        meme.getMemeData().save();

    }
    public void setDownVotes(int votes) {
        this.downVotes = votes;
        meme.getMemeData().set("data." + member.getId() + ".downvotes", votes);
        meme.getMemeData().save();

    }

    public void setData(Reactions reactions, int vote) {
        switch (reactions) {
            case UPVOTE ->
                    meme.getMemeData().set("data." + member.getId() + ".upvotes", vote);
            case DOWN_VOTE ->
                    meme.getMemeData().set("data." + member.getId() + ".downvotes", vote);
            case NO_REACTION ->
                    meme.getMemeData().set("data." + member.getId() + ".noreactions", vote);
        }
        meme.getMemeData().save();
    }
    public void setData(Reactions reactions, String vote) {
        if (reactions.equals(Reactions.EMBED_ID)) {
            List<String> embedList = meme.getMemeData().getStringList("data." + member.getId() + ".embedid");
            embedList.add(vote);
            meme.getMemeData().set("data." + member.getId() + ".embedid", embedList);
            meme.getMemeData().save();
        }
    }
    public void addReaction(Reactions reactions) {
        Bukkit.getLogger().info("Reaction added: " + reactions);
        switch (reactions) {
            case UPVOTE -> setUpVotes(this.upVotes + 1);
            case DOWN_VOTE -> setDownVotes(this.downVotes + 1);
            case NO_REACTION -> setNoReactions(this.noReactions + 1);
        }
    }
    public void removeReaction(Reactions reactions) {
        switch (reactions) {
            case UPVOTE -> setUpVotes(this.upVotes - 1);
            case DOWN_VOTE -> setDownVotes(this.downVotes - 1);
            case NO_REACTION -> setNoReactions(this.noReactions - 1);
        }
    }
    public enum Reactions {
        UPVOTE,
        NO_REACTION,
        DOWN_VOTE,
        EMBED_ID
    }


}
