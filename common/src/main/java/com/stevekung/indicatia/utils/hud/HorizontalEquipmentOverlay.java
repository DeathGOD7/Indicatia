package com.stevekung.indicatia.utils.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.stevekung.indicatia.config.Equipments;
import com.stevekung.indicatia.config.IndicatiaSettings;
import com.stevekung.stevekunglib.utils.ColorUtils;
import com.stevekung.stevekunglib.utils.TextComponentUtils;
import com.stevekung.stevekunglib.utils.client.ClientUtils;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

public class HorizontalEquipmentOverlay extends EquipmentOverlay
{
    private int width;
    private int itemDamageWidth;

    public HorizontalEquipmentOverlay(ItemStack itemStack)
    {
        super(itemStack);
        this.initSize();
    }

    public int getWidth()
    {
        return this.width;
    }

    public void render(PoseStack poseStack, int x, int y)
    {
        var right = IndicatiaSettings.INSTANCE.equipmentPosition == Equipments.Position.RIGHT;
        var arrowInfo = TextComponentUtils.component(this.renderArrowInfo()).copy();
        arrowInfo.setStyle(arrowInfo.getStyle().withFont(ClientUtils.UNICODE));
        EquipmentOverlay.renderItem(this.itemStack, right ? x - 18 : x, y);
        this.mc.font.drawShadow(poseStack, this.renderInfo(), right ? x - 20 - this.itemDamageWidth : x + 18, y + 4, ColorUtils.rgbToDecimal(IndicatiaSettings.INSTANCE.equipmentStatusColor));

        if (this.itemStack.getItem() instanceof BowItem)
        {
            RenderSystem.disableDepthTest();
            this.mc.font.drawShadow(poseStack, arrowInfo, right ? x - this.mc.font.width(arrowInfo) : x + 6, y + 8, ColorUtils.rgbToDecimal(IndicatiaSettings.INSTANCE.arrowCountColor));
            RenderSystem.enableDepthTest();
        }
    }

    private void initSize()
    {
        this.itemDamageWidth = this.mc.font.width(this.renderInfo());
        this.width = 20 + this.itemDamageWidth;
    }
}