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
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import net.dv8tion.jda.internal.interactions.component.ModalImpl;
import net.dv8tion.jda.internal.interactions.component.TextInputImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Duration;

public class EventHandler extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);

        if (PissAI.getInstance() != null && !event.getUser().isBot() && event.getUser().getAvatarUrl() != null) {
            try {
                //JsonObject jsonObject = PissAI.getInstance().checkImage(ImageFactory.getInstance().fromUrl(event.getUser().getAvatarUrl()), PissAI.getInstance().getModel(), PissAI.getInstance().getSyncs());

                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("probability", 1);
                jsonObject.addProperty("className", "Balls");

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

                        String imageUrl = "";

                        if (event.getUser().getAvatarUrl() != null)
                            embedBuilder.setThumbnail(imageUrl = event.getUser().getAvatarUrl());
                        embedBuilder.setFooter("Piss AI - v" + BotWorker.getBuild());
                        String finalImageUrl = imageUrl;
                        Thread userCheckThread = new Thread(() -> {
                            long startTimeThread = System.currentTimeMillis();
                            long endTimeThread = startTimeThread + Duration.ofSeconds(30).toMillis();

                            User user = privateChannel.getUser();
                            Guild guild = event.getGuild();
                            Message message = privateChannel.sendMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(new ButtonImpl("report", "Report false positiv.",
                                    ButtonStyle.LINK, "https://presti.me", false, null))).complete();


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
                                if (finalImageUrl != null && !finalImageUrl.isEmpty())
                                    newEmbedBuilder.setThumbnail(finalImageUrl);

                                message.editMessageEmbeds(newEmbedBuilder.build()).setActionRows(ActionRow.of(new ButtonImpl("report", "Report false positiv.",
                                        ButtonStyle.LINK, "https://presti.me", false, null))).complete();

                                try {
                                    Thread.sleep(2000);
                                } catch (Exception ignored) {
                                }
                            }

                            if (user != null) {
                                try {
                                    // JsonObject jsonObject2 = PissAI.getInstance().checkImage(ImageFactory.getInstance().fromUrl(event.getUser().getAvatarUrl()), PissAI.getInstance().getModel(), PissAI.getInstance().getSyncs());

                                    JsonObject jsonObject2 = new JsonObject();

                                    jsonObject.addProperty("probability", 0.73);
                                    jsonObject.addProperty("className", "Balls");

                                    if (jsonObject2.has("probability") && jsonObject2.get("probability").getAsFloat() >= 0.87 && jsonObject2.has("className")) {
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
                                        newEmbedBuilder.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl());
                                        newEmbedBuilder.setFooter("Piss AI - v" + BotWorker.getBuild());
                                        message.editMessageEmbeds(newEmbedBuilder.build()).setActionRows(ActionRow.of(new ButtonImpl("report", "Report false positiv.",
                                                ButtonStyle.LINK, "https://presti.me", false, null))).complete();
                                    }
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                            }
                        });

                        userCheckThread.setName(event.getUser().getId() + "-CheckerThread");
                        userCheckThread.start();
                    });
                }

                System.out.println("User with " + (jsonObject.has("className") ? jsonObject.get("className").getAsString() : "Unknown") + " PB joined!");
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
