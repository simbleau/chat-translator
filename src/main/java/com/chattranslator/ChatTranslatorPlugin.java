package com.chattranslator;

import com.chattranslator.data.TranslateTextResponseList;
import com.chattranslator.data.TranslateTextResponseTranslation;
import com.chattranslator.ex.GoogleAPIException;
import com.chattranslator.ex.GoogleException;
import com.chattranslator.ui.ChatTranslatorPanel;
import com.google.inject.Provides;

import javax.annotation.Nullable;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.*;
import net.runelite.api.vars.InputType;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A plugin to translate chat text.
 *
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 * @version January 2021
 */
@Slf4j
@PluginDescriptor(
        name = "Chat Translator",
        description = "Translate chat messages from OldSchool RuneScape",
        tags = {"chat", "translator", "translate", "language"}
)
public class ChatTranslatorPlugin extends Plugin {

    /**
     * The navigation button on the toolbar which brings up the Chat Translator panel.
     */
    private NavigationButton navButton;

    /**
     * A menu entry buffer decorated to store translation data.
     */
    private ChatTranslatorMenuEntry menuEntry = null;

    /**
     * A buffer used to preview an interactive translation in the chat input.
     */
    private String previewTranslation = null;
    private String lastPreviewText = null;

    /**
     * The user interface panel.
     */
    @Inject
    private ChatTranslatorPanel panel;

    /**
     * The game client.
     */
    @Inject
    private Client client;

    /**
     * The client thread.
     */
    @Inject
    private ClientThread clientThread;

    /**
     * The translator for the plugin.
     */
    @Inject
    private ChatTranslator translator;

    /**
     * The runelite side toolbar.
     */
    @Inject
    private ClientToolbar clientToolbar;

    /**
     * The config for the the plugin.
     */
    @Inject
    private ChatTranslatorConfig config;

    @Provides
    private ChatTranslatorConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ChatTranslatorConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        //If we have credentials, attempt to authenticate now.
        if (this.config.apiKey() != null) {
            try {
                this.translator.authenticateFromConfig();
            } catch (GoogleException e) {
                log.warn("Authentication from config failed", e);
                this.translator.unauthenticate(); // Clear auth if auth fails.
            }
        }

        // Initialize the panel
        this.panel = injector.getInstance(ChatTranslatorPanel.class);
        if (this.translator.isAuthenticated()) {
            this.panel.bodyPanel.langPanel.enableOptions(this.translator.getSupportedLanguages());
            this.loadLastSettings();
        } else {
            this.panel.bodyPanel.langPanel.disableOptions();
        }

