package com.chattranslator.ui;

import com.chattranslator.ChatTranslator;
import com.chattranslator.ChatTranslatorPlugin;
import com.chattranslator.ex.GoogleException;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.SwingUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.io.File;

/**
 * The authentication button for the Chat Translator. This allows the user to pick their Google Translate API config and reflects the status of their authentication.
 *
 * @version January 2021
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 */
public class ChatTranslatorAuthenticateButton extends JButton implements Runnable {

    private static final String AUTH_TEXT = "Authenticate";
    private static final String UNAUTH_TEXT = "Unauthenticate";
    private static final String AUTH_TOOLTIP = "Upload your service account credentials and test authentication.";
    private static final String UNAUTH_TOOLTIP = "Unauthenticate your account.";
    private static final Color BUTTON_COLOR = ColorScheme.DARKER_GRAY_COLOR;
    private static final Color BUTTON_HOVER_COLOR = ColorScheme.DARKER_GRAY_HOVER_COLOR;

    private static final String LOADING_TEXT = "Authenticating...";
    private static final Color LOADING_COLOR = ColorScheme.PROGRESS_INPROGRESS_COLOR;

    private static final String ERROR_TEXT = "Failed Authentication";
    private static final Color ERROR_COLOR = ColorScheme.PROGRESS_ERROR_COLOR;

    private static final String SUCCESS_TEXT = "Authenticated";
    private static final Color SUCCESS_COLOR = ColorScheme.PROGRESS_COMPLETE_COLOR;

    /**
     * The chat translator.
     */
    private final ChatTranslator translator;

    /**
     * The plugin itself.
     */
    private final ChatTranslatorPlugin plugin;

    /**
     * The authentication file chosen by the user.
     */
    private File authFile;

    /**
     * The text the button should return to on mouse exit.
     */
    private String buttonReturnText;

    /**
     * The color the button should return to on mouse exit.
     */
    private Color buttonReturnColor;

    /**
     * Construct the authenticate button.
     *
     * @param translator - the translator
     * @param plugin - the plugin
     */
    @Inject
    public ChatTranslatorAuthenticateButton(ChatTranslator translator, ChatTranslatorPlugin plugin) {
        super();
        this.translator = translator;
        this.plugin = plugin;

        SwingUtil.removeButtonDecorations(this);
        this.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        this.setForeground(Color.WHITE);
        this.setUI(new BasicButtonUI());

        // Set initial state
        if (this.translator.isAuthenticated()) {
            this.setBackground(SUCCESS_COLOR);
            this.setText(SUCCESS_TEXT);
            this.setToolTipText(UNAUTH_TOOLTIP);
        } else {
            this.setBackground(BUTTON_COLOR);
            this.setText(AUTH_TEXT);
            this.setToolTipText(AUTH_TOOLTIP);
        }

        // Determine what happens when the user clicks the button
        this.addActionListener(e -> {
            if (this.translator.isAuthenticated()) {
                // User clicked to unauthenticate.
                this.translator.unauthenticate();
//                this.plugin.getPanel().hideLanguagePanel();
                this.plugin.getPanel().disableLanguagePanel();
                buttonReturnColor = BUTTON_COLOR;
                this.setBackground(BUTTON_HOVER_COLOR); // User clicked it, so they're still hovering
                this.setText(buttonReturnText = AUTH_TEXT);
                this.setToolTipText(AUTH_TOOLTIP);
            } else {
                // User clicked to upload credentials
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                int result = fileChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    this.authFile = fileChooser.getSelectedFile();
                    new Thread(this).start();
                }
            }
        });

        // Determine what happens when the user hovers/exits the button
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            // Button Hover effects
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (isEnabled()) {
                    buttonReturnText = getText();
                    buttonReturnColor = getBackground();
                    setBackground(BUTTON_HOVER_COLOR);
                    if (translator.isAuthenticated()) {
                        setText(UNAUTH_TEXT);
                    } else {
                        setText(AUTH_TEXT);
                    }

                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (isEnabled()) {
                    setBackground(buttonReturnColor);
                    setText(buttonReturnText);
                }
            }
        });
        this.setPreferredSize(new Dimension(200, 30));
        this.setMinimumSize(new Dimension(200, 30));
        this.setMaximumSize(new Dimension(200, 30));
        this.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    // Authentication via the button will be threaded
    @Override
    public void run() {
        // Performs authentication attempt
        this.setEnabled(false);
        this.plugin.getPanel().disableLanguagePanel();
        // Show progress
        this.setBackground(LOADING_COLOR);
        this.setText(LOADING_TEXT);
        // Auth here
        boolean result;
        try {
            translator.authenticate(authFile);
            this.plugin.getPanel().showLanguagePanel();
            this.plugin.getPanel().enableLanguagePanel(translator.getSupportedLanguages());
            this.plugin.loadLastSettings();
            result = true;
        } catch (GoogleException e) {
            result = false;
        }
        if (result) { // Check the result
            this.setBackground(SUCCESS_COLOR);
            this.setText(SUCCESS_TEXT);
            this.setToolTipText(UNAUTH_TOOLTIP);
        } else {
            this.setBackground(ERROR_COLOR);
            this.setText(ERROR_TEXT);
            this.plugin.getPanel().disableLanguagePanel();
        }
        buttonReturnText = getText();
        buttonReturnColor = getBackground();
        this.setEnabled(true);
    }
}
