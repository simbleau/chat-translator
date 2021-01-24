package com.chattranslator.ui;

import com.chattranslator.ChatTranslatorPlugin;
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
 * The header panel for the Chat Translator. This holds the name of the plugin and a kind donation button to buy the author a coffee.
 *
 * @version January 2021
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 */
public class ChatTranslatorHeaderPanel  extends PluginPanel {

    /**
     * The BuyMeACoffee donation button for kind souls.
     */
    private static final ImageIcon DONATE_BUTTON;
    static
    {
        final BufferedImage donateIcon = ImageUtil.getResourceStreamFromClass(ChatTranslatorPlugin.class, "/donate.png");
        DONATE_BUTTON = new ImageIcon(ImageUtil.resizeImage(donateIcon, 20, 20));

    }

    /**
     * Construct the header panel.
     */
    public ChatTranslatorHeaderPanel() {
        super();
        // Border insets
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        this.setLayout(new BorderLayout());

        // Add "Chat Translator" to the top left
        JLabel title = new JLabel();
        title.setText("Chat Translator");
        title.setForeground(Color.WHITE);
        this.add(title, BorderLayout.WEST);

        // Add donate button to the top right
        JButton donateIcon = new JButton();
        SwingUtil.removeButtonDecorations(donateIcon);
        donateIcon.setIcon(DONATE_BUTTON);
        donateIcon.setToolTipText("Please consider tipping the developer if you find this plugin useful.");
        donateIcon.setBackground(ColorScheme.DARK_GRAY_COLOR);
        donateIcon.setUI(new BasicButtonUI());
        donateIcon.addActionListener((ev) -> LinkBrowser.browse("https://www.buymeacoffee.com/simbleau"));
        donateIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                donateIcon.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                donateIcon.setBackground(ColorScheme.DARK_GRAY_COLOR);
            }
        });
        this.add(donateIcon, BorderLayout.EAST);
    }
}
