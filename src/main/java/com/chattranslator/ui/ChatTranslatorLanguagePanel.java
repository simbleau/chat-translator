package com.chattranslator.ui;

import com.chattranslator.ChatTranslatorConfig;
import com.chattranslator.data.GetSupportedLanguagesResponseLanguage;
import com.chattranslator.data.GetSupportedLanguagesResponseList;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import java.util.List;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.stream.Stream;

/**
 * The language panel for the Chat Translator. This holds options for the user to change their source and target language currently.
 *
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 * @version January 2021
 */
@Slf4j
public class ChatTranslatorLanguagePanel extends PluginPanel {

    /**
     * The plugin configuration.
     */
    private final ChatTranslatorConfig config;

    /**
     * The source language combo box.
     */
    private final JComboBox<LanguageComboItem> sourceLanguageComboBox;

    /**
     * The target language combo box.
     */
    private final JComboBox<LanguageComboItem> targetLanguageComboBox;

    /**
     * Construct the language options panel.
     *
     * @param config - the plugin configuration
     */
    @Inject
    public ChatTranslatorLanguagePanel(ChatTranslatorConfig config) {
        super();
        this.config = config;

        // Border insets
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));

        // Create the source language horizontal box
        final JPanel sourceHBox = new JPanel();
        sourceHBox.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0)); // Left to right

        // Create instructions label
        JLabel sourceLangLabel = new JLabel();
        sourceLangLabel.setForeground(Color.WHITE);
        sourceLangLabel.setText("Source Language: ");

        // Create language options combo box
        sourceLanguageComboBox = new JComboBox<>();
        sourceLanguageComboBox.setPreferredSize(new Dimension(100, 20));
        sourceLanguageComboBox.setMinimumSize(new Dimension(100, 20));
        sourceLanguageComboBox.setMaximumSize(new Dimension(100, 20));

        // Assemble HBox for Source Language
        sourceHBox.add(sourceLangLabel);
        sourceHBox.add(this.sourceLanguageComboBox);

        // Create the target language horizontal box
        final JPanel targetHBox = new JPanel();
        targetHBox.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0)); // Left to right

        // Create instructions label
        JLabel targetLangLabel = new JLabel();
        targetLangLabel.setForeground(Color.WHITE);
        targetLangLabel.setText("Target Language: ");

        // Create language options combo box
        targetLanguageComboBox = new JComboBox<>();
        targetLanguageComboBox.setPreferredSize(new Dimension(100, 20));
        targetLanguageComboBox.setMinimumSize(new Dimension(100, 20));
        targetLanguageComboBox.setMaximumSize(new Dimension(100, 20));

        // Assemble HBox for Source Language
        targetHBox.add(targetLangLabel);
        targetHBox.add(this.targetLanguageComboBox);

        // Fill content pane
        this.add(sourceHBox);
        this.add(targetHBox);
    }

    /**
     * Disable the source and target combo boxes.
     */
    public void disableOptions() {
        this.sourceLanguageComboBox.removeAllItems();
        this.targetLanguageComboBox.removeAllItems();
        this.sourceLanguageComboBox.setEnabled(false);
        this.targetLanguageComboBox.setEnabled(false);
    }

    /**
     * Load a list of Google Translate API languages into and enable the source/target combo boxes.
     *
     * @param languages - a list of Google Translate API supported languages
     */
    public void enableOptions(GetSupportedLanguagesResponseList languages) {
        // Avoid events while loading
        Stream.of(sourceLanguageComboBox.getItemListeners()).forEach(sourceLanguageComboBox::removeItemListener);
        Stream.of(targetLanguageComboBox.getItemListeners()).forEach(targetLanguageComboBox::removeItemListener);

        this.sourceLanguageComboBox.removeAllItems();
        this.targetLanguageComboBox.removeAllItems();
        for (GetSupportedLanguagesResponseLanguage lang : languages.languages) {
            LanguageComboItem wrapper = new LanguageComboItem(lang);
            this.sourceLanguageComboBox.addItem(wrapper);
            this.targetLanguageComboBox.addItem(wrapper);
        }

        // Start listening for config changes the user makes
        sourceLanguageComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() instanceof LanguageComboItem) {
                    GetSupportedLanguagesResponseLanguage sourceLang = ((LanguageComboItem) e.getItem()).language;
                    log.info("Selected source language: " + sourceLang.name);
                    this.config.lastSourceLanguageCode(sourceLang.language);
                    this.config.lastSourceLanguageName(sourceLang.name);
                    log.info("Saved source language: " + sourceLang.language);
                }
            }
        });
        targetLanguageComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() instanceof LanguageComboItem) {
                    GetSupportedLanguagesResponseLanguage targetLang = ((LanguageComboItem) e.getItem()).language;
                    log.info("Selected target language: " + targetLang.name);
                    this.config.lastTargetLanguageCode(targetLang.language);
                    this.config.lastTargetLanguageName(targetLang.name);
                    log.info("Saved target language: " + targetLang.language);
                }
            }
        });

        this.sourceLanguageComboBox.setEnabled(true);
        this.targetLanguageComboBox.setEnabled(true);
    }

    /**
     * Load a language code into the source language combo box.
     *
     * @param languageCode - the language code, e.g. 'en' (English)
     */
    public void setSourceLanguage(String languageCode) {
        this.setLanguage(languageCode, this.sourceLanguageComboBox);
    }

    /**
     * Load a language code into the target language combo box.
     *
     * @param languageCode - the language code, e.g. 'da' (Danish)
     */
    public void setTargetLanguage(String languageCode) {
        this.setLanguage(languageCode, this.targetLanguageComboBox);
    }

    /**
     * Helper method to load a language code into a combo box.
     *
     * @param languageCode - the language code to load in, e.g. 'en' (English)
     * @param comboBox     - the language combo box
     */
    private void setLanguage(String languageCode, JComboBox<LanguageComboItem> comboBox) {
        if (languageCode == null) {
            log.warn("Could not load null language.");
            return;
        }

        boolean loaded = false;
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            LanguageComboItem item = comboBox.getItemAt(i);
            GetSupportedLanguagesResponseLanguage lang = item.language;
            if (lang.language.equalsIgnoreCase(languageCode)) {
                comboBox.setSelectedItem(item);
                loaded = true;
                break;
            }
        }
        if (!loaded) {
            log.warn("Could not load language: '" + languageCode + "', as it was not found in " + comboBox.getItemCount() + " entries.");
        }
    }

    /**
     * An combo box item which stores a Language from Google's API
     */
    static class LanguageComboItem {
        private final GetSupportedLanguagesResponseLanguage language;

        public LanguageComboItem(GetSupportedLanguagesResponseLanguage language) {
            this.language = language;
        }

        @Override
        public String toString() {
            return this.language.name;
        }
    }
}
