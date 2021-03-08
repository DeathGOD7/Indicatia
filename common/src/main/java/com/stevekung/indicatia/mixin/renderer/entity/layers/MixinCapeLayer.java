package com.stevekung.indicatia.mixin.renderer.entity.layers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.stevekung.indicatia.config.IndicatiaConfig;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.model.PlayerModel;

@Mixin(CapeLayer.class)
public class MixinCapeLayer
{
    @Redirect(method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/entity/model/PlayerModel.renderCape(Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;II)V"))
    private void renderCape(PlayerModel<?> playerModel, MatrixStack _matrixStackIn, IVertexBuilder _bufferIn, int _packedLightIn, int _packedOverlayIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        playerModel.renderCape(_matrixStackIn, _bufferIn, _packedLightIn, IndicatiaConfig.GENERAL.enableOldArmorRender.get() ? LivingRenderer.getPackedOverlay(entitylivingbaseIn, 0.0F) : _packedOverlayIn);
    }
}