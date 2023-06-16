package de.presti.pissai.discord;

import de.presti.pissai.discord.events.EventHandler;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.version.BotVersion;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {
        try {
            BotWorker.createBot(BotVersion.DEVELOPMENT_BUILD);
            BotWorker.addEvent(new EventHandler());
        } catch (Exception exception) {
            log.error("Couldn't create BotInstance, " + exception.getMessage());
        }
    }
}
