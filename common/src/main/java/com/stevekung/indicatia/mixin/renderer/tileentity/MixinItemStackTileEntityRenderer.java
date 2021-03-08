package com.stevekung.indicatia.mixin.renderer.tileentity;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.stevekung.indicatia.utils.EnchantedSkullTileEntityRenderer;

import net.minecraft.block.SkullBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

@Mixin(ItemStackTileEntityRenderer.class)
public class MixinItemStackTileEntityRenderer
{
    @Redirect(method = "func_239207_a_(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/model/ItemCameraTransforms$TransformType;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;II)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/tileentity/SkullTileEntityRenderer.render(Lnet/minecraft/util/Direction;FLnet/minecraft/block/SkullBlock$ISkullType;Lcom/mojang/authlib/GameProfile;FLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V"))
    private void renderEnchantedSkull(@Nullable Direction direction, float rotationYaw, SkullBlock.ISkullType skullType, @Nullable GameProfile gameProfile, float animationProgress, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack _matrixStack, IRenderTypeBuffer _buffer, int _combinedLight, int _combinedOverlay)
    {
        if (EnchantedSkullTileEntityRenderer.isVanillaHead(skullType))
        {
            EnchantedSkullTileEntityRenderer.render(skullType, gameProfile, matrixStack, buffer, combinedLight, OverlayTexture.NO_OVERLAY, itemStack.hasEffect());
        }
        else
        {
            SkullTileEntityRenderer.render(direction, rotationYaw, skullType, gameProfile, animationProgress, matrixStack, buffer, combinedLight);
        }
    }
}