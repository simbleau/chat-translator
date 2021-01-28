package com.chattranslator.ui;

import com.chattranslator.ChatTranslatorPlugin;
import com.google.inject.Inject;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The authentication panel for the Chat Translator. This holds instructions and authentication.
 *
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 * @version January 2021
 */
public class ChatTranslatorAuthPanel extends PluginPanel {

    /**
     * The help icon which links to instructions.
     */
    private static final ImageIcon HELP_ICON;

    static {
        final BufferedImage helpIcon = ImageUtil.getResourceStreamFromClass(ChatTranslatorPlugin.class, "/help.png");
        HELP_ICON = new ImageIcon(ImageUtil.resizeImage(helpIcon, 16, 16));
    }

    /**
     * The authentication button.
     */
    public final ChatTranslatorAuthenticateButton authenticateButton;

    /**
     * Construct the authentication panel.
     *
     * @param authenticateButton - the child authentication button
     */
    @Inject
    public ChatTranslatorAuthPanel(ChatTranslatorAuthenticateButton authenticateButton) {
        super();
        this.authenticateButton = authenticateButton;

        // Border insets
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Create the instructions horizontal box
        final JPanel instructionHBox = new JPanel();
        BoxLayout hBoxLayout = new BoxLayout(instructionHBox, BoxLayout.X_AXIS); // Left to right
        instructionHBox.setLayout(hBoxLayout);

        // Create instructions label
        JLabel instructions = new JLabel();
        instructions.setForeground(Color.GRAY);
        instructions.setText("<html><body style ='text-align:left'><h1 style='text-align:center'>Instructions</h1>"
                + "<p>You will need access to the Google Translate API under Google Cloud Platform. "
                + "Once you have an API key, press the button below to authenticate and start the plugin."
                + "</p><br><p>For step-by-step instructions, click the help icon to the left."
                + "</p></body></html>");

        // Create instructions help icon button
        JButton helpIcon = new JButton();
        SwingUtil.removeButtonDecorations(helpIcon);
        helpIcon.setIcon(HELP_ICON);
        helpIcon.setToolTipText("Step-by-step instructions for this plugin");
        helpIcon.setBackground(ColorScheme.DARK_GRAY_COLOR);
        helpIcon.setUI(new BasicButtonUI());
        helpIcon.addActionListener((ev) -> LinkBrowser.browse("https://github.com/simbleau/chat-translator#setup"));
        helpIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                helpIcon.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                helpIcon.setBackground(ColorScheme.DARK_GRAY_COLOR);
            }
        });
        helpIcon.setPreferredSize(new Dimension(30, 30));
        helpIcon.setMinimumSize(new Dimension(30, 30));
        helpIcon.setMaximumSize(new Dimension(30, 30));
        helpIcon.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Assemble instructions
        instructionHBox.add(helpIcon);
        instructionHBox.add(Box.createRigidArea(new Dimension(5, 0)));
        instructionHBox.add(instructions);

        // Fill content pane
        this.add(instructionHBox);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(this.authenticateButton);
    }
}
