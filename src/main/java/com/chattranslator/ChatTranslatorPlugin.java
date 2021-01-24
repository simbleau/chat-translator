package com.chattranslator;

import com.chattranslator.ex.GoogleException;
import com.chattranslator.ui.ChatTranslatorPanel;
import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.ArrayUtils;

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
 * @version January 2021
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
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
     * A menu entry buffer used to translate text for translation.
     */
    private ChatTranslatorMenuEntry menuEntry = null;

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
        if (this.config.lastCredentials() != null) {
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
            this.panel.enableLanguagePanel(this.translator.getSupportedLanguages());
            this.panel.loadSourceLanguage(this.config.lastSourceLanguageCode());
            this.panel.loadTargetLanguage(this.config.lastTargetLanguageCode());
        } else {
            this.panel.disableLanguagePanel();
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
    protected void shutDown() throws Exception {
        // Remove the nav button from the toolbar
        this.clientToolbar.removeNavigation(this.navButton);
    }

    public void loadLastSettings() {
        this.panel.loadSourceLanguage(this.config.lastSourceLanguageCode());
        this.panel.loadTargetLanguage(this.config.lastTargetLanguageCode());
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event) {
        if (!this.config.rightClickChat()) {
            return;
        }

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
            ChatLineData message;
            if (isHoveringChatInputWidget()) {
                message = getLocalPlayerChatLineData();
                menuEntry.setChatLineData(message);
                menuEntry.setSourceLanguage(this.config.lastSourceLanguageCode(), this.config.lastSourceLanguageName());
                menuEntry.setTargetLanguage(this.config.lastTargetLanguageCode(), this.config.lastTargetLanguageName());
                client.setMenuEntries(ArrayUtils.insert(1, client.getMenuEntries(), menuEntry));
            } else if (isHoveringChatLineWidget()) {
                message = getHoveredChatLineData();
                menuEntry.setChatLineData(message);
                menuEntry.setTargetLanguage(this.config.lastSourceLanguageCode(), this.config.lastSourceLanguageName());
                client.setMenuEntries(ArrayUtils.insert(1, client.getMenuEntries(), menuEntry));
            }
        } else {
            this.menuEntry = null;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuAction() == MenuAction.RUNELITE && event.getMenuOption().startsWith("Translate")) {
            try {
                log.debug("Translating " + this.menuEntry.getSourceLanguageCode() + " to " + this.menuEntry.getTargetLanguageCode());
                String translation = this.translator.translate(this.menuEntry);
                String languageCode = this.menuEntry.getTargetLanguageCode().toUpperCase();
                log.debug("Translation Complete."
                        + "\n\tBefore: '" + this.menuEntry.getChatLineData().getChatLine() + "'"
                        + "\n\tAfter[" + languageCode + "]: " + translation + "'");

                // Highlighting
                if (this.config.isTranslationHighlighted()) {
                    if (languageCode.equalsIgnoreCase(this.config.lastSourceLanguageCode())) {
                        languageCode = ColorUtil.wrapWithColorTag(languageCode, this.config.sourceLangColor());
                        translation = ColorUtil.wrapWithColorTag(translation, this.config.sourceLangColor());
                    } else if (languageCode.equalsIgnoreCase(this.config.lastTargetLanguageCode())) {
                        languageCode = ColorUtil.wrapWithColorTag(languageCode, this.config.targetLangColor());
                        translation = ColorUtil.wrapWithColorTag(translation, this.config.targetLangColor());
                    }
                }

                // Send translation message
                ChatMessageType messageFilter = getVisibleChatMessageType();
                switch (messageFilter) {
                    case PUBLICCHAT:
                        sendTranslationToPublicChat(languageCode, translation, menuEntry);
                        break;
                    case FRIENDSCHAT:
                        sendTranslationToFriendsChat(languageCode, translation, menuEntry);
                        break;
                    case TRADE:
                        sendTranslationToTradeChat(languageCode, translation, menuEntry);
                        break;
                    case PRIVATECHAT:
                        if (menuEntry.getChatLineData().isSaidByPlayer()) {
                            if (menuEntry.getChatLineData().getRSN().startsWith("To ")) {
                                menuEntry.getChatLineData().fixRsnForPMFilter(); // Change "From <rsn>" to "<rsn>" - This is specific to the Private Chat filter.
                                sendTranslationToPrivateChatOut(languageCode, translation, menuEntry);
                                break;
                            } else if (menuEntry.getChatLineData().getRSN().startsWith("From ")) {
                                menuEntry.getChatLineData().fixRsnForPMFilter(); // Change "From <rsn>" to "<rsn>" - This is specific to the Private Chat filter.
                                sendTranslationToPrivateChat(languageCode, translation, menuEntry);
                                break;
                            }
                        }
                        sendTranslationToPrivateChat(languageCode, translation, menuEntry);
                        break;
                    case GAMEMESSAGE:
                    default:
                        sendTranslationToGameChat(languageCode, translation, menuEntry);
                        break;
                }

                // Preview the translation in chat input
                if (menuEntry.getChatLineData().isSaidByLocalPlayer()) {
                    previewTranslation(translation);
                }
            } catch (Exception e) {
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Translation Error: " + e.getMessage(), "");
            }
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
        return isMouseOverWidget(chatBoxWidget);
    }

    /**
     * Helper method to determine if the user is hovering over their own chat input.
     *
     * @return true if the user is hovering over their own chat input, false otherwise
     */
    private boolean isHoveringChatInputWidget() {
        Widget chatInputWidget = this.client.getWidget(WidgetInfo.CHATBOX_INPUT);
        return isMouseOverWidget(chatInputWidget);
    }

    /**
     * Helper method to determine if the user is hovering over a chat line in the chat box.
     *
     * @return true if the user is hovering over a message in the chat box, false otherwise
     */
    private boolean isHoveringChatLineWidget() {
        Widget chatLinesWidget = this.client.getWidget(WidgetInfo.CHATBOX_MESSAGE_LINES);
        return isMouseOverWidget(chatLinesWidget);
    }

    /**
     * Helper method to return the chat line data from the message underneath the mouse
     *
     * @return chat line data from the message underneath the mouse, false otherwise
     */
    private ChatLineData getHoveredChatLineData() {
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
        String fc = null;
        matcher = fcPattern.matcher(chatLine);
        if (matcher.find()) {
            fc = matcher.group();
            chatLine = chatLine.replace(fc, ""); // Remove FC heading
            fc = fc.substring(1, fc.length() - 2); // Remove the '[' and '] ' which surround the FC name
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
    }

    /**
     * Helper method to return the chat line data in the local player's chat input
     *
     * @return chat line data from the local player
     */
    private ChatLineData getLocalPlayerChatLineData() {
        Widget chatBuffer = this.client.getWidget(WidgetInfo.CHATBOX_INPUT);
        String chatLine = Text.removeTags(chatBuffer.getText());
        String rsn = this.client.getLocalPlayer().getName();
        chatLine = chatLine.replace(rsn + ": ", ""); // Remove username
        chatLine = chatLine.substring(0, chatLine.length() - 1); // Remove the '*' at the end
        return new ChatLineData(rsn, chatLine, true);
    }

    /**
     * Helper method to return a {@link ChatMessageType} that the player will see, even in their given filter.
     * For example, if the player is currently filtering to 'Trade' chat, we return {@link ChatMessageType#TRADE}.
     * This helps send a translation where the player can see it.
     *
     * @return a {@link ChatMessageType} to reach the player in their given filter
     */
    private ChatMessageType getVisibleChatMessageType() {
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

        // Default to a game message
        return ChatMessageType.GAMEMESSAGE;
    }

    /**
     * Helper method to preview a translation in a user's chat input.
     *
     * @param translation - the translation to preview for the user in their chat input
     */
    private void previewTranslation(String translation) {
        if (Text.removeTags(translation).isEmpty()) {
            return;
        }
        Widget chatBuffer = this.client.getWidget(WidgetInfo.CHATBOX_INPUT);
        String rawChatBuffer = chatBuffer.getText();
        String chatLine = getLocalPlayerChatLineData().getChatLine();
        chatBuffer.setText(rawChatBuffer.replace(chatLine, translation));
    }

    /**
     * Helper method to send a translation message to public chat. This is done because a user is filtering that chat and we want them to see the translation under the same filter.
     */
    private void sendTranslationToPublicChat(String languageCode, String translation, ChatTranslatorMenuEntry menuEntry) {
        client.addChatMessage(ChatMessageType.PUBLICCHAT,
                "[" + languageCode + "] "
                        + (menuEntry.getChatLineData().isSaidByPlayer() ? menuEntry.getChatLineData().getRSN() : "GAME"),
                "</col>" + translation,
                "xx");
    }

    /**
     * Helper method to send a translation message to private chat. This is done because a user is filtering that chat and we want them to see the translation under the same filter.
     */
    private void sendTranslationToPrivateChat(String languageCode, String translation, ChatTranslatorMenuEntry menuEntry) {
        client.addChatMessage(ChatMessageType.PRIVATECHAT,
                (menuEntry.getChatLineData().isSaidByPlayer() ? menuEntry.getChatLineData().getRSN() : "GAME"),
                "</col>[" + languageCode + "] " + translation,
                "");
    }

    /**
     * Helper method to send a translation message to public chat as the user. This is done because a user is filtering that chat and we want them to see the translation under the same filter.
     */
    private void sendTranslationToPrivateChatOut(String languageCode, String translation, ChatTranslatorMenuEntry menuEntry) {
        client.addChatMessage(ChatMessageType.PRIVATECHATOUT,
                (menuEntry.getChatLineData().isSaidByPlayer() ? menuEntry.getChatLineData().getRSN() : "GAME"),
                "</col>[" + languageCode + "] " + translation, "");
    }

    /**
     * Helper method to send a translation message to trade chat. This is done because a user is filtering that chat and we want them to see the translation under the same filter.
     */
    private void sendTranslationToTradeChat(String languageCode, String translation, ChatTranslatorMenuEntry menuEntry) {
        client.addChatMessage(ChatMessageType.TRADE,
                "",
                "[" + languageCode + "]" + (menuEntry.getChatLineData().isSaidByLocalPlayer() ? " " + menuEntry.getChatLineData().getRSN() + ": " : ": ") + translation,
                "");
    }

    /**
     * Helper method to send a translation message to friends chat. This is done because a user is filtering that chat and we want them to see the translation under the same filter.
     */
    private void sendTranslationToFriendsChat(String languageCode, String translation, ChatTranslatorMenuEntry menuEntry) {
        client.addChatMessage(ChatMessageType.FRIENDSCHAT,
                (menuEntry.getChatLineData().isGameMessage() ? "GAME" : menuEntry.getChatLineData().getRSN()),
                "</col>" + translation,
                languageCode);
    }

    /**
     * Helper method to send a translation message to game chat. This is the default.
     */
    private void sendTranslationToGameChat(String languageCode, String translation, ChatTranslatorMenuEntry menuEntry) {
        client.addChatMessage(ChatMessageType.GAMEMESSAGE,
                "",
                "[" + languageCode + "] "
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
