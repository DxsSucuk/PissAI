package de.presti.pissai.discord.events;

import ai.djl.modality.cv.ImageFactory;
import com.google.gson.JsonObject;
import de.presti.pissai.main.PissAI;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class EventHandler extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);

        if (PissAI.getInstance() != null) {
            try {
                JsonObject jsonObject = PissAI.getInstance().checkImage(ImageFactory.getInstance().fromUrl(event.getUser().getAvatarUrl()), PissAI.getInstance().getModel(), PissAI.getInstance().getSyncs());

                if (jsonObject.has("probability") && jsonObject.get("probability").getAsFloat() >= 0.75) {
                    // TODO ban timer.
                    System.out.println("User with " + (jsonObject.has("className") ? jsonObject.get("className").getAsString() : "Unknown") + " PB joined!");
                }
            } catch (Exception exception) {
                // TODO handle.
            }
        }
    }
}
