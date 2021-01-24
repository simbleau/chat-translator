package com.chattranslator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A data holder for information extracted from the chat used for translation.
 *
 * @version January 2021
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 */
class ChatLineData {

    /**
     * The RuneScape name of the player being translated
     */
    private String rsn;

    /**
     * The chat line text being translated.
     */
    private final String chatLine;

    /**
     * Whether the chat line text was sent by the local player.
     */
    private final boolean isLocalPlayer;

    /**
     * Store chatline data
     *
     * @param rsn - the runescape name of the player who sent the message (or null)
     * @param chatLine - the text on the chatline
     * @param isLocalPlayer - whether the chatline came from the local player
     */
    public ChatLineData(@Nullable String rsn, @Nonnull String chatLine, boolean isLocalPlayer) {
        this.rsn = rsn;
        this.chatLine = chatLine;
        this.isLocalPlayer = isLocalPlayer;
    }

    /**
     * Return whether the message was (probably) sent by the system, not a player.
     *
     * @return true if the message was not sent by a player, false otherwise
     */
    public boolean isGameMessage() {
        return this.rsn == null;
    }

    /**
     * Return whether the message was sent by a player.
     *
     * @return true if the message was sent by a player, false otherwise
     */
    public boolean isSaidByPlayer() {
        return this.rsn != null;
    }

    /**
     * Return whether the message was sent by the local player.
     *
     * @return true if the message was sent by the local player, false otherwise
     */
    public boolean isSaidByLocalPlayer() {
        return this.isLocalPlayer;
    }

    /**
     * @return the runescape name of the player being translated
     */
    public @Nullable
    String getRSN() {
        return this.rsn;
    }

    /**
     * @return the text being translated
     */
    public String getChatLine() {
        return chatLine;
    }

    /**
     * Fixes the RuneScape Name for messages exclusively in the Private Chat filter. These messages only appear to start with "From RSN: Message" or "To RSN: Message" only when the user is currently filtering the private chat.
     */
    public void fixRsnForPMFilter() {
        if (this.rsn.startsWith("To ")) {
            this.rsn = this.rsn.substring(3);
        } else if (this.rsn.startsWith("From ")) {
            this.rsn = this.rsn.substring(5);
        }
    }
}