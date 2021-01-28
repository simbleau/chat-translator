package com.chattranslator.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The requesting dialog for an API key used by the {@link ChatTranslatorAuthenticateButton}. This allows the user to submit
 * their Google Translate API key for authentication.
 *
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 * @version January 2021
 */
public class ChatTranslatorAPIKeyDialog extends JFrame {

    /**
     * The dialog frame icons. Swing chooses the proper frame icon depending on the OS.
     */
    private static final List<Image> FRAME_ICONS = new LinkedList<>();

    /**
     * The text field which the user interacts with.
     */
    private JTextField keyTextField;

    /**
     * Construct the API Key Dialog.
     *
     * @param authButton - the auth button which spawns this dialog used for call back
     */
    public ChatTranslatorAPIKeyDialog(ChatTranslatorAuthenticateButton authButton) {
        super();

        // Set frame properties
        this.setTitle("Enter an API Key");
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.setLocationRelativeTo(null); // Centers on screen
        this.setResizable(false);

        // Set frame icons
        final BufferedImage icon128 = ImageUtil.getResourceStreamFromClass(ChatTranslatorAPIKeyDialog.class, "/frame_icons/auth_128.png");
        final BufferedImage icon64 = ImageUtil.getResourceStreamFromClass(ChatTranslatorAPIKeyDialog.class, "/frame_icons/auth_64.png");
        final BufferedImage icon32 = ImageUtil.getResourceStreamFromClass(ChatTranslatorAPIKeyDialog.class, "/frame_icons/auth_32.png");
        final BufferedImage icon16 = ImageUtil.getResourceStreamFromClass(ChatTranslatorAPIKeyDialog.class, "/frame_icons/auth_16.png");
        Collections.addAll(FRAME_ICONS, icon128, icon64, icon32, icon16);
        this.setIconImages(FRAME_ICONS);

        // Create content pane
        JPanel content = new JPanel();
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        content.setBackground(ColorScheme.DARK_GRAY_COLOR);
        content.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0)); // Left to right

        // Add "Chat Translator" to the top left
        JLabel title = new JLabel();
        title.setText("API Key:");
        title.setForeground(Color.WHITE);
        content.add(title, BorderLayout.WEST);

        // Add Text field to the top right
        JPasswordField keyTextField = new JPasswordField();
        keyTextField.setHorizontalAlignment(JTextField.CENTER);
        keyTextField.setSelectedTextColor(Color.RED);
        keyTextField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        keyTextField.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
        keyTextField.setMinimumSize(new Dimension(300, 30));
        keyTextField.setPreferredSize(new Dimension(300, 30));
        keyTextField.setMaximumSize(new Dimension(300, 30));
        this.keyTextField = keyTextField;
        content.add(this.keyTextField, BorderLayout.EAST);

        // Add donate button to the top right
        JButton submitButton = new JButton();
        submitButton.setMinimumSize(new Dimension(300, 30));
        submitButton.setPreferredSize(new Dimension(300, 30));
        submitButton.setMaximumSize(new Dimension(300, 30));
        SwingUtil.removeButtonDecorations(submitButton);
        submitButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        submitButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        submitButton.setForeground(Color.WHITE);
        submitButton.setText("Submit");
        submitButton.setUI(new BasicButtonUI());
        submitButton.addActionListener(e -> {
            // Call authentication
            new Thread(authButton).start();
            this.setVisible(false);
        });
        submitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                submitButton.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                submitButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            }
        });

        this.add(content, BorderLayout.CENTER);
        this.add(submitButton, BorderLayout.SOUTH);
        this.pack();
    }

    /**
     * Focus the text field for the API key.
     */
    public void focusTextField() {
        super.requestFocus();
        this.keyTextField.requestFocus();
    }

    /**
     * Clear the API key text field.
     */
    public void clearApiKey() {
        this.keyTextField.setText("");
    }

    /**
     * Retrieve the API key in the text field provided by the user.
     */
    public String getApiKey() {
        return this.keyTextField.getText().trim();
    }

}
