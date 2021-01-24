package com.chattranslator;

import com.chattranslator.ex.GoogleAPIException;
import com.chattranslator.ex.GoogleAuthenticationException;
import com.chattranslator.ex.GoogleException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class to use the Google Translate API.
 *
 * @version January 2021
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
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
     * A buffer which holds the service used for translation, which is loaded after successful authentication.
     */
    private Translate translate = null;

    /**
     * The list of supported languages by Google Translate API.
     */
    private List<Language> supportedLanguages;

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
        config.lastCredentials(null); // Clear config
        this.authenticated = false;
        this.translate = null;
    }

    /**
     * Authenticate using credential data.
     *
     * @param credentialData - the data used for authentication
     * @throws GoogleAuthenticationException on invalid credentials
     * @throws GoogleAPIException            on failure to receive supported languages
     */
    public void authenticate(String credentialData) throws GoogleAuthenticationException, GoogleAPIException {
        try {
            InputStream credentialsData = new ByteArrayInputStream(credentialData.getBytes());
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsData);
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(credentials).build();
            this.translate = translateOptions.getService();

            log.info("Chat Translator authentication successful."
                    + "\n\tHost: " + translateOptions.getHost()
                    + "\n\tVersion: " + translateOptions.getLibraryVersion()
                    + "\n\tProject: " + translateOptions.getProjectId()
                    + "\n\tApp Name: " + translateOptions.getApplicationName());
        } catch (Exception e) {
            throw new GoogleAuthenticationException("Invalid Google Cloud Platform service account credentials", e);
        }

        try {
            this.supportedLanguages = translate.listSupportedLanguages();

            log.info("Supported languages:\n"
                    + supportedLanguages.stream()
                    .map(lang -> "\t" + lang.toString())
                    .collect(Collectors.joining("\n")));
        } catch (Exception e) {
            throw new GoogleAuthenticationException("Failed to receive supported languages after successful authentication", e);
        }

        config.lastCredentials(credentialData); // Save config
        this.authenticated = true;
    }

    /**
     * Authenticate using a file
     *
     * @param authFile - the file used for authentication
     * @throws GoogleAuthenticationException on invalid credentials
     * @throws GoogleAPIException            on failure to receive supported languages
     */
    public void authenticate(@Nonnull File authFile) throws GoogleAuthenticationException, GoogleAPIException {
        String credentialsData;
        try {
            InputStream credentialsStream = new FileInputStream(authFile);
            credentialsData = new BufferedReader(new InputStreamReader(credentialsStream)).lines().collect(Collectors.joining());
        } catch (Exception e) {
            throw new GoogleAuthenticationException("Could not use the credential file: " + authFile.getAbsolutePath(), e);
        }
        authenticate(credentialsData);
    }

    /**
     * Authenticate using previously saved configuration data.
     */
    public void authenticateFromConfig() throws GoogleAuthenticationException, GoogleAPIException {
        if (this.config.lastCredentials() == null) {
            return;
        }
        authenticate(config.lastCredentials());
    }

    /**
     * Helper method used to translate text.
     *
     * @param text    - the text to translate
     * @param options - the options for translation
     * @return the translated text
     * @throws GoogleException on call failure
     */
    private String translate(String text, Translate.TranslateOption... options) throws GoogleException {
        if (!authenticated) {
            throw new GoogleAuthenticationException("You are not authenticated for Chat Translation.");
        }
        try {
            Translation translation = this.translate.translate(text, options);
            return StringEscapeUtils.unescapeHtml4(translation.getTranslatedText());
        } catch (Exception e) {
            throw new GoogleAPIException("API call failed. Try again or re-authenticate.");
        }
    }

    /**
     * Translate text from a source language to a target language.
     *
     * @param text               - the text to translate
     * @param sourceLanguageCode - the source language's code, e.g. 'en' (English)
     * @param targetLanguageCode - the target language' code, e.g. 'da' (Danish)
     * @return the translated text
     * @throws GoogleException on call failure
     */
    private String translate(String text, String sourceLanguageCode, String targetLanguageCode) throws GoogleException {
        Translate.TranslateOption[] options = {
                Translate.TranslateOption.sourceLanguage(sourceLanguageCode),
                Translate.TranslateOption.targetLanguage(targetLanguageCode)
        };
        return translate(text, options);
    }

    /**
     * Translate text from an automatically detected language to a target language.
     *
     * @param text               - the text to translate
     * @param targetLanguageCode - the target language' code, e.g. 'da' (Danish)
     * @return the translated text
     * @throws GoogleException on call failure
     */
    private String translate(String text, String targetLanguageCode) throws GoogleException {
        Translate.TranslateOption[] options = {
                Translate.TranslateOption.targetLanguage(targetLanguageCode)
        };
        return translate(text, options);
    }

    /**
     * Translate text from a {@link ChatTranslatorMenuEntry}.
     *
     * @param menuEntry - the menu entry to translate
     * @return the translated text
     * @throws GoogleException on call failure
     */
    public String translate(ChatTranslatorMenuEntry menuEntry) throws GoogleException {
        if (menuEntry.getSourceLanguageCode() == null) {
            return translate(menuEntry.getChatLineData().getChatLine(), menuEntry.getTargetLanguageCode());
        } else {
            return translate(menuEntry.getChatLineData().getChatLine(), menuEntry.getSourceLanguageCode(), menuEntry.getTargetLanguageCode());
        }
    }

    /**
     * Returns a list of supported languages by the Google Translate API.
     *
     * @return a list of supported translation languages
     * @throws GoogleException on call failure
     */
    public List<Language> getSupportedLanguages() throws GoogleException {
        if (authenticated) {
            return this.supportedLanguages;
        } else {
            throw new GoogleAuthenticationException("You are not authenticated for Chat Translation.");
        }
    }
}
