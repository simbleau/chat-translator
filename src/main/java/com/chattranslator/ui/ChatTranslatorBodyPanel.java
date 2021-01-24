package com.chattranslator.ui;

import com.google.inject.Inject;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

/**
 * The body panel for the Chat Translator. This holds the entire body content of the panel.
 *
 * @version January 2021
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 */
public class ChatTranslatorBodyPanel extends PluginPanel {

    /**
     * The panel which allows the user to authenticate.
     */
    public final ChatTranslatorAuthPanel authPanel;

    /**
     * The panel which allows the user to configure language translation options.
     */
    public final ChatTranslatorLanguagePanel langPanel;

    /**
     * Construct the body panel.
     *
     * @param authPanel - the child authentication panel
     * @param langPanel - the child language panel
     */
    @Inject
    public ChatTranslatorBodyPanel(ChatTranslatorAuthPanel authPanel, ChatTranslatorLanguagePanel langPanel) {
        super();
        this.authPanel = authPanel;
        this.langPanel = langPanel;

        // General style
        setBorder(new EmptyBorder(0, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Wrapper pane
        final JPanel vBox = new JPanel();
        BoxLayout vBoxLayout = new BoxLayout(vBox, BoxLayout.Y_AXIS); //Top to bottom
        vBox.setLayout(vBoxLayout);
        final JScrollPane scrollPane = new JScrollPane(vBox);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Assembly
        vBox.add(this.authPanel);
        vBox.add(this.langPanel);

        // Fill body with contents
        add(scrollPane, BorderLayout.CENTER);
    }

}
