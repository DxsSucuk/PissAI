package de.presti.pissai.discord;

import de.presti.pissai.discord.bot.BotWorker;
import de.presti.pissai.discord.bot.version.BotVersion;
import de.presti.pissai.main.PissAI;

import java.util.logging.Logger;

public class Main {

    static Main instance;

    Logger logger;

    public static void main(String[] args) {
        instance = new Main();
        instance.logger = Logger.getGlobal();

        try {
            PissAI.create();
        } catch (Exception exception) {
            instance.logger.severe("Couldn't create Piss-AI Instance, " + exception.getMessage());
            System.exit(-1);
        }

        try {
            BotWorker.createBot("", BotVersion.DEV, "1.0.0");
        } catch (Exception exception) {
            instance.logger.severe("Couldn't create BotInstance, " + exception.getMessage());
        }

    }

    public static Main getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }
}
