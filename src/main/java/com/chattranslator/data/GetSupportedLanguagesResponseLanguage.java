package com.chattranslator.data;

import com.google.gson.*;

import javax.annotation.Nullable;

/**
 * A single supported language response corresponds to information related to one supported language.
 *
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 * @version January 2021
 * @see <a href="https://cloud.google.com/translate/docs/reference/rest/v2/languages#getsupportedlanguagesresponselanguage">https://cloud.google.com/translate/docs/reference/rest/v2/languages#getsupportedlanguagesresponselanguage</a>
 */
public class GetSupportedLanguagesResponseLanguage {

    /**
     * Supported language code, generally consisting of its ISO 639-1 identifier. (E.g. 'en', 'ja'). In certain cases, BCP-47 codes including language + region identifiers are returned (e.g. 'zh-TW' and 'zh-CH').
     */
    public final String language;

    /**
     * Human readable name of the language localized to the target language.
     */
    public final String name;

    /**
     * Initialize a single supported language.
     *
     * @param language the supported language code
     * @param name     the human readable name of the language
     */
    private GetSupportedLanguagesResponseLanguage(final String language, final String name) {
        this.language = language;
        this.name = name;
    }

    /**
     * Parse a JSON source to receive this object.
     *
     * @param json the JSON source of this object, e.g.
     *             <pre>{"language": string, "name": string,}</pre>
     * @return an instance of this class on successful parsing
     * @throws Exception on parse failure
     */
    public @Nullable
    static GetSupportedLanguagesResponseLanguage fromJSON(final JsonElement json) {
        try {
            JsonObject jsonObject = json.getAsJsonObject();
            String language = jsonObject.get("language").getAsString();
            String name = jsonObject.get("name").getAsString();

            return new GetSupportedLanguagesResponseLanguage(language, name);
        } catch (Exception e) {
            return null;
        }
    }
}