        // Add the panel nav button to the client toolbar
        final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "/panel_icon.png");
        this.navButton = NavigationButton.builder()
                .tooltip("Chat Translator")
                .icon(icon)
                .priority(7)
                .panel(this.panel)
                .build();
        // Add the nav button to the toolbar, which navigates to the chat translator panel
        this.clientToolbar.addNavigation(this.navButton);
    }

    @Override
    protected void shutDown() {
        // Remove the nav button from the toolbar
        this.clientToolbar.removeNavigation(this.navButton);
    }

    public void loadLastSettings() {
        // Load the last source language and target language into the language panel
        this.panel.bodyPanel.langPanel.setSourceLanguage(this.config.lastSourceLanguageCode());
        this.panel.bodyPanel.langPanel.setTargetLanguage(this.config.lastTargetLanguageCode());
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event) throws Exception {
        if (!this.config.isStandardTranslationEnabled()) return;

        if (isHoveringChatBoxWidget()) {
            // If the user isn't hovering their chat buffer or a message, end here
            if (!isHoveringChatInputWidget() && !isHoveringChatLineWidget()) {
                this.menuEntry = null;
                return;
            }

            // Inject the translate menu entry
            this.menuEntry = new ChatTranslatorMenuEntry(config);
            menuEntry.setType(MenuAction.RUNELITE.getId());
            menuEntry.setTarget("");
            ChatLineData chatData = null;
            if (isHoveringChatInputWidget()) {
                chatData = getLocalPlayerChatLineData();
                menuEntry.setChatLineData(chatData);
                menuEntry.setSourceLanguage(this.config.lastSourceLanguageCode(), this.config.lastSourceLanguageName());
                menuEntry.setTargetLanguage(this.config.lastTargetLanguageCode(), this.config.lastTargetLanguageName());
            } else if (isHoveringChatLineWidget()) {
                chatData = getHoveredChatLineData();
                menuEntry.setChatLineData(chatData);
                menuEntry.setTargetLanguage(this.config.lastSourceLanguageCode(), this.config.lastSourceLanguageName());
            }
            if (chatData == null || chatData.getChatLine().isEmpty()) {
                return;
            }
            client.setMenuEntries(ArrayUtils.insert(1, client.getMenuEntries(), menuEntry));
        } else {
            this.menuEntry = null;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws Exception {
        if (event.getMenuAction() == MenuAction.RUNELITE && event.getMenuOption().startsWith("Translate")) {
            new Thread(() -> {
                try {
                    log.info("Translating "
                            + (menuEntry.getSourceLanguageCode() == null ? "auto" : menuEntry.getSourceLanguageCode())
                            + " to "
                            + menuEntry.getTargetLanguageCode());
                    TranslateTextResponseList translationList = translator.translate(
                            menuEntry.getChatLineData().getChatLine(),
                            menuEntry.getSourceLanguageCode(),
                            menuEntry.getTargetLanguageCode());
                    if (translationList.isEmpty()) {
                        throw new GoogleAPIException("No translations found");
                    }
                    TranslateTextResponseTranslation translationResponse = translationList.getBestTranslation(config.lastTargetLanguageCode());
                    String translation = translationResponse.translatedText;
                    String fromLanguage = translationResponse.detectedSourceLanguage.toUpperCase();
                    String toLanguage = menuEntry.getTargetLanguageCode().toUpperCase();
                    log.info("Translation Complete."
                            + "\n\tBefore[" + fromLanguage + "]: '" + menuEntry.getChatLineData().getChatLine() + "'"
                            + "\n\tAfter[" + toLanguage + "]: '" + translation + "'");

                    clientThread.invokeLater(() -> {
                        // Copy items for translation formatting
                        String translationFormatted = translation;
                        String fromLanguageFormatted = fromLanguage;
                        String toLanguageFormatted = toLanguage;

                        // Preview the translation in chat input
                        if (this.config.isPreviewingChatInput() && menuEntry.getChatLineData().isSaidByLocalPlayer()) {
                            stagePreview(translationFormatted);
                        }

                        // Highlighting for chat box
                        if (config.isTranslationHighlighted()) {
                            // Color from language code
                            if (fromLanguageFormatted.equalsIgnoreCase(config.lastSourceLanguageCode())) {
                                fromLanguageFormatted = ColorUtil.wrapWithColorTag(fromLanguageFormatted, config.sourceLangColor());
                            } else if (fromLanguageFormatted.equalsIgnoreCase(config.lastTargetLanguageCode())) {
                                fromLanguageFormatted = ColorUtil.wrapWithColorTag(fromLanguageFormatted, config.targetLangColor());
                            }

                            // Color to language code
                            if (toLanguageFormatted.equalsIgnoreCase(config.lastSourceLanguageCode())) {
                                toLanguageFormatted = ColorUtil.wrapWithColorTag(toLanguageFormatted, config.sourceLangColor());
                                translationFormatted = ColorUtil.wrapWithColorTag(translationFormatted, config.sourceLangColor());
                            } else if (toLanguageFormatted.equalsIgnoreCase(config.lastTargetLanguageCode())) {
                                toLanguageFormatted = ColorUtil.wrapWithColorTag(toLanguageFormatted, config.targetLangColor());
                                translationFormatted = ColorUtil.wrapWithColorTag(translationFormatted, config.targetLangColor());
                            }
                        }

                        // Send translation message
                        ChatMessageType messageFilter = getVisibleChatMessageType();
                        switch (messageFilter) {
                            case PUBLICCHAT:
                                sendTranslationToPublicChat(fromLanguageFormatted, toLanguageFormatted, translationFormatted, menuEntry);
                                break;
                            case FRIENDSCHAT:
                                sendTranslationToFriendsChat(fromLanguageFormatted, toLanguageFormatted, translationFormatted, menuEntry);
                                break;
                            case TRADE:
                                sendTranslationToTradeChat(fromLanguageFormatted, toLanguageFormatted, translationFormatted, menuEntry);
                                break;
                            case PRIVATECHAT:
                                if (menuEntry.getChatLineData().isSaidByPlayer()) {
                                    if (menuEntry.getChatLineData().getRSN().startsWith("To ")) {
                                        menuEntry.getChatLineData().fixRsnForPMFilter(); // Change "From <rsn>" to "<rsn>" - This is specific to the Private Chat filter.
                                        sendTranslationToPrivateChatOut(fromLanguageFormatted, toLanguageFormatted, translationFormatted, menuEntry);
                                        break;
                                    } else if (menuEntry.getChatLineData().getRSN().startsWith("From ")) {
                                        menuEntry.getChatLineData().fixRsnForPMFilter(); // Change "From <rsn>" to "<rsn>" - This is specific to the Private Chat filter.
                                        sendTranslationToPrivateChat(fromLanguageFormatted, toLanguageFormatted, translationFormatted, menuEntry);
                                        break;
                                    }
                                }
                                sendTranslationToPrivateChat(fromLanguageFormatted, toLanguageFormatted, translationFormatted, menuEntry);
                                break;
                            case GAMEMESSAGE:
                            default:
                                sendTranslationToGameChat(fromLanguageFormatted, toLanguageFormatted, translationFormatted, menuEntry);
                                break;
                        }
                    });

                } catch (Exception e) {
                    log.error("Translation exception: ", e);
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Translation Error: " + e.getMessage(), "");
                }
            }).start();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) throws Exception {
        if (configChanged.getKey().equals("previewChatInput")) {
            if (!config.isPreviewingChatInput()) {
                stopPreview();
            }
        }
    }

    @Subscribe
    public void onVarClientStrChanged(VarClientStrChanged varClientStrChanged) throws Exception {
        // Return if the user does not want to preview chat
        if (!config.isPreviewingChatInput()) return;

        // Return if there is nothing to preview
        if (this.previewTranslation == null) return;

        // Update the preview
        if (varClientStrChanged.getIndex() == VarClientStr.CHATBOX_TYPED_TEXT.getIndex()) {
            String userInput = client.getVar(VarClientStr.CHATBOX_TYPED_TEXT);

            // Conditions to cancel a preview when typing:
            // 1 : User sends the chatline
            // 2 : User hits backspace when the chatline is already empty
            if (!this.lastPreviewText.isEmpty() && userInput.isEmpty()) {
                clientThread.invokeLater(() -> stopPreview());
                return;
            }
            // 3 : User input length is longer than the translation length
            if (userInput.length() > previewTranslation.length()) {
                clientThread.invokeLater(() -> stopPreview());
                return;
            }
            // 4 : The last several characters in a row entered do not match the translation
            final int incorrectMax = 5;
            if (userInput.length() >= incorrectMax && previewTranslation.length() >= incorrectMax) {
                int incorrect = 0;
                for (int i = 0; i < incorrectMax; i++) {
                    char correct = Character.toLowerCase(previewTranslation.charAt(userInput.length() - i - 1));
                    char user = Character.toLowerCase(userInput.charAt(userInput.length() - i - 1));
                    if (user != correct) {
                        incorrect++;
                    }
                }
                if (incorrect == incorrectMax) {
                    clientThread.invokeLater(() -> stopPreview());
                    return;
                }
            }

            this.lastPreviewText = userInput;
            clientThread.invokeLater(() -> writeChatInput(getChatInputPreviewText()));
        }

    }

    /**
     * @return the panel
     */
    public ChatTranslatorPanel getPanel() {
        return this.panel;
    }

    /**
     * Helper method to determine if the user is hovering over a widget.
     *
     * @param widget - the widget to mouse test
     * @return true if the user is hovering over the given widget, false otherwise
     */
    private boolean isMouseOverWidget(Widget widget) {
        Point mousePosition = this.client.getMouseCanvasPosition();
        return widget.getBounds().contains(mousePosition.getX(), mousePosition.getY());
    }

    /**
     * Helper method to determine if the user is hovering over the chat box.
     *
     * @return true if the user is hovering over the chat box, false otherwise
     */
    private boolean isHoveringChatBoxWidget() {
        Widget chatBoxWidget = this.client.getWidget(WidgetInfo.CHATBOX);
        if (chatBoxWidget == null) return false;
        return isMouseOverWidget(chatBoxWidget);
    }

    /**
     * Helper method to determine if the user is hovering over their own chat input.
     *
     * @return true if the user is hovering over their own chat input, false otherwise
     */
    private boolean isHoveringChatInputWidget() {
        Widget chatInputWidget = this.client.getWidget(WidgetInfo.CHATBOX_INPUT);
        if (chatInputWidget == null) return false;
        return isMouseOverWidget(chatInputWidget);
    }

    /**
     * Helper method to determine if the user is hovering over a chat line in the chat box.
     *
     * @return true if the user is hovering over a message in the chat box, false otherwise
     */
    private boolean isHoveringChatLineWidget() {
        Widget chatLinesWidget = this.client.getWidget(WidgetInfo.CHATBOX_MESSAGE_LINES);
        if (chatLinesWidget == null) return false;
        return isMouseOverWidget(chatLinesWidget);
    }

    /**
     * Helper method to return the chat line data from the message underneath the mouse
     *
     * @return chat line data from the message underneath the mouse, or null on error
     */
    private @Nullable
    ChatLineData getHoveredChatLineData() {
        try {
            Widget chatBox = this.client.getWidget(WidgetInfo.CHATBOX_MESSAGE_LINES);
            // This magic will get all of the chat lines in the chat box and filter it down to the chat line hovered over
            // by the mouse and then remove all formatting and join the "username:" widget text to the " message" widget text.
            String chatLine = Stream.of(chatBox.getChildren())
                    .filter(widget -> !widget.isHidden())
                    .filter(widget -> widget.getId() < WidgetInfo.CHATBOX_FIRST_MESSAGE.getId())
                    .filter(widget -> {
                        int mouseY = this.client.getMouseCanvasPosition().getY();
                        return (mouseY >= widget.getBounds().getMinY() && mouseY <= widget.getBounds().getMaxY());
                    })
                    .map(Widget::getText)
                    .map(Text::removeTags)
                    .collect(Collectors.joining(" "));

            // Regex matcher buffer
            Matcher matcher;

            // Remove Friends Chat heading, i.e. '[Friends Chat] Nuzzler: Hey' -> 'Nuzzler: Hey'
            Pattern fcPattern = Pattern.compile("^\\[.+\\] ");
            matcher = fcPattern.matcher(chatLine);
            if (matcher.find()) {
                String fc = matcher.group();
                chatLine = chatLine.replace(fc, ""); // Remove FC heading
            }

            // Capture username
            Pattern rsnPattern = Pattern.compile("^.+: ");
            String username = null;
            matcher = rsnPattern.matcher(chatLine);
            if (matcher.find()) {
                username = matcher.group();
                chatLine = chatLine.replace(username, ""); // Remove FC heading
                username = username.substring(0, username.length() - 2); // Remove the ': ' at the end
            }

            return new ChatLineData(username, chatLine, false);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper method to return the chat line data in the local player's chat input
     *
     * @return chat line data from the local player, or null on error
     */
    private @Nullable
    ChatLineData getLocalPlayerChatLineData() {
        try {
            String rsn = this.client.getLocalPlayer().getName();
            String chatInput = client.getVar(VarClientStr.CHATBOX_TYPED_TEXT);
            return new ChatLineData(rsn, chatInput, true);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper method to return a {@link ChatMessageType} that the player will see, even in their given filter.
     * For example, if the player is currently filtering to 'Trade' chat, we return {@link ChatMessageType#TRADE}.
     * This helps send a translation where the player can see it.
     *
     * @return a {@link ChatMessageType} to reach the player in their given filter
     */
    private ChatMessageType getVisibleChatMessageType() {
        try {
            Widget chatBox = this.client.getWidget(WidgetInfo.CHATBOX_BUTTONS);

            List<Widget> chatBuckets = Stream.of(chatBox.getStaticChildren())
                    .map(Widget::getStaticChildren)
                    .flatMap(Arrays::stream)
                    .map(Widget::getStaticChildren)
                    .flatMap(Arrays::stream)
                    .filter(widget -> !widget.isHidden())
                    .filter(widget -> widget.getSpriteId() == 1022)
                    .collect(Collectors.toList());

            if (chatBuckets.size() > 0) {
                Widget activePebble = chatBuckets.get(0);
                Widget tabPebble = activePebble.getParent().getParent();
                if (this.client.getWidget(WidgetInfo.CHATBOX_TAB_CLAN).equals(tabPebble)) {
                    return ChatMessageType.FRIENDSCHAT;
                } else if (this.client.getWidget(WidgetInfo.CHATBOX_TAB_PRIVATE).equals(tabPebble)) {
                    return ChatMessageType.PRIVATECHAT;
                } else if (this.client.getWidget(WidgetInfo.CHATBOX_TAB_PUBLIC).equals(tabPebble)) {
                    return ChatMessageType.PUBLICCHAT;
                } else if (this.client.getWidget(WidgetInfo.CHATBOX_TAB_TRADE).equals(tabPebble)) {
                    return ChatMessageType.TRADE;
                }
            }
        } catch (Exception e) {
            // Do nothing
        }
        // Default to a game message
        return ChatMessageType.GAMEMESSAGE;
    }

    /**
     * Helper method to stage a translation preview for the user. This must be called from the client thread.
     *
     * @param translation - the translation to preview for the user in their chat input
     */
    private void stagePreview(String translation) {
        try {
            //  Clear the chat input
            int inputType = client.getVar(VarClientInt.INPUT_TYPE);
            if (inputType == InputType.NONE.getType()) {
                // Clear the chat input in preparation for chat preview
                client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, "");
                client.runScript(ScriptID.CHAT_PROMPT_INIT);
            } else {
                // User is typing in private chat or in a dialog, etc.
                // The preview cannot be done
                return;
            }

            // Stage the preview
            this.previewTranslation = translation;
            this.lastPreviewText = "";

            writeChatInput(getChatInputPreviewText());
            log.debug("Started translation preview of '" + translation + "'");
        } catch (Exception e) {
            log.error("Translation could not be staged", e);
        }
    }

    /**
     * Helper method to cancel the translation preview for the user. This must be called from the client thread.
     */
    private void stopPreview() {
        try {
            // Clear preview
            this.previewTranslation = null;
            this.lastPreviewText = null;

            // Return back to the normal state
            String chatInput = client.getVar(VarClientStr.CHATBOX_TYPED_TEXT);
            final Color defaultTextColor = new Color(0x90, 0x90, 0xff);
            writeChatInput(ColorUtil.wrapWithColorTag(chatInput, defaultTextColor));
            log.debug("Stopped translation preview.");
        } catch (Exception e) {
            log.error("Translation could not be cancelled", e);
        }
    }

    /**
     * Helper method to put raw chat data into the chat input widget.
     * For example, if the desired text is "Lala", similar to the following will be returned:
     * <pre>
     *     Username: Lala<col=default>*</col>
     * </pre>
     *
     * @param text - the desired chat content to be inserted into the chat input widget
     */
    private void writeChatInput(String text) {
        try {
            // Replace the visible chatline input with a preview
            Widget chatBuffer = client.getWidget(WidgetInfo.CHATBOX_INPUT);

            StringBuilder rawChatInput = new StringBuilder();
            rawChatInput.append(client.getLocalPlayer().getName());
            rawChatInput.append(": ");
            rawChatInput.append(text);
            final Color defaultTextColor = new Color(0x90, 0x90, 0xff);
            rawChatInput.append(ColorUtil.wrapWithColorTag("*", defaultTextColor)); // The asterisk at the end

            chatBuffer.setText(rawChatInput.toString());
        } catch (Exception e) {
            log.error("Could not write chat input", e);
        }
    }


    /**
     * Helper method to return the raw chat data of the current preview.
     * For example, if the desired preview text is "Hej" (Hello), and the user has inputted "hZ", similar to the following will be returned:
     * <pre>{@code
     *     <col=green>h</col><col=red>Z</col><col=grey>j</col>
     * }</pre>
     * Where green is the correct color, red is the incorrect color, and grey is a not-typed character color.
     *
     * @return the raw chat data for a preview of the current translation attempt
     */
    private @Nullable
    String getChatInputPreviewText() {
        try {
            String userInput = client.getVar(VarClientStr.CHATBOX_TYPED_TEXT);

            StringBuilder translationPreviewColoring = new StringBuilder();
            for (int i = 0; i < previewTranslation.length(); i++) {
                Character correct = previewTranslation.charAt(i);

                if (i < userInput.length()) {
                    // User typed a character at this index
                    Character user = userInput.charAt(i);
                    if (Character.toLowerCase(user) == Character.toLowerCase(correct)) {
                        // With highlighting
                        if (this.config.isTranslationHighlighted()) {
                            translationPreviewColoring.append(ColorUtil.wrapWithColorTag(Character.toString(user), config.targetLangColor()));
                        } else {
                            final Color defaultTextColor = new Color(0x90, 0x90, 0xff);
                            translationPreviewColoring.append(ColorUtil.wrapWithColorTag(Character.toString(user), defaultTextColor));
                        }
                    } else {
                        translationPreviewColoring.append(ColorUtil.wrapWithColorTag(Character.toString(user), Color.RED));
                    }
                } else {
                    // User did not type a character at this index
                    translationPreviewColoring.append(ColorUtil.wrapWithColorTag(Character.toString(correct), Color.GRAY));
                }
            }

            return translationPreviewColoring.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper method to send a translation message to public chat. This is done because a user is filtering that chat and we want them to see the translation under the same filter.
     */
    private void sendTranslationToPublicChat(String fromLanguageCode, String toLanguageCode, String translation, ChatTranslatorMenuEntry menuEntry) {
        client.addChatMessage(ChatMessageType.PUBLICCHAT,
                "[" + (this.config.isShowingDetectedLanguages() ? fromLanguageCode + "->" + toLanguageCode : toLanguageCode) + "] "
                        + (menuEntry.getChatLineData().isSaidByPlayer() ? menuEntry.getChatLineData().getRSN() : "GAME"),
                "</col>" + translation,
                "xx");
    }

    /**
     * Helper method to send a translation message to private chat. This is done because a user is filtering that chat and we want them to see the translation under the same filter.
     */
    private void sendTranslationToPrivateChat(String fromLanguageCode, String toLanguageCode, String translation, ChatTranslatorMenuEntry menuEntry) {
        client.addChatMessage(ChatMessageType.PRIVATECHAT,
                (menuEntry.getChatLineData().isSaidByPlayer() ? menuEntry.getChatLineData().getRSN() : "GAME"),
                "</col>[" + (this.config.isShowingDetectedLanguages() ? fromLanguageCode + "->" + toLanguageCode : toLanguageCode) + "] " + translation,
                "");
    }

    /**
     * Helper method to send a translation message to public chat as the user. This is done because a user is filtering that chat and we want them to see the translation under the same filter.
     */
    private void sendTranslationToPrivateChatOut(String fromLanguageCode, String toLanguageCode, String translation, ChatTranslatorMenuEntry menuEntry) {
        client.addChatMessage(ChatMessageType.PRIVATECHATOUT,
                (menuEntry.getChatLineData().isSaidByPlayer() ? menuEntry.getChatLineData().getRSN() : "GAME"),
                "</col>[" + (this.config.isShowingDetectedLanguages() ? fromLanguageCode + "->" + toLanguageCode : toLanguageCode) + "] " + translation, "");
    }

    /**
     * Helper method to send a translation message to trade chat. This is done because a user is filtering that chat and we want them to see the translation under the same filter.
     */
    private void sendTranslationToTradeChat(String fromLanguageCode, String toLanguageCode, String translation, ChatTranslatorMenuEntry menuEntry) {
        client.addChatMessage(ChatMessageType.TRADE,
                "",
                "[" + (this.config.isShowingDetectedLanguages() ? fromLanguageCode + "->" + toLanguageCode : toLanguageCode) + "]" + (menuEntry.getChatLineData().isSaidByLocalPlayer() ? " " + menuEntry.getChatLineData().getRSN() + ": " : ": ") + translation,
                "");
    }

    /**
     * Helper method to send a translation message to friends chat. This is done because a user is filtering that chat and we want them to see the translation under the same filter.
     */
    private void sendTranslationToFriendsChat(String fromLanguageCode, String toLanguageCode, String translation, ChatTranslatorMenuEntry menuEntry) {
        client.addChatMessage(ChatMessageType.FRIENDSCHAT,
                (menuEntry.getChatLineData().isGameMessage() ? "GAME" : menuEntry.getChatLineData().getRSN()),
                "</col>" + translation,
                "</col>" + (this.config.isShowingDetectedLanguages() ? fromLanguageCode + "->" + toLanguageCode : toLanguageCode));
    }

    /**
     * Helper method to send a translation message to game chat. This is the default.
     */
    private void sendTranslationToGameChat(String fromLanguageCode, String toLanguageCode, String translation, ChatTranslatorMenuEntry menuEntry) {
        client.addChatMessage(ChatMessageType.GAMEMESSAGE,
                "",
                "[" + (this.config.isShowingDetectedLanguages() ? fromLanguageCode + "->" + toLanguageCode : toLanguageCode) + "] "
                        + (menuEntry.getChatLineData().isSaidByPlayer() ? menuEntry.getChatLineData().getRSN() + ": " : "")
                        + translation,
                "");
    }

    /*
        Never let language be a barrier, let it be a challenge to overcome.
        Golale Ahour + Spencer Imbleau
        Made with love.
     */
}
