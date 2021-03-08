package com.stevekung.indicatia.mixin.gui.screen;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.stevekung.indicatia.config.IndicatiaConfig;
import com.stevekung.stevekungslib.utils.LangUtils;
import com.stevekung.stevekungslib.utils.TextComponentUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.ClientHooks;

@Mixin(ServerSelectionList.NormalEntry.class)
public abstract class MixinServerSelectionList_NormalEntry
{
    private final ServerSelectionList.NormalEntry that = (ServerSelectionList.NormalEntry) (Object) this;

    @Shadow
    @Final
    private MultiplayerScreen owner;

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    @Final
    private ServerData server;

    @Shadow
    @Final
    private ResourceLocation serverIcon;

    @Shadow
    private String lastIconB64;

    @Shadow
    private DynamicTexture icon;

    @Shadow
    protected abstract void func_238859_a_(MatrixStack matrixStack, int x, int y, ResourceLocation resource);

    @Shadow
    protected abstract boolean func_241614_a_(@Nullable String icon);

    @Shadow
    protected abstract boolean canJoin();

    @SuppressWarnings("deprecation")
    @Inject(method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;IIIIIIIZF)V", cancellable = true, at = @At("HEAD"))
    private void render(MatrixStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks, CallbackInfo info)
    {
        if (IndicatiaConfig.GENERAL.multiplayerScreenEnhancement.get())
        {
            if (!this.server.pinged)
            {
                this.server.pinged = true;
                this.server.pingToServer = -2L;
                this.server.serverMOTD = StringTextComponent.EMPTY;
                this.server.populationInfo = StringTextComponent.EMPTY;

                ServerSelectionList.field_214358_b.submit(() ->
                {
                    try
                    {
                        this.owner.getOldServerPinger().ping(this.server, () -> this.mc.execute(this.that::func_241613_a_));
                    }
                    catch (UnknownHostException e)
                    {
                        this.server.pingToServer = -1L;
                        this.server.serverMOTD = LangUtils.formatted("multiplayer.status.cannot_resolve", TextFormatting.DARK_RED);
                    }
                    catch (Exception e)
                    {
                        this.server.pingToServer = -1L;
                        this.server.serverMOTD = LangUtils.formatted("multiplayer.status.cannot_connect", TextFormatting.DARK_RED);
                    }
                });
            }

            boolean flag = this.server.version > SharedConstants.getVersion().getProtocolVersion();
            boolean flag1 = this.server.version < SharedConstants.getVersion().getProtocolVersion();
            boolean flag2 = flag || flag1;
            this.mc.fontRenderer.drawString(matrixStack, this.server.serverName, x + 32 + 3, y + 1, 16777215);
            List<IReorderingProcessor> list = this.mc.fontRenderer.trimStringToWidth(this.server.serverMOTD, listWidth - 50);

            for (int i = 0; i < Math.min(list.size(), 2); ++i)
            {
                this.mc.fontRenderer.func_238422_b_(matrixStack, list.get(i), x + 35, y + 12 + 9 * i, 8421504);
            }

            ITextComponent ping = StringTextComponent.EMPTY;
            long responseTime = this.server.pingToServer;
            String responseTimeText = String.valueOf(responseTime);

            if (this.server.serverMOTD.getString().contains(LangUtils.translateString("multiplayer.status.cannot_connect")))
            {
                ping = LangUtils.formatted("menu.failed_to_ping", TextFormatting.DARK_RED);
            }
            else if (responseTime < 0L)
            {
                ping = LangUtils.formatted("multiplayer.status.pinging", TextFormatting.GRAY);
            }
            else if (responseTime >= 200 && responseTime < 300)
            {
                ping = TextComponentUtils.formatted(responseTimeText + "ms", TextFormatting.YELLOW);
            }
            else if (responseTime >= 300 && responseTime < 500)
            {
                ping = TextComponentUtils.formatted(responseTimeText + "ms", TextFormatting.RED);
            }
            else if (responseTime >= 500)
            {
                ping = TextComponentUtils.formatted(responseTimeText + "ms", TextFormatting.DARK_RED);
            }
            else
            {
                ping = TextComponentUtils.formatted(responseTimeText + "ms", TextFormatting.GREEN);
            }

            ITextComponent s2 = flag2 ? this.server.gameVersion.deepCopy().mergeStyle(TextFormatting.DARK_RED) : this.server.populationInfo.deepCopy().appendString(" ").append(ping);
            int j = this.mc.fontRenderer.getStringPropertyWidth(s2);
            this.mc.fontRenderer.func_243248_b(matrixStack, s2, x + listWidth - j - 6, y + 1, 8421504);
            List<ITextComponent> s = Collections.emptyList();

            if (flag2)
            {
                s = this.server.playerList;
            }
            else if (this.server.pinged && this.server.pingToServer != -2L)
            {
                if (this.server.pingToServer > 0L)
                {
                    s = this.server.playerList;
                }
            }

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            String icon = this.server.getBase64EncodedIconData();

            if (!Objects.equals(icon, this.lastIconB64))
            {
                if (this.func_241614_a_(icon))
                {
                    this.lastIconB64 = icon;
                }
                else
                {
                    this.server.setBase64EncodedIconData((String)null);
                    this.that.func_241613_a_();
                }
            }

            if (this.icon != null)
            {
                this.func_238859_a_(matrixStack, x, y, this.serverIcon);
            }
            else
            {
                this.func_238859_a_(matrixStack, x, y, ServerSelectionList.field_214359_c);
            }

            int i1 = mouseX - x;
            int j1 = mouseY - y;

            if (i1 >= listWidth - j - 6 && i1 <= listWidth - 7 && j1 >= 0 && j1 <= 8)
            {
                this.owner.func_238854_b_(s);
            }

            ClientHooks.drawForgePingInfo(this.owner, this.server, matrixStack, x, y, listWidth, i1, j1);

            if (this.mc.gameSettings.touchscreen || isSelected)
            {
                this.mc.getTextureManager().bindTexture(ServerSelectionList.field_214360_d);
                AbstractGui.fill(matrixStack, x, y, x + 32, y + 32, -1601138544);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                int k1 = mouseX - x;
                int l1 = mouseY - y;

                if (this.canJoin())
                {
                    if (k1 < 32 && k1 > 16)
                    {
                        AbstractGui.blit(matrixStack, x, y, 0.0F, 32.0F, 32, 32, 256, 256);
                    }
                    else
                    {
                        AbstractGui.blit(matrixStack, x, y, 0.0F, 0.0F, 32, 32, 256, 256);
                    }
                }

                if (slotIndex > 0)
                {
                    if (k1 < 16 && l1 < 16)
                    {
                        AbstractGui.blit(matrixStack, x, y, 96.0F, 32.0F, 32, 32, 256, 256);
                    }
                    else
                    {
                        AbstractGui.blit(matrixStack, x, y, 96.0F, 0.0F, 32, 32, 256, 256);
                    }
                }
                if (slotIndex < this.owner.getServerList().countServers() - 1)
                {
                    if (k1 < 16 && l1 > 16)
                    {
                        AbstractGui.blit(matrixStack, x, y, 64.0F, 32.0F, 32, 32, 256, 256);
                    }
                    else
                    {
                        AbstractGui.blit(matrixStack, x, y, 64.0F, 0.0F, 32, 32, 256, 256);
                    }
                }
            }
            info.cancel();
        }
    }
}