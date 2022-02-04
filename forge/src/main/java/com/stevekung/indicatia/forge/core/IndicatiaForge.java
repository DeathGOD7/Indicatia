package com.stevekung.indicatia.forge.core;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import com.stevekung.indicatia.core.Indicatia;
import com.stevekung.indicatia.forge.config.IndicatiaConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;

@Mod(Indicatia.MOD_ID)
public class IndicatiaForge
{
    static
    {
        Indicatia.KEY_ALT_OPEN_CHAT = new KeyMapping("key.alt_open_chat", new IKeyConflictContext()
        {
            @Override
            public boolean isActive()
            {
                return !KeyConflictContext.GUI.isActive();
            }

            @Override
            public boolean conflicts(IKeyConflictContext other)
            {
                return false;
            }
        }, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_ENTER, KeyMapping.CATEGORY_MULTIPLAYER);
    }

    public IndicatiaForge()
    {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (remote, isServer) -> true));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().register(IndicatiaConfig.class);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, IndicatiaConfig.CONFIG_SPEC);
    }

    private void clientSetup(FMLClientSetupEvent event)
    {
        ClientRegistry.registerKeyBinding(Indicatia.KEY_ALT_OPEN_CHAT);
    }

    @SubscribeEvent
    public void initScreen(ScreenEvent.InitScreenEvent event)
    {
        if (IndicatiaConfig.CONFIG.reloadResourcesButton.get() && event.getScreen() instanceof PackSelectionScreen)
        {
            event.addListener(Indicatia.getReloadResourcesButton(event.getScreen(), event.getScreen().getMinecraft()));
        }
    }
}