package com.stevekung.indicatia.event;

import java.net.InetAddress;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nonnull;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.stevekung.indicatia.config.IndicatiaConfig;
import com.stevekung.indicatia.core.IndicatiaMod;
import com.stevekung.indicatia.gui.exconfig.screen.ExtendedConfigScreen;
import com.stevekung.indicatia.gui.exconfig.screen.OffsetRenderPreviewScreen;
import com.stevekung.indicatia.gui.screen.MojangStatusScreen;
import com.stevekung.indicatia.gui.widget.MojangStatusButton;
import com.stevekung.indicatia.handler.KeyBindingHandler;
import com.stevekung.indicatia.utils.AFKMode;
import com.stevekung.stevekungslib.utils.LangUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.status.IClientStatusNetHandler;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.status.client.CPingPacket;
import net.minecraft.network.status.client.CServerQueryPacket;
import net.minecraft.network.status.server.SPongPacket;
import net.minecraft.network.status.server.SServerInfoPacket;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IndicatiaEventHandler
{
    private final Minecraft mc;
    public static int currentServerPing;
    private static final ThreadPoolExecutor REALTIME_PINGER = new ScheduledThreadPoolExecutor(5, new ThreadFactoryBuilder().setNameFormat("Real Time Server Pinger #%d").setDaemon(true).build());
    private long lastPinger = -1L;

    public static boolean START_AFK;
    public static AFKMode AFK_MODE = AFKMode.IDLE;
    public static String AFK_REASON;
    public static int afkMoveTicks;
    public static int afkTicks;

    public static boolean START_AUTO_FISH;
    private static int autoFishTick;

    public IndicatiaEventHandler()
    {
        this.mc = Minecraft.getInstance();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (this.mc.player != null)
        {
            if (IndicatiaConfig.GENERAL.enableVersionChecker.get())
            {
                if (!IndicatiaMod.CHECKER.hasChecked())
                {
                    IndicatiaMod.CHECKER.checkFail();
                    IndicatiaMod.CHECKER.printInfo();
                    IndicatiaMod.CHECKER.setChecked(true);
                }
            }

            if (event.phase == TickEvent.Phase.START)
            {
                IndicatiaEventHandler.afkTick(this.mc.player);
                IndicatiaEventHandler.autoFishTick(this.mc);

                if (this.mc.getCurrentServerData() != null)
                {
                    long now = Util.milliTime();

                    if (this.lastPinger == -1L || now - this.lastPinger > 5000L)
                    {
                        this.lastPinger = now;
                        IndicatiaEventHandler.getRealTimeServerPing(this.mc.getCurrentServerData());
                    }
                }

                for (UseAction action : UseAction.values())
                {
                    if (action != UseAction.NONE)
                    {
                        if (IndicatiaConfig.GENERAL.enableBlockhitAnimation.get() && this.mc.gameSettings.keyBindAttack.isKeyDown() && this.mc.objectMouseOver != null && this.mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK && !this.mc.player.getHeldItemMainhand().isEmpty() && this.mc.player.getHeldItemMainhand().getUseAction() == action)
                        {
                            this.mc.player.swingArm(Hand.MAIN_HAND);
                        }
                    }
                }
            }
        }
        ForgeIngameGui.renderObjective = IndicatiaConfig.GENERAL.enableSidebarScoreboardRender.get();
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event)
    {
        MovementInput movement = event.getMovementInput();

        // afk stuff
        if (IndicatiaEventHandler.AFK_MODE == AFKMode.RANDOM_MOVE_360)
        {
            int afkMoveTick = IndicatiaEventHandler.afkMoveTicks;

            if (afkMoveTick > 0 && afkMoveTick < 2)
            {
                movement.moveForward += Math.random();
                movement.forwardKeyDown = true;
            }
            else if (afkMoveTick > 2 && afkMoveTick < 4)
            {
                movement.moveStrafe += Math.random();
                movement.leftKeyDown = true;
            }
            else if (afkMoveTick > 4 && afkMoveTick < 6)
            {
                movement.moveForward -= Math.random();
                movement.backKeyDown = true;
            }
            else if (afkMoveTick > 6 && afkMoveTick < 8)
            {
                movement.moveStrafe -= Math.random();
                movement.rightKeyDown = true;
            }
        }
    }

    @SubscribeEvent
    public void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        IndicatiaEventHandler.stopCommandTicks();
    }

    @SubscribeEvent
    public void onPressKey(InputEvent.KeyInputEvent event)
    {
        if (KeyBindingHandler.KEY_QUICK_CONFIG.isKeyDown())
        {
            this.mc.displayGuiScreen(new ExtendedConfigScreen());
        }
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event)
    {
        Screen screen = event.getGui();

        if (screen instanceof MainMenuScreen)
        {
            int height = screen.height / 4 + 48;
            event.addWidget(new MojangStatusButton(screen.width / 2 + 104, height + 63, button -> this.mc.displayGuiScreen(new MojangStatusScreen(screen))));
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event)
    {
        if (this.mc.currentScreen instanceof OffsetRenderPreviewScreen)
        {
            event.setCanceled(true);
        }
    }

    private static void getRealTimeServerPing(ServerData server)
    {
        IndicatiaEventHandler.REALTIME_PINGER.submit(() ->
        {
            try
            {
                ServerAddress address = ServerAddress.fromString(server.serverIP);
                NetworkManager manager = NetworkManager.createNetworkManagerAndConnect(InetAddress.getByName(address.getIP()), address.getPort(), false);

                manager.setNetHandler(new IClientStatusNetHandler()
                {
                    private long currentSystemTime = 0L;

                    @Override
                    public void handleServerInfo(@Nonnull SServerInfoPacket packet)
                    {
                        this.currentSystemTime = Util.milliTime();
                        manager.sendPacket(new CPingPacket(this.currentSystemTime));
                    }

                    @Override
                    public void handlePong(@Nonnull SPongPacket packet)
                    {
                        long i = this.currentSystemTime;
                        long j = Util.milliTime();
                        IndicatiaEventHandler.currentServerPing = (int) (j - i);
                    }

                    @Override
                    public void onDisconnect(@Nonnull ITextComponent component) {}

                    @Override
                    public NetworkManager getNetworkManager()
                    {
                        return manager;
                    }
                });
                manager.sendPacket(new CHandshakePacket(address.getIP(), address.getPort(), ProtocolType.STATUS));
                manager.sendPacket(new CServerQueryPacket());
            }
            catch (Exception e) {}
        });
    }

    private static void afkTick(ClientPlayerEntity player)
    {
        if (IndicatiaEventHandler.START_AFK)
        {
            IndicatiaEventHandler.afkTicks++;
            int tick = IndicatiaEventHandler.afkTicks;
            int messageMin = 1200 * IndicatiaConfig.GENERAL.afkMessageTime.get();
            float angle = tick % 2 == 0 ? 0.0001F : -0.0001F;

            if (IndicatiaConfig.GENERAL.enableAFKMessage.get())
            {
                if (tick % messageMin == 0)
                {
                    String reason = IndicatiaEventHandler.AFK_REASON;
                    reason = StringUtils.isNullOrEmpty(reason) ? "" : ", " + LangUtils.translate("commands.afk.reason") + ": " + reason;
                    player.sendChatMessage("AFK : " + StringUtils.ticksToElapsedTime(tick) + " minute" + (tick == 0 ? "" : "s") + reason);
                }
            }

            switch (IndicatiaEventHandler.AFK_MODE)
            {
            case IDLE:
                player.rotateTowards(angle, angle);
                break;
            case RANDOM_MOVE:
                player.rotateTowards(angle, angle);
                IndicatiaEventHandler.afkMoveTicks++;
                IndicatiaEventHandler.afkMoveTicks %= 8;
                break;
            case RANDOM_360:
                player.rotateTowards((float)(Math.random() + 1.0F), 0.0F);
                break;
            case RANDOM_MOVE_360:
                player.rotateTowards((float)(Math.random() + 1.0F), 0.0F);
                IndicatiaEventHandler.afkMoveTicks++;
                IndicatiaEventHandler.afkMoveTicks %= 8;
                break;
            }
        }
        else
        {
            IndicatiaEventHandler.afkTicks = 0;
        }
    }

    private static void stopCommandTicks()
    {
        if (IndicatiaEventHandler.START_AFK)
        {
            IndicatiaEventHandler.START_AFK = false;
            IndicatiaEventHandler.AFK_REASON = "";
            IndicatiaEventHandler.afkTicks = 0;
            IndicatiaEventHandler.afkMoveTicks = 0;
            IndicatiaEventHandler.AFK_MODE = AFKMode.IDLE;
            IndicatiaMod.LOGGER.info("Stopping AFK Command");
        }
        if (IndicatiaEventHandler.START_AUTO_FISH)
        {
            IndicatiaEventHandler.START_AUTO_FISH = false;
            IndicatiaEventHandler.autoFishTick = 0;
            IndicatiaMod.LOGGER.info("Stopping Autofish Command");
        }
    }

    private static void autoFishTick(Minecraft mc)
    {
        if (IndicatiaEventHandler.START_AUTO_FISH)
        {
            IndicatiaEventHandler.autoFishTick++;

            if (mc.objectMouseOver != null)
            {
                if (IndicatiaEventHandler.autoFishTick % 4 == 0)
                {
                    for (Hand hand : Hand.values())
                    {
                        ItemStack itemStack = mc.player.getHeldItem(hand);
                        boolean mainHand = mc.player.getHeldItemMainhand().getItem() instanceof FishingRodItem;
                        boolean offHand = mc.player.getHeldItemOffhand().getItem() instanceof FishingRodItem;

                        if (mc.player.getHeldItemMainhand().getItem() instanceof FishingRodItem)
                        {
                            offHand = false;
                        }

                        if (mainHand || offHand)
                        {
                            if (mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK)
                            {
                                BlockRayTraceResult blockRayTrace = (BlockRayTraceResult)mc.objectMouseOver;
                                ActionResultType result = mc.playerController.func_217292_a(mc.player, mc.world, hand, blockRayTrace);

                                if (result.isSuccessOrConsume())
                                {
                                    if (result.isSuccess())
                                    {
                                        mc.player.swingArm(hand);
                                    }
                                    return;
                                }
                                if (result == ActionResultType.FAIL)
                                {
                                    return;
                                }
                            }
                        }
                        else
                        {
                            IndicatiaEventHandler.START_AUTO_FISH = false;
                            IndicatiaEventHandler.autoFishTick = 0;
                            mc.player.sendMessage(LangUtils.translate("commands.auto_fish.not_equipped_fishing_rod").deepCopy().mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                            return;
                        }

                        if (!itemStack.isEmpty())
                        {
                            ActionResultType result = mc.playerController.processRightClick(mc.player, mc.world, hand);

                            if (result.isSuccessOrConsume())
                            {
                                if (result.isSuccess())
                                {
                                    mc.player.swingArm(hand);
                                }
                                mc.gameRenderer.itemRenderer.resetEquippedProgress(hand);
                                return;
                            }
                        }
                    }
                }
            }
        }
        else
        {
            IndicatiaEventHandler.autoFishTick = 0;
        }
    }
}