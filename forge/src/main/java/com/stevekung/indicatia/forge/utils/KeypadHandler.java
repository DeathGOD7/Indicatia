package com.stevekung.indicatia.forge.utils;

import com.stevekung.indicatia.core.Indicatia;
import com.stevekung.indicatia.forge.config.IndicatiaConfig;

public class KeypadHandler
{
    public static boolean isAltChatEnabled()
    {
        return IndicatiaConfig.CONFIG.enableAlternateChatKey.get() && Indicatia.KEY_ALT_OPEN_CHAT.consumeClick();
    }

    public static boolean isAltChatMatches(int keysym, int scancode)
    {
        return IndicatiaConfig.CONFIG.enableAlternateChatKey.get() && Indicatia.KEY_ALT_OPEN_CHAT.matches(keysym, scancode);
    }
}