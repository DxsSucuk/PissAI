package de.presti.pissai.discord.events;

import ai.djl.modality.cv.ImageFactory;
import com.google.gson.JsonObject;
import de.presti.pissai.discord.bot.BotWorker;
import de.presti.pissai.main.PissAI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;

public class EventHandler extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);

        if (PissAI.getInstance() != null && !event.getUser().isBot() && event.getUser().getAvatarUrl() != null) {
            try {
                JsonObject jsonObject = PissAI.getInstance().checkImage(ImageFactory.getInstance().fromUrl(event.getUser().getAvatarUrl()), PissAI.getInstance().getModel(), PissAI.getInstance().getSyncs());

                if (jsonObject.has("probability") && jsonObject.get("probability").getAsFloat() >= 0.87 && jsonObject.has("className")) {

                    String detectedName = jsonObject.get("className").getAsString();
                    float probability = jsonObject.get("probability").getAsFloat();

                    event.getUser().openPrivateChannel().queue(privateChannel -> {
                        long startTime = System.currentTimeMillis();
                        long endTime = startTime + Duration.ofSeconds(30).toMillis();

                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setColor(Color.RED);
                        embedBuilder.setTitle("Piss AI");
                        embedBuilder.setDescription("""
                                Hello there, I am pretty sure that you have a Dream related profile picture!
                                Which is not wanted here, so please leave this Discord Server or be kicked, incase this is a false detection please contact a Staff.
                                """);
                        embedBuilder.addField("**Time until kick:**", Duration.ofMillis(endTime - startTime).toSeconds() + "s", true);
                        embedBuilder.addField("**Detected:**", (detectedName.charAt(0) + "").toUpperCase() + detectedName.substring(1), true);
                        embedBuilder.addField("**Probability:**", Math.round(probability * 100) + "%", true);
                        if(event.getUser().getAvatarUrl() != null) embedBuilder.setThumbnail(event.getUser().getAvatarUrl());
                        embedBuilder.setFooter("Piss AI - v" + BotWorker.getBuild());

                        Thread userCheckThread = new Thread(() -> {
                            long startTimeThread = System.currentTimeMillis();
                            long endTimeThread = startTimeThread + Duration.ofSeconds(30).toMillis();

                            User user = privateChannel.getUser();
                            Guild guild = event.getGuild();
                            Message message = privateChannel.sendMessageEmbeds(embedBuilder.build()).complete();

                            while (endTimeThread > System.currentTimeMillis()) {
                                long leftSeconds = Duration.ofMillis(endTimeThread - System.currentTimeMillis()).toSeconds();
                                if (leftSeconds < 0) leftSeconds = 0;
                                EmbedBuilder newEmbedBuilder = new EmbedBuilder();
                                newEmbedBuilder.setColor(Color.RED);
                                newEmbedBuilder.setTitle("Piss AI");
                                newEmbedBuilder.setDescription("""
                                Hello there, I am pretty sure that you have a Dream related profile picture!
                                Which is not wanted here, so please leave this Discord Server or be kicked, incase this is a false detection please contact a Staff.
                                """);
                                newEmbedBuilder.addField("**Time until kick:**", leftSeconds + "s", true);
                                newEmbedBuilder.addField("**Detected:**", (detectedName.charAt(0) + "").toUpperCase() + detectedName.substring(1), true);
                                newEmbedBuilder.addField("**Probability:**", Math.round(probability * 100) + "%", true);
                                newEmbedBuilder.setFooter("Piss AI - v" + BotWorker.getBuild());
                                if(user != null && user.getAvatarUrl() != null) newEmbedBuilder.setThumbnail(user.getAvatarUrl());

                                message.editMessageEmbeds(newEmbedBuilder.build()).complete();

                                try {
                                    Thread.sleep(2000);
                                } catch (Exception ignored) {}
                            }

                            if (user != null)
                                guild.kick(user).queue();
                        });

                        userCheckThread.setName(event.getUser().getId() + "-CheckerThread");
                        userCheckThread.start();
                    });
                }

                System.out.println("User with " + (jsonObject.has("className") ? jsonObject.get("className").getAsString() : "Unknown") + " PB joined!");
            } catch (Exception exception) {
                // TODO handle.
            }
        }
    }
}
