package com.stevekung.indicatia.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.lwjgl.glfw.GLFW;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.stevekung.indicatia.command.*;
import com.stevekung.indicatia.config.IndicatiaConfig;
import com.stevekung.indicatia.config.IndicatiaSettings;
import com.stevekung.indicatia.event.HUDRenderEventHandler;
import com.stevekung.indicatia.event.HypixelEventHandler;
import com.stevekung.indicatia.event.IndicatiaEventHandler;
import com.stevekung.indicatia.gui.screen.IndicatiaChatScreen;
import com.stevekung.indicatia.handler.KeyBindingHandler;
import com.stevekung.indicatia.key.KeypadChatKey;
import com.stevekung.indicatia.utils.ThreadMinigameData;
import com.stevekung.stevekungslib.utils.CommonUtils;
import com.stevekung.stevekungslib.utils.LoggerBase;
import com.stevekung.stevekungslib.utils.ModVersionChecker;
import com.stevekung.stevekungslib.utils.client.command.ClientCommands;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@Mod(IndicatiaMod.MOD_ID)
public class IndicatiaMod
{
    public static final String MOD_ID = "indicatia";
    private static final File PROFILE = new File(IndicatiaSettings.USER_DIR, "profile.txt");
    public static final ModVersionChecker CHECKER = new ModVersionChecker(MOD_ID);
    public static boolean isGalacticraftLoaded;
    public static final LoggerBase LOGGER = new LoggerBase("Indicatia");
    private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
    public static final KeyBinding keyBindAltChat = new KeyBinding("key.chatAlt", new KeypadChatKey(), InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_KP_ENTER, "key.categories.multiplayer");

    static
    {
        IndicatiaMod.initProfileFile();
    }

    public IndicatiaMod()
    {
        CommonUtils.addModListener(this::phaseOne);
        CommonUtils.addModListener(this::loadComplete);

        CommonUtils.registerConfig(ModConfig.Type.CLIENT, IndicatiaConfig.GENERAL_BUILDER);
        CommonUtils.registerModEventBus(IndicatiaConfig.class);

        IndicatiaMod.isGalacticraftLoaded = ModList.get().isLoaded("galacticraftcore");
    }

    private void phaseOne(FMLClientSetupEvent event)
    {
        this.registerClientCommands();
        KeyBindingHandler.init();
        CommonUtils.registerEventHandler(new HUDRenderEventHandler());
        CommonUtils.registerEventHandler(new IndicatiaEventHandler());
        CommonUtils.registerEventHandler(new HypixelEventHandler());
        CommonUtils.registerEventHandler(new IndicatiaChatScreen());

        IndicatiaMod.loadProfileOption();
    }

    private void loadComplete(FMLLoadCompleteEvent event)
    {
        CommonUtils.runAsync(ThreadMinigameData::new);

        if (IndicatiaConfig.GENERAL.enableVersionChecker.get())
        {
            IndicatiaMod.CHECKER.startCheck();
        }
    }

    private void registerClientCommands()
    {
        ClientCommands.register(new AFKCommand());
        ClientCommands.register(new AutoFishCommand());
        ClientCommands.register(new MojangStatusCheckCommand());
        ClientCommands.register(new PingAllCommand());
        ClientCommands.register(new ProfileCommand());
        ClientCommands.register(new SlimeSeedCommand());
        IndicatiaMod.LOGGER.info("Registering client side commands");
    }

    private static void loadProfileOption()
    {
        if (!PROFILE.exists())
        {
            return;
        }
        if (!IndicatiaSettings.DEFAULT_CONFIG_FILE.exists())
        {
            IndicatiaMod.LOGGER.info("Creating default profile...");
            IndicatiaSettings.setCurrentProfile("default");
            IndicatiaSettings.INSTANCE.save();
        }

        CompoundNBT nbt = new CompoundNBT();

        try (BufferedReader reader = Files.newReader(PROFILE, Charsets.UTF_8))
        {
            reader.lines().forEach(option ->
            {
                try
                {
                    Iterator<String> iterator = IndicatiaMod.COLON_SPLITTER.split(option).iterator();
                    nbt.putString(iterator.next(), iterator.next());
                }
                catch (Exception e) {}
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        for (String property : nbt.keySet())
        {
            String key = nbt.getString(property);

            if ("profile".equals(property))
            {
                IndicatiaMod.LOGGER.info("Load current profile '{}'", key);
                IndicatiaSettings.setCurrentProfile(key);
                IndicatiaSettings.INSTANCE.load();
            }
        }
    }

    private static void initProfileFile()
    {
        if (!IndicatiaSettings.INDICATIA_DIR.exists())
        {
            IndicatiaSettings.INDICATIA_DIR.mkdirs();
        }
        if (!IndicatiaSettings.USER_DIR.exists())
        {
            IndicatiaSettings.USER_DIR.mkdirs();
        }

        if (!IndicatiaMod.PROFILE.exists())
        {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(IndicatiaMod.PROFILE), StandardCharsets.UTF_8)))
            {
                writer.println("profile:default");
                IndicatiaMod.LOGGER.info("Creating profile option at {}", IndicatiaMod.PROFILE.getPath());
            }
            catch (IOException e)
            {
                IndicatiaMod.LOGGER.error("Failed to save profile");
                e.printStackTrace();
            }
        }
    }
}