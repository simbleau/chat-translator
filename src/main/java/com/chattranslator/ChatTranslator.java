package com.chattranslator;

import com.chattranslator.data.GetSupportedLanguagesResponseLanguage;
import com.chattranslator.data.GetSupportedLanguagesResponseList;
import com.chattranslator.data.TranslateTextResponseList;
import com.chattranslator.data.TranslateTextResponseTranslation;
import com.chattranslator.ex.GoogleAPIException;
import com.chattranslator.ex.GoogleAuthenticationException;
import com.chattranslator.ex.GoogleException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.text.StringEscapeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class to use the Google Translate API.
 *
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 * @version January 2021
 */
@Singleton
@Slf4j
public class ChatTranslator {

    /**
     * The configuration of the plugin.
     */
    @Inject
    private ChatTranslatorConfig config;

    /**
     * An HTTP Client to access the Google Translate API.
     */
    private OkHttpClient client = new OkHttpClient();

    /**
     * The list of supported languages by Google Translate API.
     */
    private GetSupportedLanguagesResponseList supportedLanguages = null;

    /**
     * Whether the user is authenticated.
     */
    private boolean authenticated = false;

    /**
     * @return true if the the chat translator is authenticated to Google Cloud Platform, false otherwise
     */
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    /**
     * Un-authenticate your credentials. This clears saved config and any session data used for chat translation.
     */
    public void unauthenticate() {
        config.apiKey(null); // Clear config
        this.authenticated = false;
    }

    /**
     * Authenticate using an API Key.
     *
     * @param apiKey - the data used for authentication
     * @throws GoogleAuthenticationException on authentication failure
     */
    public void authenticate(String apiKey) throws GoogleAuthenticationException {
        try {
            log.debug("Google Cloud Platform: Sending request for supported languages");
            final Request req = new Request.Builder()
                    .method("GET", null)
                    .url("https://translation.googleapis.com/language/translate/v2/languages?target=en&key=" + apiKey)
                    .build();

            Response response = client.newCall(req).execute();
            String responseSource = new String(response.body().bytes(), StandardCharsets.UTF_8);
            log.debug("Response:\n" + responseSource);
            log.debug("Google Cloud Platform: Received response");
            if (response.code() != 200) {
                throw new GoogleAuthenticationException("Google returned code " + response.code());
            }

            JsonParser parser = new JsonParser();
            JsonObject dom = parser.parse(responseSource).getAsJsonObject();
            JsonElement data = dom.get("data");
            this.supportedLanguages = GetSupportedLanguagesResponseList.fromJSON(data);
            log.debug("Supported languages:\n" +
                    Stream.of(supportedLanguages.languages)
                            .map(lang -> "\t" + lang.language + " - " + lang.name)
                            .collect(Collectors.joining("\n")));

            config.apiKey(apiKey);
            this.authenticated = true;
            log.info("Chat Translator authentication successful.");
        } catch (Exception e) {
            throw new GoogleAuthenticationException("Invalid Google Cloud Platform service account credentials", e);
        }
    }

    /**
     * Authenticate using previously saved configuration data.
     *
     * @throws GoogleAuthenticationException on authentication failure
     */
    public void authenticateFromConfig() throws GoogleAuthenticationException {
        if (this.config.apiKey() == null) {
            return;
        }
        authenticate(config.apiKey());
    }

    /**
     * Translate text from a source language to a target language.
     *
     * @param text           - the text to translate
     * @param sourceLanguage - the source language's code, e.g. 'en' (English)
     * @param targetLanguage - the target language' code, e.g. 'da' (Danish)
     * @return the translated text
     * @throws GoogleException on call failure
     */
    public TranslateTextResponseList translate(@Nonnull String text, @Nullable String sourceLanguage, @Nonnull String targetLanguage) throws GoogleException {
        if (!authenticated) {
            throw new GoogleAuthenticationException("You are not authenticated for Chat Translation.");
        }
        try {
            // Build request body
            JsonObject requestJson = new JsonObject();
            if (sourceLanguage != null) {
                requestJson.addProperty("source", sourceLanguage);
            }
            requestJson.addProperty("target", targetLanguage);
            requestJson.addProperty("q", text);
            log.debug("Request body: " + requestJson.toString());
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"), requestJson.toString());

            // Build request
            log.debug("Google Cloud Platform: Sending request for translation");
            final Request req = new Request.Builder()
                    .method("POST", requestBody)
                    .header("Content-Type", "application/json")
                    .url("https://translation.googleapis.com/language/translate/v2?key=" + config.apiKey())
                    .build();

            Response response = client.newCall(req).execute();
            String responseSource = new String(response.body().bytes(), StandardCharsets.UTF_8);
            log.debug("Response:\n" + responseSource);
            log.debug("Google Cloud Platform: Received response");
            if (response.code() != 200) {
                throw new GoogleAuthenticationException("Google returned code " + response.code());
            }

            JsonParser parser = new JsonParser();
            JsonObject dom = parser.parse(responseSource).getAsJsonObject();
            JsonElement data = dom.get("data");

            TranslateTextResponseList translationList;
            if (sourceLanguage == null) {
                translationList = TranslateTextResponseList.fromJSONImplicit(data);
            } else {
                translationList = TranslateTextResponseList.fromJSONExplicit(data, sourceLanguage);
            }

            if (!translationList.isEmpty()) {
                log.debug("Translations returned:\n" +
                        Stream.of(translationList.translations)
                                .map(translation -> "\t" + translation.detectedSourceLanguage + " - " + translation.translatedText)
                                .collect(Collectors.joining("\n")));
            }
            return translationList;
        } catch (Exception e) {
            throw new GoogleAPIException("API call failed. Try again or re-authenticate.", e);
        }
    }

    /**
     * Returns a list of supported languages by the Google Translate API.
     *
     * @return a list of supported translation languages
     * @throws GoogleException on call failure
     */
    public GetSupportedLanguagesResponseList getSupportedLanguages() throws GoogleException {
        if (authenticated) {
            return this.supportedLanguages;
        } else { ;
            throw new GoogleAuthenticationException("You are not authenticated for Chat Translation.");
        }
    }
}
