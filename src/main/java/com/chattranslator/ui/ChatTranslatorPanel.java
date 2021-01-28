package com.chattranslator.ui;

import com.chattranslator.data.GetSupportedLanguagesResponseList;
import com.google.inject.Inject;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * The root plugin panel for the Chat Translator.
 *
 * @version January 2021
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 */
public class ChatTranslatorPanel extends PluginPanel {

    /**
     * The header content panel. This holds the title and BuyMeACoffee button.
     */
    public final ChatTranslatorHeaderPanel headerPanel;

    /**
     * The body content panel.
     */
    public final ChatTranslatorBodyPanel bodyPanel;

    /**
     * Construct the plugin panel.
     *
     * @param headerPanel - the child header panel
     * @param bodyPanel - the child body panel
     */
    @Inject
    public ChatTranslatorPanel(ChatTranslatorHeaderPanel headerPanel ,ChatTranslatorBodyPanel bodyPanel) {
        super(false);
        this.headerPanel = headerPanel;
        this.bodyPanel = bodyPanel;

        //Insets
        setBorder(new EmptyBorder(0,0,0,0));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Add header
        add(this.headerPanel, BorderLayout.NORTH);

        // Add content
        add(this.bodyPanel, BorderLayout.CENTER);
    }

    /**
     * Show the language options panel.
     */
    public void showLanguagePanel() {
        this.bodyPanel.langPanel.setVisible(true);
    }

    /**
     * Hide the language options panel.
     */
    public void hideLanguagePanel() {
        this.bodyPanel.langPanel.setVisible(false);
    }

    /**
     * Enable the language options panel and load in the usable languages.
     *
     * @param languages - the supported languages for translation
     */
    public void enableLanguagePanel(GetSupportedLanguagesResponseList languages) {
        this.bodyPanel.langPanel.enableOptions(languages);
    }

    /**
     * Disable the language options panel.
     */
    public void disableLanguagePanel() {
        this.bodyPanel.langPanel.disableOptions();
    }

}
