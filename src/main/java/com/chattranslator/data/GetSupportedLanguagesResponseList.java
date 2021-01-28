package com.chattranslator.data;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A response list contains a list of separate supported language responses.
 *
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 * @version January 2021
 * @see <a href="https://cloud.google.com/translate/docs/reference/rest/v2/languages#getsupportedlanguagesresponselist">https://cloud.google.com/translate/docs/reference/rest/v2/languages#getsupportedlanguagesresponselist</a>
 */
public class GetSupportedLanguagesResponseList {

    /**
     * The set of supported languages.
     */
    public final GetSupportedLanguagesResponseLanguage[] languages;

    /**
     * Initialize a list of supported languages.
     *
     * @param languages the supported languages
     */
    private GetSupportedLanguagesResponseList(final GetSupportedLanguagesResponseLanguage[] languages) {
        this.languages = languages;
    }

    /**
     * Parse a JSON source to receive this object.
     *
     * @param json the JSON source of this object, e.g.
     *             <pre>{"languages": [{object(GetSupportedLanguagesResponseLanguage)}],}</pre>
     * @return an instance of this class on successful parsing
     * @throws Exception on parse failure
     */
    public static GetSupportedLanguagesResponseList fromJSON(final JsonElement json) throws Exception {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonArray languages = jsonObject.getAsJsonArray("languages");

        GetSupportedLanguagesResponseLanguage[] response = IntStream
                .range(0, languages.size())
                .mapToObj(languages::get)
                .map(GetSupportedLanguagesResponseLanguage::fromJSON)
                .filter(obj -> obj != null)
                .toArray(GetSupportedLanguagesResponseLanguage[]::new);

        return new GetSupportedLanguagesResponseList(response);
    }
}
