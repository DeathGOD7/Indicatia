package stevekung.mods.indicatia.utils;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import stevekung.mods.stevekungslib.utils.client.RenderUtils;

public class RenderUtilsIN
{
    public static void renderEntityHealth(LivingEntity entityLivingBase, String text, double x, double y, double z)
    {
        Minecraft mc = Minecraft.getInstance();
        boolean hasName = entityLivingBase.hasCustomName();
        double distance = entityLivingBase.getDistanceSq(mc.getRenderManager().pointedEntity);
        int maxDistance = 64;

        if (distance <= maxDistance * maxDistance)
        {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float)x, hasName ? (float)y + entityLivingBase.getHeight() + 0.75F : !mc.isSingleplayer() ? (float)y + entityLivingBase.getHeight() + 1F : (float)y + entityLivingBase.getHeight() + 0.5F, (float)z);
            GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef((mc.getRenderManager().options.thirdPersonView == 2 ? -1 : 1) * mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scalef(-0.025F, -0.025F, 0.025F);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);

            if (!entityLivingBase.isSneaking())
            {
                GlStateManager.disableDepthTest();
            }

            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            FontRenderer fontrenderer = mc.fontRenderer;
            int j = fontrenderer.getStringWidth(text) / 2;
            GlStateManager.disableTexture();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            vertexbuffer.pos(-j - 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            vertexbuffer.pos(-j - 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            vertexbuffer.pos(j + 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            vertexbuffer.pos(j + 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture();

            if (!entityLivingBase.isSneaking())
            {
                fontrenderer.drawString(text, -fontrenderer.getStringWidth(text) / 2, 0, 553648127);
                GlStateManager.enableDepthTest();
            }

            GlStateManager.depthMask(true);
            fontrenderer.drawString(text, -fontrenderer.getStringWidth(text) / 2, 0, entityLivingBase.isSneaking() ? 553648127 : -1);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    public static void bindKeystrokeTexture(String texture)
    {
        RenderUtils.bindTexture(new ResourceLocation("indicatia:textures/gui/" + texture + ".png"));
        GlStateManager.color3f(1.0F, 1.0F, 1.0F);
    }

    public static void drawRect(int left, int top, int right, int bottom, int color, float alpha)
    {
        if (alpha > 0.1F)
        {
            if (left < right)
            {
                int i = left;
                left = right;
                right = i;
            }
            if (top < bottom)
            {
                int j = top;
                top = bottom;
                bottom = j;
            }
            float r = (color >> 16 & 255) / 255.0F;
            float g = (color >> 8 & 255) / 255.0F;
            float b = (color & 255) / 255.0F;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture();
            GlStateManager.blendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color4f(r, g, b, alpha);
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
            vertexbuffer.pos(left, bottom, 0.0D).endVertex();
            vertexbuffer.pos(right, bottom, 0.0D).endVertex();
            vertexbuffer.pos(right, top, 0.0D).endVertex();
            vertexbuffer.pos(left, top, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture();
            GlStateManager.disableBlend();
        }
    }
}