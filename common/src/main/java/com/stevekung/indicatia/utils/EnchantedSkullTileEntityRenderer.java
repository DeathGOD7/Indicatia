package com.stevekung.indicatia.utils;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.SkullBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.GenericHeadModel;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;

public class EnchantedSkullTileEntityRenderer
{
    public static void render(SkullBlock.ISkullType skullType, @Nullable GameProfile gameProfile, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedHurt, boolean glint)
    {
        GenericHeadModel genericheadmodel = SkullTileEntityRenderer.MODELS.get(skullType);
        matrixStack.push();
        matrixStack.translate(0.5D, 0.0D, 0.5D);
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        IVertexBuilder ivertexbuilder;

        if (gameProfile == null)
        {
            ivertexbuilder = ItemRenderer.getArmorVertexBuilder(buffer, RenderType.getEntityCutoutNoCullZOffset(SkullTileEntityRenderer.SKINS.get(skullType)), false, glint);
        }
        else
        {
            ivertexbuilder = ItemRenderer.getEntityGlintVertexBuilder(buffer, SkullTileEntityRenderer.getRenderType(skullType, gameProfile), false, glint);
        }

        genericheadmodel.func_225603_a_(0.0F, 180.0F, 0.0F);
        genericheadmodel.render(matrixStack, ivertexbuilder, combinedLight, combinedHurt, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();
    }

    public static boolean isVanillaHead(SkullBlock.ISkullType skullType)
    {
        return Arrays.stream(SkullBlock.Types.values()).anyMatch(type -> skullType == type);
    }
}