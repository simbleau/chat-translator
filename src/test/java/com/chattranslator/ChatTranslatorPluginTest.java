package com.chattranslator;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ChatTranslatorPluginTest {

    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(ChatTranslatorPlugin.class);
        RuneLite.main(args);
    }
}