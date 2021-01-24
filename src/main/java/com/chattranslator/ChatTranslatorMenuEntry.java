package com.chattranslator;

import net.runelite.api.MenuEntry;
import net.runelite.client.util.ColorUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A wrapped {@link MenuEntry} used to store metadata passed for translation.
 *
 * @version January 2021
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 */
public class ChatTranslatorMenuEntry extends MenuEntry {

    /**
     * What the menu entry says when a valid source language and target language are specified.
     */
    private static final String EXPLICIT_TRANSLATION_FORMAT = "Translate from %s to %s";

    /**
     * What the menu entry says when a valid target language is specified.
     */
    private static final String IMPLICIT_TRANSLATION_FORMAT = "Translate to %s";

    /**
     * The plugin configuration
     */
    private ChatTranslatorConfig config;

    /**
     * The code of the source language used, e.g. en
     */
    private @Nullable String sourceLanguageCode;
    /**
     * The name of the source language used, e.g. English
     */
    private @Nullable String sourceLanguageName;

    /**
     * The code of the target language used, e.g. da
     */
    private String targetLanguageCode;
    /**
     * The name of the target language used, e.g. Danish
     */
    private String targetLanguageName;

    /**
     * The data being used for translation in this menu option.
     */
    private ChatLineData chatLineData;

    /**
     * Construct a translation menu entry.
     *
     * @param config - the plugin config
     */
    public ChatTranslatorMenuEntry(ChatTranslatorConfig config) {
        this.config = config;
        this.sourceLanguageCode = null;
        this.sourceLanguageName = null;
        this.targetLanguageCode = null;
        this.targetLanguageName = null;
        this.chatLineData = null;
    }

    /**
     * Set the data being used in translation for this menu entry.
     *
     * @param chatLineData - the data being used for translation
     */
    public void setChatLineData(ChatLineData chatLineData) {
        this.chatLineData = chatLineData;
    }

    /**
     * @return the data being used for translation
     */
    public ChatLineData getChatLineData() {
        return this.chatLineData;
    }

    /**
     * Set the source language. Null indicates auto-detect language.
     *
     * @param languageCode - the language's code to translate from, e.g. 'en'
     * @param languageName - the language's name to translate from, e.g. 'English'
     */
    public void setSourceLanguage(@Nullable String languageCode, @Nullable String languageName) {
        this.sourceLanguageCode = languageCode;
        this.sourceLanguageName = languageName;
    }

    /**
     * Set the target language for translation.
     *
     * @param languageCode - the language's code to translate from, e.g. 'da'
     * @param languageName - the language's name to translate from, e.g. 'Danish'
     */
    public void setTargetLanguage(@Nonnull String languageCode, @Nonnull String languageName) {
        this.targetLanguageCode = languageCode;
        this.targetLanguageName = languageName;
    }

    /**
     * @return the source language code, e.g. 'en' (English)
     */
    public @Nullable
    String getSourceLanguageCode() {
        return sourceLanguageCode;
    }

    /**
     * @return the target language code, e.g. 'da' (Danish)
     */
    public @Nonnull
    String getTargetLanguageCode() {
        return targetLanguageCode;
    }

    /**
     * Return the menu entry option text. Should say something such as "Translate English to Danish" or "Translate to Danish".
     *
     * @return the menu entry option text
     * @see #EXPLICIT_TRANSLATION_FORMAT
     * @see #IMPLICIT_TRANSLATION_FORMAT
     */
    @Override
    public String getOption() {
        String menuOption;
        if (this.sourceLanguageCode == null) {
            String langName = this.targetLanguageName;
            if (config.isTranslationHighlighted()) {
                if (this.targetLanguageCode.equalsIgnoreCase(config.lastTargetLanguageCode())) {
                    langName = ColorUtil.wrapWithColorTag(langName, config.targetLangColor());
                } else if (this.targetLanguageCode.equalsIgnoreCase(config.lastSourceLanguageCode())) {
                    langName = ColorUtil.wrapWithColorTag(langName, config.sourceLangColor());
                }
            }
            menuOption = String.format(IMPLICIT_TRANSLATION_FORMAT, langName);
        } else {
            String sLangName = this.sourceLanguageName;
            String tLangName = this.targetLanguageName;
            if (config.isTranslationHighlighted()) {
                if (this.sourceLanguageCode.equalsIgnoreCase(config.lastTargetLanguageCode())) {
                    sLangName = ColorUtil.wrapWithColorTag(sLangName, config.targetLangColor());
                } else if (this.sourceLanguageCode.equalsIgnoreCase(config.lastSourceLanguageCode())) {
                    sLangName = ColorUtil.wrapWithColorTag(sLangName, config.sourceLangColor());
                }
                if (this.targetLanguageCode.equalsIgnoreCase(config.lastTargetLanguageCode())) {
                    tLangName = ColorUtil.wrapWithColorTag(tLangName, config.targetLangColor());
                } else if (this.targetLanguageCode.equalsIgnoreCase(config.lastSourceLanguageCode())) {
                    tLangName = ColorUtil.wrapWithColorTag(tLangName, config.sourceLangColor());
                }
            }
            menuOption = String.format(EXPLICIT_TRANSLATION_FORMAT, sLangName, tLangName);
        }
        return menuOption;
    }

}
