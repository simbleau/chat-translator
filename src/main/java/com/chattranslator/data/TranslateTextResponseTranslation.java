package com.chattranslator.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.text.StringEscapeUtils;

import javax.annotation.Nullable;

/**
 * A list of translation results for the requested text.
 *
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 * @version January 2021
 * @see <a href="https://cloud.google.com/translate/docs/reference/rest/v2/translate#translatetextresponsetranslation">https://cloud.google.com/translate/docs/reference/rest/v2/translate#translatetextresponsetranslation</a>
 */
public class TranslateTextResponseTranslation {

    /**
     * The source language of the initial request, detected automatically, if no source language was passed within the initial request. If the source language was passed, auto-detection of the language will not occur and this field will be omitted.
     */
    public final String detectedSourceLanguage;

    /**
     * Text translated into the target language.
     */
    public final String translatedText;

    private TranslateTextResponseTranslation(String detectedSourceLanguage, String translatedText) {
        this.detectedSourceLanguage = detectedSourceLanguage;
        this.translatedText = translatedText;
    }

    /**
     * Parse a JSON source to receive this object with an implicitly detected source language.
     *
     * @param json the JSON source of this object, e.g.
     *             <pre>{"detectedSourceLanguage": string,"translatedText": string,}</pre>
     * @return an instance of this class on successful parsing
     * @throws Exception on parse failure
     */
    public @Nullable
    static TranslateTextResponseTranslation fromJSONImplicit(final JsonElement json) {
        try {
            JsonObject jsonObject = json.getAsJsonObject();
            String translatedText = StringEscapeUtils.unescapeHtml4(jsonObject.get("translatedText").getAsString());
            if (jsonObject.has("detectedSourceLanguage")) {
                String detectedSourceLanguage = jsonObject.get("detectedSourceLanguage").getAsString();
                return new TranslateTextResponseTranslation(detectedSourceLanguage, translatedText);
            } else {
                return new TranslateTextResponseTranslation("?", translatedText);
            }
        } catch (Exception e ) {
            return null;
        }
    }

    /**
     * Parse a JSON source to receive this object with an explicitly provided source language.
     *
     * @param json the JSON source of this object, e.g.
     *             <pre>{"detectedSourceLanguage": string,}</pre>
     * @return an instance of this class on successful parsing
     * @throws Exception on parse failure
     */
    public @Nullable
    static TranslateTextResponseTranslation fromJSONExplicit(JsonElement json, String sourceLanguage) {
        try {
            JsonObject jsonObject = json.getAsJsonObject();
            String translatedText = StringEscapeUtils.unescapeHtml4(jsonObject.get("translatedText").getAsString());
            return new TranslateTextResponseTranslation(sourceLanguage, translatedText);
        } catch (Exception e ) {
            return null;
        }
    }
}
