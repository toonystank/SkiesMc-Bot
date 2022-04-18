package com.taggernation.skiesmcbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.AttributedString;
import java.util.Objects;

public class Test extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getMessage().getContentRaw().startsWith("!test")) return;
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) return;

        BufferedImage image = null;
        try {
            image = ImageIO.read(new URL("https://c4.wallpaperflare.com/wallpaper/948/782/354/minecraft-minecraft-dungeons-ocean-view-minecraft-dungeons-hidden-depths-4k-hd-wallpaper-preview.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert image != null;
        Font font = new Font("Arial", Font.PLAIN, 30);
        String text = event.getMessage().getContentRaw().replace("!test ", "");
        Graphics imageGraphics = image.getGraphics();
        AttributedString attributedText = new AttributedString(text);
        attributedText.addAttribute(TextAttribute.FONT, font);
        attributedText.addAttribute(TextAttribute.FOREGROUND, Color.GREEN);
        attributedText.addAttribute(TextAttribute.BACKGROUND, Color.BLACK);
        attributedText.addAttribute(TextAttribute.BIDI_EMBEDDING, Color.GREEN);

        FontMetrics metrics = imageGraphics.getFontMetrics(font);
        int positionX = (image.getWidth() - metrics.stringWidth(text)) / 10;
        int positionY = (image.getHeight() - metrics.getHeight()) / 4 + metrics.getAscent();
        imageGraphics.drawString(attributedText.getIterator(), positionX, positionY);
        File outfile = new File("test.png");
        try {
            ImageIO.write(image, "png", outfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        event.getChannel().sendMessage(attributedText.toString()).addFile(outfile).queue();

    }
}
