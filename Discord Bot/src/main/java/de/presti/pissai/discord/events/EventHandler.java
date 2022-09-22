package de.presti.pissai.discord.events;

import ai.djl.modality.cv.ImageFactory;
import de.presti.pissai.discord.bot.BotWorker;
import de.presti.pissai.main.PissAI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Duration;

public class EventHandler extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);
        checkUser(event.getUser(), event.getGuild());
    }

    @Override
    public void onGuildMemberUpdateAvatar(@NotNull GuildMemberUpdateAvatarEvent event) {
        super.onGuildMemberUpdateAvatar(event);
        checkUser(event.getUser(), event.getGuild());
    }

    public void checkUser(User user, Guild guild) {
        if (PissAI.getInstance() != null && !user.isBot() && user.getAvatarUrl() != null) {
            try {

                final float[] probability = {PissAI.getInstance().checkImage(ImageFactory.getInstance().fromUrl(user.getAvatarUrl()))};

                if (probability[0] >= 0.87) {

                    user.openPrivateChannel().queue(privateChannel -> {
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
                        embedBuilder.addField("**Detected:**", "Dream", true);
                        embedBuilder.addField("**Probability:**", Math.round(probability[0] * 100) + "%", true);

                        String imageUrl = "";

                        if (user.getAvatarUrl() != null)
                            embedBuilder.setThumbnail(imageUrl = user.getAvatarUrl());
                        embedBuilder.setFooter("Piss AI - v" + BotWorker.getBuild());
                        String finalImageUrl = imageUrl;
                        Thread userCheckThread = new Thread(() -> {
                            long startTimeThread = System.currentTimeMillis();
                            long endTimeThread = startTimeThread + Duration.ofSeconds(30).toMillis();

                            User privateChannelUser = privateChannel.getUser();
                            Message message = privateChannel.sendMessageEmbeds(embedBuilder.build()).setActionRow(new ButtonImpl("report", "Report false positiv.",
                                    ButtonStyle.LINK, "https://presti.me", false, null)).complete();


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
                                newEmbedBuilder.addField("**Detected:**", "Dream", true);
                                newEmbedBuilder.addField("**Probability:**", Math.round(probability[0] * 100) + "%", true);
                                newEmbedBuilder.setFooter("Piss AI - v" + BotWorker.getBuild());
                                if (finalImageUrl != null && !finalImageUrl.isEmpty())
                                    newEmbedBuilder.setThumbnail(finalImageUrl);

                                message.editMessageEmbeds(newEmbedBuilder.build()).setActionRow(new ButtonImpl("report", "Report false positiv.",
                                        ButtonStyle.LINK, "https://presti.me", false, null)).complete();

                                try {
                                    Thread.sleep(2000);
                                } catch (Exception ignored) {
                                }
                            }

                            if (privateChannelUser != null) {
                                try {
                                    probability[0] = PissAI.getInstance().checkImage(ImageFactory.getInstance().fromUrl(privateChannelUser.getAvatarUrl()));
                                    System.out.println("User with Dream PB changed, I am about, " + Math.round(probability[0] * 100) + "%! - " + privateChannelUser.getAsTag());
                                    if (probability[0] >= 0.87) {
                                        guild.kick(user).queue();
                                    } else {
                                        EmbedBuilder newEmbedBuilder = new EmbedBuilder();
                                        newEmbedBuilder.setColor(Color.GREEN);
                                        newEmbedBuilder.setTitle("Piss AI");
                                        newEmbedBuilder.setDescription("""
                                                Thank you for understanding!
                                                You now have permission to stay, have a good day!
                                                """);
                                        newEmbedBuilder.addField("**Time until kick:**", "Dream Image appears again.", true);
                                        newEmbedBuilder.addField("**Detected:**", "A father having child.", true);
                                        newEmbedBuilder.addField("**Probability:**", "100%", true);
                                        newEmbedBuilder.setThumbnail(privateChannelUser.getJDA().getSelfUser().getAvatarUrl());
                                        newEmbedBuilder.setFooter("Piss AI - v" + BotWorker.getBuild());
                                        message.editMessageEmbeds(newEmbedBuilder.build()).setActionRow(new ButtonImpl("report", "Report false positiv.",
                                                ButtonStyle.LINK, "https://presti.me", false, null)).complete();
                                    }
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                            }
                        });

                        userCheckThread.setName(user.getId() + "-CheckerThread");
                        userCheckThread.start();
                    });
                }

                System.out.println("User with Dream PB joined, I am about, " + Math.round(probability[0] * 100) + "%! - " + user.getAsTag());
            } catch (Exception exception) {
                // TODO handle.
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        super.onButtonInteraction(event);
    }
}
