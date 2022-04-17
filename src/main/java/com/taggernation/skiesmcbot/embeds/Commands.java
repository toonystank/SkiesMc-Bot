package com.taggernation.skiesmcbot.embeds;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class Commands extends ListenerAdapter {

    private final EmbedFromYML embed;
    public Commands(EmbedFromYML embed) {
        this.embed = embed;
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String[] args = event.getMessage().getContentRaw().split(" ");
        if (args[0].equalsIgnoreCase(embed.getCommand())) {
            event.getChannel().sendMessageEmbeds(embed.getEmbed()).queue();
        }
    }
}
