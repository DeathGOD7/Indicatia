package com.stevekung.indicatia.event;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.stevekung.indicatia.config.Equipments;
import com.stevekung.indicatia.config.IndicatiaConfig;
import com.stevekung.indicatia.config.IndicatiaSettings;
import com.stevekung.indicatia.gui.exconfig.screen.OffsetRenderPreviewScreen;
import com.stevekung.indicatia.hud.EffectOverlays;
import com.stevekung.indicatia.hud.EquipmentOverlays;
import com.stevekung.indicatia.hud.InfoOverlays;
import com.stevekung.indicatia.hud.InfoUtils;
import com.stevekung.indicatia.utils.event.InfoOverlayEvent;
import com.stevekung.indicatia.utils.hud.InfoOverlay;
import com.stevekung.stevekungslib.utils.CommonUtils;
import com.stevekung.stevekungslib.utils.LangUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.versions.mcp.MCPVersion;

public class HUDRenderEventHandler
{
    private final Minecraft mc;

    public HUDRenderEventHandler()
    {
        this.mc = Minecraft.getInstance();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

            if (server != null)
            {
                InfoOverlays.getTPS(server);
            }
        }
    }

    @SubscribeEvent
    public void onPreInfoRender(RenderGameOverlayEvent.Pre event)
    {
        MatrixStack matrixStack = event.getMatrixStack();

        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSHEALTH)
        {
            event.setCanceled(!IndicatiaConfig.GENERAL.enableRenderBossHealthStatus.get());
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.POTION_ICONS)
        {
            if (!IndicatiaConfig.GENERAL.enableVanillaPotionHUD.get())
            {
                event.setCanceled(true);
            }
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT)
        {
            if (!this.mc.gameSettings.showDebugInfo)
            {
                if (IndicatiaConfig.GENERAL.enableRenderInfo.get() && this.mc.player != null && this.mc.world != null && !(this.mc.currentScreen instanceof OffsetRenderPreviewScreen))
                {
                    int iLeft = 0;
                    int iRight = 0;

                    for (InfoOverlay info : HUDRenderEventHandler.getInfoOverlays(this.mc))
                    {
                        if (info.isEmpty())
                        {
                            continue;
                        }

                        IFormattableTextComponent value = info.toFormatted();
                        InfoOverlay.Position pos = info.getPos();
                        float defaultPos = 3.0625F;
                        float fontHeight = this.mc.fontRenderer.FONT_HEIGHT + 1;
                        float yOffset = 3 + fontHeight * (pos == InfoOverlay.Position.LEFT ? iLeft : iRight);
                        float xOffset = this.mc.getMainWindow().getScaledWidth() - 2 - this.mc.fontRenderer.getStringWidth(value.getString());
                        this.mc.fontRenderer.func_243246_a(matrixStack, value, pos == InfoOverlay.Position.LEFT ? !IndicatiaSettings.INSTANCE.swapRenderInfo ? defaultPos : xOffset : pos == InfoOverlay.Position.RIGHT ? !IndicatiaSettings.INSTANCE.swapRenderInfo ? xOffset : defaultPos : defaultPos, yOffset, 16777215);

                        if (pos == InfoOverlay.Position.LEFT)
                        {
                            ++iLeft;
                        }
                        else
                        {
                            ++iRight;
                        }
                    }
                }

                if (!this.mc.player.isSpectator() && IndicatiaSettings.INSTANCE.equipmentHUD)
                {
                    if (IndicatiaSettings.INSTANCE.equipmentPosition == Equipments.Position.HOTBAR)
                    {
                        EquipmentOverlays.renderHotbarEquippedItems(this.mc, matrixStack);
                    }
                    else
                    {
                        if (IndicatiaSettings.INSTANCE.equipmentDirection == Equipments.Direction.VERTICAL)
                        {
                            EquipmentOverlays.renderVerticalEquippedItems(this.mc, matrixStack);
                        }
                        else
                        {
                            EquipmentOverlays.renderHorizontalEquippedItems(this.mc, matrixStack);
                        }
                    }
                }

                if (IndicatiaSettings.INSTANCE.potionHUD)
                {
                    EffectOverlays.renderPotionHUD(this.mc, matrixStack);
                }
            }
        }
        else
        {
            if (this.mc.currentScreen instanceof OffsetRenderPreviewScreen)
            {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        InfoOverlays.OVERALL_TPS = InfoOverlay.empty();
        InfoOverlays.OVERWORLD_TPS = InfoOverlay.empty();
        InfoOverlays.TPS = InfoOverlay.empty();
        InfoOverlays.ALL_TPS.clear();
    }

    public static List<InfoOverlay> getInfoOverlays(Minecraft mc)
    {
        List<InfoOverlay> infos = Lists.newArrayList();
        BlockPos playerPos = new BlockPos(mc.getRenderViewEntity().getPosX(), mc.getRenderViewEntity().getBoundingBox().minY, mc.getRenderViewEntity().getPosZ());

        if (IndicatiaSettings.INSTANCE.fps)
        {
            int fps = Minecraft.debugFPS;
            infos.add(new InfoOverlay("hud.fps", String.valueOf(fps), IndicatiaSettings.INSTANCE.fpsColor, fps <= 25 ? IndicatiaSettings.INSTANCE.fpsLow25Color : fps >= 26 && fps <= 49 ? IndicatiaSettings.INSTANCE.fps26And49Color : IndicatiaSettings.INSTANCE.fpsValueColor, InfoOverlay.Position.LEFT));
        }

        if (!mc.isSingleplayer())
        {
            if (IndicatiaSettings.INSTANCE.ping)
            {
                int responseTime = InfoUtils.INSTANCE.getPing();
                infos.add(new InfoOverlay("hud.ping", responseTime + "ms", IndicatiaSettings.INSTANCE.pingColor, InfoUtils.INSTANCE.getResponseTimeColor(responseTime), InfoOverlay.Position.RIGHT));

                if (IndicatiaSettings.INSTANCE.pingToSecond)
                {
                    double responseTimeSecond = InfoUtils.INSTANCE.getPing() / 1000.0D;
                    infos.add(new InfoOverlay("hud.ping.delay", responseTimeSecond + "s", IndicatiaSettings.INSTANCE.pingToSecondColor, InfoUtils.INSTANCE.getResponseTimeColor((int)(responseTimeSecond * 1000.0D)), InfoOverlay.Position.RIGHT));
                }
            }
            if (IndicatiaSettings.INSTANCE.serverIP && mc.getCurrentServerData() != null)
            {
                infos.add(new InfoOverlay("IP", (mc.isConnectedToRealms() ? "Realms Server" : mc.getCurrentServerData().serverIP) + (IndicatiaSettings.INSTANCE.serverIPMCVersion ? "/" + MCPVersion.getMCVersion() : ""), IndicatiaSettings.INSTANCE.serverIPColor, IndicatiaSettings.INSTANCE.serverIPValueColor, InfoOverlay.Position.RIGHT));
            }
        }

        if (IndicatiaSettings.INSTANCE.xyz)
        {
            String stringPos = playerPos.getX() + " " + playerPos.getY() + " " + playerPos.getZ();
            String nether = mc.player.world.getDimensionType().isPiglinSafe() ? "Nether " : "";
            infos.add(new InfoOverlay(nether + "XYZ", stringPos, IndicatiaSettings.INSTANCE.xyzColor, IndicatiaSettings.INSTANCE.xyzValueColor, InfoOverlay.Position.LEFT));

            if (mc.player.world.getDimensionType().isPiglinSafe())
            {
                String stringNetherPos = playerPos.getX() * 8 + " " + playerPos.getY() + " " + playerPos.getZ() * 8;
                infos.add(new InfoOverlay("Overworld XYZ", stringNetherPos, IndicatiaSettings.INSTANCE.xyzColor, IndicatiaSettings.INSTANCE.xyzValueColor, InfoOverlay.Position.LEFT));
            }
        }

        if (IndicatiaSettings.INSTANCE.direction)
        {
            infos.add(InfoOverlays.getDirection(mc));
        }

        if (IndicatiaSettings.INSTANCE.biome)
        {
            ChunkPos chunkPos = new ChunkPos(playerPos);
            Chunk worldChunk = mc.world.getChunk(chunkPos.x, chunkPos.z);
            ResourceLocation biomeResource = mc.world.func_241828_r().getRegistry(Registry.BIOME_KEY).getKey(mc.world.getBiome(playerPos));
            String biomeName = "biome." + biomeResource.getNamespace() + "." + biomeResource.getPath();
            infos.add(new InfoOverlay("hud.biome", !worldChunk.isEmpty() ? new TranslationTextComponent(biomeName).getString() : LangUtils.translate("hud.biome.waiting_for_chunk").getString(), IndicatiaSettings.INSTANCE.biomeColor, IndicatiaSettings.INSTANCE.biomeValueColor, InfoOverlay.Position.LEFT));
        }

        if (IndicatiaSettings.INSTANCE.slimeChunkFinder && !mc.player.world.getDimensionType().isPiglinSafe())
        {
            infos.add(new InfoOverlay("hud.slime_chunk", InfoUtils.INSTANCE.isSlimeChunk(mc.player.getPosition()) ? "gui.yes" : "gui.no", IndicatiaSettings.INSTANCE.slimeChunkColor, IndicatiaSettings.INSTANCE.slimeChunkValueColor, InfoOverlay.Position.LEFT));
        }

        if (IndicatiaSettings.INSTANCE.tps)
        {
            infos.add(InfoOverlays.OVERALL_TPS);
            infos.add(InfoOverlays.OVERWORLD_TPS);
            infos.addAll(InfoOverlays.ALL_TPS);
            infos.add(InfoOverlays.TPS);
        }

        if (IndicatiaSettings.INSTANCE.realTime)
        {
            infos.add(InfoOverlays.getRealWorldTime());
        }
        if (IndicatiaSettings.INSTANCE.gameTime)
        {
            infos.add(InfoOverlays.getGameTime(mc));
        }
        if (IndicatiaSettings.INSTANCE.gameWeather && mc.world.isRaining())
        {
            String weather = !mc.world.isThundering() ? "hud.weather.raining" : "hud.weather.thundering";
            infos.add(new InfoOverlay("hud.weather", weather, IndicatiaSettings.INSTANCE.gameWeatherColor, IndicatiaSettings.INSTANCE.gameWeatherValueColor, InfoOverlay.Position.RIGHT));
        }
        if (IndicatiaSettings.INSTANCE.moonPhase)
        {
            infos.add(new InfoOverlay("hud.moon_phase", InfoUtils.INSTANCE.getMoonPhase(mc), IndicatiaSettings.INSTANCE.moonPhaseColor, IndicatiaSettings.INSTANCE.moonPhaseValueColor, InfoOverlay.Position.RIGHT));
        }
        CommonUtils.post(new InfoOverlayEvent(infos));
        return infos;
    }
}