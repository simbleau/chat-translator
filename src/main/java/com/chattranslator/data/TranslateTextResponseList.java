package com.chattranslator.data;

import com.chattranslator.ChatTranslatorMenuEntry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A response list contains a list of separate supported language responses.
 *
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 * @version January 2021
 * @see <a href="https://cloud.google.com/translate/docs/reference/rest/v2/translate#translatetextresponselist">https://cloud.google.com/translate/docs/reference/rest/v2/translate#translatetextresponselist</a>
 */
@Slf4j
public class TranslateTextResponseList {

    /**
     * The list of language translation responses. This list contains a language translation response for each query (q) sent in the language translation request.
     */
    public final TranslateTextResponseTranslation[] translations;

    /**
     * Initialize a list of translations.
     *
     * @param translations the transaltions
     */
    private TranslateTextResponseList(final TranslateTextResponseTranslation[] translations) {
        this.translations = translations;
    }

    /**
     * @return true if this translation list is empty, false otherwise
     */
    public boolean isEmpty() {
        return this.translations.length == 0;
    }


    /**
     * Return the best translation possible.
     *
     * @param language the preferred language to match, or null
     * @return the most applicable translation, or null, if no translations exist
     */
    public TranslateTextResponseTranslation getBestTranslation(@Nullable String language) {
        // 99% of the time we only have 1 translation or we don't know how to choose the best translation
        if (translations.length == 1 || language == null) {
            log.debug("Translation found");
            return translations[0];
        }

        // Attempt to find the perfect translation match
        Optional<TranslateTextResponseTranslation> perfectMatch = Stream.of(translations)
                .filter(translation -> translation.detectedSourceLanguage != null)
                .filter(translation -> translation.detectedSourceLanguage.equalsIgnoreCase(language))
                .findFirst();

        if (perfectMatch.isPresent()) {
            // Return the perfect match
            log.debug("Translation found");
            return perfectMatch.get();
        } else {
            // Return any translation, so it might as well be the first
            log.debug("Translation not matched - using any");
            return translations[0];
        }
    }

    /**
     * Parse a JSON source to receive this object.
     *
     * @param json the JSON source of this object, e.g.
     *             <pre>{"translations": [array(TranslateTextResponseTranslation)]}</pre>
     * @return an instance of this class on successful parsing
     * @throws Exception on parse failure
     */
    public static TranslateTextResponseList fromJSONExplicit(final JsonElement json, String sourceLanguage) throws Exception {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonArray translations = jsonObject.getAsJsonArray("translations");
        log.info("translations: " + translations.size());

        if (translations.size() != 1) {
            return fromJSONImplicit(json);
        }

        TranslateTextResponseTranslation[] response = new TranslateTextResponseTranslation[1];
        response[0] = TranslateTextResponseTranslation.fromJSONExplicit(translations.get(0), sourceLanguage);

        return new TranslateTextResponseList(response);
    }

    /**
     * Parse a JSON source to receive this object.
     *
     * @param json the JSON source of this object, e.g.
     *             <pre>{"translations": [array(TranslateTextResponseTranslation)],}</pre>
     * @return an instance of this class on successful parsing
     * @throws Exception on parse failure
     */
    public static TranslateTextResponseList fromJSONImplicit(final JsonElement json) throws Exception {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonArray translations = jsonObject.getAsJsonArray("translations");

        TranslateTextResponseTranslation[] response = IntStream
                .range(0, translations.size())
                .mapToObj(translations::get)
                .map(TranslateTextResponseTranslation::fromJSONImplicit)
                .filter(obj -> obj != null)
                .toArray(TranslateTextResponseTranslation[]::new);

        return new TranslateTextResponseList(response);
    }

}
