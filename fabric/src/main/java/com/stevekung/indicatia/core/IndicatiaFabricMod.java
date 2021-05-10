package com.stevekung.indicatia.core;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import com.stevekung.indicatia.command.*;
import com.stevekung.indicatia.command.config.ConfigHandlerIN;
import com.stevekung.indicatia.event.HUDRenderEventHandler;
import com.stevekung.indicatia.event.IndicatiaEventHandler;
import com.stevekung.indicatia.gui.exconfig.screens.ExtendedConfigScreen;
import com.stevekung.indicatia.handler.KeyBindingHandler;
import com.stevekung.indicatia.utils.hud.HUDHelper;
import com.stevekung.stevekungslib.utils.client.ClientRegistryUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.KeyMapping;

public class IndicatiaFabricMod implements ClientModInitializer
{
    public static final ConfigHandlerIN CONFIG = new ConfigHandlerIN();

    static
    {
        IndicatiaMod.keyBindAltChat = new KeyMapping("key.chatAlt", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_ENTER, "key.categories.multiplayer");
    }

    @Override
    public void onInitializeClient()
    {
        IndicatiaMod.init();
        ClientRegistryUtils.registerKeyBinding(IndicatiaMod.keyBindAltChat);

        new AFKCommand(ClientCommandManager.DISPATCHER);
        new AutoFishCommand(ClientCommandManager.DISPATCHER);
        new MojangStatusCheckCommand(ClientCommandManager.DISPATCHER);
        new PingAllCommand(ClientCommandManager.DISPATCHER);
        new ProfileCommand(ClientCommandManager.DISPATCHER);
        new SlimeSeedCommand(ClientCommandManager.DISPATCHER);

        ClientTickEvents.END_CLIENT_TICK.register(mc ->
        {
            while (KeyBindingHandler.KEY_QUICK_CONFIG.consumeClick())
            {
                mc.setScreen(new ExtendedConfigScreen());
            }
            if (KeyBindingHandler.KEY_QUICK_CONFIG.isDown())
            {
                mc.setScreen(new ExtendedConfigScreen());
            }
        });

        ClientTickEvents.START_CLIENT_TICK.register(IndicatiaEventHandler.INSTANCE::onClientTick);
        ClientLoginConnectionEvents.DISCONNECT.register((handler, mc) -> HUDHelper.stopCommandTicks());
        ScreenEvents.AFTER_INIT.register((mc, screen, scaledWidth, scaledHeight) -> IndicatiaEventHandler.INSTANCE.onInitGui(mc, screen));

        ServerTickEvents.END_WORLD_TICK.register(HUDRenderEventHandler.INSTANCE::onClientTick);
        ClientLoginConnectionEvents.DISCONNECT.register((handler, mc) -> HUDRenderEventHandler.INSTANCE.onLoggedOut());
    }
}