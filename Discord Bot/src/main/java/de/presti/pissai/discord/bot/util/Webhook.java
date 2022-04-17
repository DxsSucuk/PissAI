package de.presti.pissai.discord.bot.util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessage;
import de.presti.pissai.discord.Main;

/**
 * Class to handle Webhook sends.
 */
public class Webhook {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     * @throws IllegalStateException it is a utility class.
     */
    private Webhook() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Send a Webhook-message to the wanted Webhook.
     * @param message the MessageContent.
     * @param webhookId the ID of the Webhook.
     * @param webhookToken the Auth-Token of the Webhook.
     */
    public static void sendWebhook(WebhookMessage message, long webhookId, String webhookToken) {

        // Check if the given data is valid.
        if (webhookToken.contains("Not setup!") || webhookId == 0) return;

        // Try sending a Webhook to the given data.
        try (WebhookClient wcl = WebhookClient.withId(webhookId, webhookToken)) {
            // Send the message and handle exceptions.
            wcl.send(message).exceptionally(throwable -> {
                // If the error 404 comes that means that the webhook is invalid.
                if (throwable.getMessage().contains("failure 404")) {
                    // Inform and delete invalid webhook.
                    Main.getInstance().getLogger().severe("[Webhook] Invalid Webhook: " + webhookId + " - " + webhookToken);
                } else if (throwable.getMessage().contains("failure 400")) {
                    // If 404 inform that the Message had an invalid Body.
                    Main.getInstance().getLogger().severe("[Webhook] Invalid Body!");
                }
                return null;
            });
        } catch (Exception ex) {
            // Inform that this is an Invalid Webhook.
            Main.getInstance().getLogger().severe("[Webhook] Invalid Webhook: " + webhookId + " - " + webhookToken);
            Main.getInstance().getLogger().severe("[Webhook] " +ex.getMessage());
        }
    }
}