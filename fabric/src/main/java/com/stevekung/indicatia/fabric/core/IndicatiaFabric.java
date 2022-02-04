package com.stevekung.indicatia.fabric.core;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import com.stevekung.indicatia.core.Indicatia;
import com.stevekung.indicatia.fabric.config.IndicatiaConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;

public class IndicatiaFabric implements ClientModInitializer
{
    public static IndicatiaConfig CONFIG;

    static
    {
        Indicatia.KEY_ALT_OPEN_CHAT = new KeyMapping("key.alt_open_chat", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_ENTER, KeyMapping.CATEGORY_MULTIPLAYER);
    }

    @Override
    public void onInitializeClient()
    {
        AutoConfig.register(IndicatiaConfig.class, GsonConfigSerializer::new);
        IndicatiaFabric.CONFIG = AutoConfig.getConfigHolder(IndicatiaConfig.class).getConfig();
        KeyBindingHelper.registerKeyBinding(Indicatia.KEY_ALT_OPEN_CHAT);
        ScreenEvents.AFTER_INIT.register((minecraft, screen, scaledWidth, scaledHeight) ->
        {
            if (IndicatiaFabric.CONFIG.reloadResourcesButton && screen instanceof PackSelectionScreen)
            {
                Screens.getButtons(screen).add(Indicatia.getReloadResourcesButton(screen, minecraft));
            }
        });
    }
}