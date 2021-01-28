package com.chattranslator;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.Color;

/**
 * Configuration for the {@link ChatTranslatorPlugin}.
 *
 * @version January 2021
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 */
@ConfigGroup("chattranslator")
public interface ChatTranslatorConfig extends Config {

    /*
        HIDDEN CONFIGURATION BEGINS HERE
     */
    /**
     * An API key for Google Cloud Platform which has access to the Google Translate API.
     *
     * @return a Google Cloud Platform API key
     */
    @ConfigItem(
            keyName = "apiKey",
            name = "API Key",
            description = "An API Key for Google Cloud Platform used to authenticate to the Google Translate API.",
            secret = true,
            hidden = true // change to false for easier debugging!
    )
    default String apiKey() {
        return null;
    }
    @ConfigItem(
            keyName = "apiKey",
            name = "API Key",
            description = "An API Key for Google Cloud Platform used to authenticate to the Google Translate API."
    )
    void apiKey(String str);
    @ConfigItem(
            keyName = "targetLangCode",
            name = "Target Language Code",
            description = "The language transcribed to as a corresponding 2 letter code, e.g. 'da' (Danish)",
            hidden = true // change to false for easier debugging!
    )
    default String lastTargetLanguageCode() {
        // Lala <3
        return "da";
    }
    @ConfigItem(
            keyName = "targetLangCode",
            name = "Target Language Code",
            description = "The language transcribed to as a corresponding 2 letter code, e.g. 'da' (Danish)"
    )
    void lastTargetLanguageCode(String languageCode);
    @ConfigItem(
            keyName = "targetLangName",
            name = "Target Language Name",
            description = "The language transcribed to",
            hidden = true // change to false for easier debugging!
    )
    default String lastTargetLanguageName() {
        // Lala <3
        return "Danish";
    }
    @ConfigItem(
            keyName = "targetLangName",
            name = "Target Language Name",
            description = "The language transcribed to"
    )
    void lastTargetLanguageName(String languageName);

    @ConfigItem(
            keyName = "sourceLangCode",
            name = "Source Language Code",
            description = "The language transcribed to as a corresponding 2 letter code, e.g. 'en' (English)",
            hidden = true // change to false for easier debugging!
    )
    default String lastSourceLanguageCode() {
        return "en";
    }
    @ConfigItem(
            keyName = "sourceLangCode",
            name = "Source Language Code",
            description = "The language transcribed to as a corresponding 2 letter code, e.g. 'en' (English)"
    )
    void lastSourceLanguageCode(String languageCode);
    @ConfigItem(
            keyName = "sourceLangName",
            name = "Source Language Name",
            description = "The language transcribed to",
            hidden = true // change to false for easier debugging!
    )
    default String lastSourceLanguageName() {
        return "English";
    }
    @ConfigItem(
            keyName = "sourceLangName",
            name = "Source Language Name",
            description = "The language transcribed to"
    )
    void lastSourceLanguageName(String languageName);


    /*
        VISIBLE CONFIGURATION STARTS HERE
     */

    // DISPLAY OPTIONS
    @ConfigSection(
            position = 1,
            name = "Translation",
            description = "How translation is performed"
    )
    String translateSection = "translateSection";
    @ConfigItem(
            keyName = "standardTranslate",
            name = "Standard translation",
            description = "Enables a right click option to translate chatlines from source to target language",
            section = "translateSection"
    )
    default boolean isStandardTranslationEnabled() {
        return true;
    }
    @ConfigItem(
            keyName = "reverseTranslate",
            name = "Reverse translation",
            description = "Enables a right click option to translate chatlines from target to source language",
            section = "translateSection",
            hidden = true // This feature is TODO
    )
    default boolean isReverseTranslationEnabled() {
        return false;
    }

    // DISPLAY OPTIONS
    @ConfigSection(
            position = 2,
            name = "Display",
            description = "Determines how translations are shown"
    )
    String displaySection = "displaySection";

    @ConfigItem(
            keyName = "previewChatInput",
            name = "Preview input translations",
            description = "In addition to translating to the chatbox, also show a preview of the translation in the chat input when translating unsent input",
            section = "displaySection"
    )
    default boolean isPreviewingChatInput() {
        return true;
    }
    @ConfigItem(
            keyName = "showDetectedLanguages",
            name = "Show source language",
            description = "Shows the source/detected language during translation",
            section = "displaySection"
    )
    default boolean isShowingDetectedLanguages() {
        return true;
    }
    @ConfigItem(
            keyName = "isTranslationHighlighted",
            name = "Highlight translated lines?",
            description = "Whether translated lines should be highlighted",
            section = "displaySection"
    )
    default boolean isTranslationHighlighted() {
        return true;
    }

    @ConfigItem(
            keyName = "sourceLangColor",
            name = "Source Language Color",
            description = "The hightlight color of the source language",
            section = "displaySection"
    )
    default Color sourceLangColor() {
        return new Color(0xff, 0xA4, 0x00);
    }

    @ConfigItem(
            keyName = "targetLangColor",
            name = "Target Language Color",
            description = "The hightlight color of the target language",
            section = "displaySection"
    )
    default Color targetLangColor() {
        return new Color(0xCE, 0x68, 0xFF);
    }
}
