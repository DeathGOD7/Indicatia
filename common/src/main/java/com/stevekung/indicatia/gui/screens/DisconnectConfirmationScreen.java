package com.stevekung.indicatia.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import com.stevekung.stevekungslib.utils.LangUtils;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class DisconnectConfirmationScreen extends Screen
{
    private final Screen parent;

    public DisconnectConfirmationScreen(Screen parent)
    {
        super(TextComponent.EMPTY);
        this.parent = parent;
    }

    @Override
    public void init()
    {
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 + 96, 150, 20, CommonComponents.GUI_YES, button ->
        {
            var flag = this.minecraft.isLocalServer();
            var flag1 = this.minecraft.isConnectedToRealms();
            var title = new TitleScreen();
            this.minecraft.level.disconnect();

            if (flag)
            {
                this.minecraft.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
            }
            else
            {
                this.minecraft.clearLevel();
            }

            if (flag)
            {
                this.minecraft.setScreen(title);
            }
            else if (flag1)
            {
                this.minecraft.setScreen(new RealmsMainScreen(title));
            }
            else
            {
                this.minecraft.setScreen(new JoinMultiplayerScreen(title));
            }
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20, CommonComponents.GUI_NO, button -> this.minecraft.setScreen(this.parent)));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(poseStack);
        GuiComponent.drawCenteredString(poseStack, this.font, LangUtils.translate("menu.confirm_disconnect"), this.width / 2, 70, 16777215);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }
}