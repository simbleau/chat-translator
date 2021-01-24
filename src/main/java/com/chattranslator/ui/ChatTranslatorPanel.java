package com.chattranslator.ui;

import com.google.cloud.translate.Language;
import com.google.inject.Inject;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.border.EmptyBorder;
import java.util.List;
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
    public void enableLanguagePanel(List<Language> languages) {
        this.bodyPanel.langPanel.enableOptions(languages);
    }

    /**
     * Disable the language options panel.
     */
    public void disableLanguagePanel() {
        this.bodyPanel.langPanel.disableOptions();
    }

    /**
     * Load the source language into the language panel. This should always be done after the panel is enabled.
     *
     * @param languageCode - the source language code, e.g. 'en' (English)
     */
    public void loadSourceLanguage(String languageCode) {
        this.bodyPanel.langPanel.setSourceLanguage(languageCode);
    }

    /**
     * Load the target language into the language panel. This should always be done after the panel is enabled.
     *
     * @param languageCode - the source language code, e.g. 'da' (Danish)
     */
    public void loadTargetLanguage(String languageCode) {
        this.bodyPanel.langPanel.setTargetLanguage(languageCode);
    }

}
