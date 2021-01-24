package com.chattranslator;

import com.chattranslator.ui.ChatTranslatorPanel;
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
     * Credentials are loaded in by the {@link ChatTranslatorPanel} via the file picker menu.
     * Credentials are given by Google to authenticate an API account.
     * <br>
     * To generate a key for a service account, visit <a href="https://console.cloud.google.com/iam-admin/serviceaccounts">https://console.cloud.google.com/iam-admin/serviceaccounts</a>.
     * </br>
     * An example credential file can look like this "example.json" file:
     * <pre>
     * {
     *   "type": "service_account",
     *   "project_id": "project-id-000000",
     *   "private_key_id": "abcdef000000000000000000000",
     *   "private_key": "-----BEGIN PRIVATE KEY-----\n__LOTS OF STUFF HERE__\n-----END PRIVATE KEY-----\n",
     *   "client_email": "runelite@project-id-000000.iam.gserviceaccount.com",
     *   "client_id": "00000000000000000000",
     *   "auth_uri": "https://accounts.google.com/o/oauth2/auth",
     *   "token_uri": "https://oauth2.googleapis.com/token",
     *   "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
     *   "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/project-id-000000.iam.gserviceaccount.com"
     * }
     * </pre>
     *
     * @return credential file contents used to authenticate and authorize a google cloud platform account
     */
    @ConfigItem(
            keyName = "credentials",
            name = "Google Cloud Credentials",
            description = "The contents of a private key file from Google Cloud Platform used to authenticate.",
            secret = false, // this should be true if hidden is false
            hidden = true // change to false for easier debugging!
    )
    default String lastCredentials() {
        return null;
    }
    @ConfigItem(
            keyName = "credentials",
            name = "Google Cloud Credentials",
            description = "The contents of a private key file from Google Cloud Platform used to authenticate."
    )
    void lastCredentials(String str);


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
    @ConfigItem(
            keyName = "rightClickChat",
            name = "Right click chatline translation",
            description = "Enables a right click option to translate chatlines to your language"
    )
    default boolean rightClickChat() {
        return true;
    }

    // DISPLAY OPTIONS
    @ConfigSection(
            position = 1,
            name = "Display",
            description = "Determines how translations are shown"
    )
    String displaySection = "displaySection";

    @ConfigItem(
            keyName = "isTranslationHighlighted",
            name = "Highlight translated lines?",
            description = "Whether translated lines by other players should be highlighted",
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
        return Color.GREEN;
    }

    @ConfigItem(
            keyName = "targetLangColor",
            name = "Target Language Color",
            description = "The hightlight color of the target language",
            section = "displaySection"
    )
    default Color targetLangColor() {
        return Color.RED;
    }
}
