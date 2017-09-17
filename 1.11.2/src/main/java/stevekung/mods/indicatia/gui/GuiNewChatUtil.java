package stevekung.mods.indicatia.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import stevekung.mods.indicatia.config.ConfigManager;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.core.IndicatiaMod;
import stevekung.mods.indicatia.gui.GuiDropdownElement.IDropboxCallback;
import stevekung.mods.indicatia.renderer.HUDInfo;
import stevekung.mods.indicatia.util.HideNameData;
import stevekung.mods.indicatia.util.InfoUtil;

public class GuiNewChatUtil extends GuiChat implements IDropboxCallback
{
    private static final Splitter NEWLINE_SPLITTER = Splitter.on('\n');
    private boolean isDragging;
    private int lastPosX;
    private int lastPosY;
    private static int gameTypeSelected;
    private GuiDropdownElement lobbyOptions;

    // skywars solo
    private GuiButton swLobby;
    private GuiButton swSoloNormal;
    private GuiButton swSoloInsane;
    private GuiButton swRankedMode;

    // skywars team
    private GuiButton swTeamNormal;
    private GuiButton swTeamInsane;
    private GuiButton swMegaMode;

    // skywars labs solo
    private GuiButton swSoloTNT;
    private GuiButton swSoloRush;
    private GuiButton swSoloSlime;

    // skywars labs team
    private GuiButton swTeamTNT;
    private GuiButton swTeamRush;
    private GuiButton swTeamSlime;

    // bedwars
    private GuiButton bwLobby;
    private GuiButton bwEightOne;
    private GuiButton bwEightTwo;
    private GuiButton bwFourThree;
    private GuiButton bwFourFour;

    // prototype
    private GuiButton ptlLobby;
    private GuiButton ptlMurder;

    public GuiNewChatUtil() {}

    public GuiNewChatUtil(String input)
    {
        super(input);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        boolean enableCPS = ConfigManager.enableCPS && ExtendedConfig.CPS_POSITION.equalsIgnoreCase("custom");

        if (enableCPS)
        {
            this.buttonList.add(new GuiButton(0, this.width - 63, this.height - 35, 60, 20, "Reset CPS"));
            this.buttonList.add(new GuiCPSSlider(1, this.width - 165, this.height - 35, GuiCPSSlider.Options.CPS_OPACITY));
        }
        if (InfoUtil.INSTANCE.isHypixel())
        {
            // hypixel
            this.buttonList.add(new GuiButton(100, this.width - 63, enableCPS ? this.height - 56 : this.height - 35, 60, 20, "Reset Chat"));
            this.buttonList.add(new GuiButton(101, this.width - 63, enableCPS ? this.height - 77 : this.height - 56, 60, 20, "Party Chat"));
            this.buttonList.add(new GuiButton(102, this.width - 63, enableCPS ? this.height - 98 : this.height - 77, 60, 20, "Guild Chat"));
            this.buttonList.add(this.lobbyOptions = new GuiDropdownElement(this, this.width - 123, 2, "Skywars: Solo", "Skywars: Team", "Skywars Labs: Solo", "Skywars Labs: Team", "Bedwars", "Prototype"));

            // skywars solo
            this.buttonList.add(this.swLobby = new GuiButton(1000, this.width - 72, 23, 70, 20, "\u00a7eSW Lobby"));

            this.buttonList.add(this.swSoloNormal = new GuiButton(1001, this.width - 72, 44, 70, 20, "Solo Normal"));
            this.buttonList.add(this.swSoloInsane = new GuiButton(1002, this.width - 72, 65, 70, 20, "Solo Insane"));
            this.buttonList.add(this.swRankedMode = new GuiButton(1003, this.width - 72, 86, 70, 20, "Ranked Mode"));

            // skywars team
            this.buttonList.add(this.swTeamNormal = new GuiButton(1004, this.width - 72, 44, 70, 20, "Team Normal"));
            this.buttonList.add(this.swTeamInsane = new GuiButton(1005, this.width - 72, 65, 70, 20, "Team Insane"));
            this.buttonList.add(this.swMegaMode = new GuiButton(1006, this.width - 72, 86, 70, 20, "Mega Mode"));

            // skywars labs solo
            this.buttonList.add(this.swSoloTNT = new GuiButton(1007, this.width - 72, 44, 70, 20, "Solo TNT"));
            this.buttonList.add(this.swSoloRush = new GuiButton(1008, this.width - 72, 65, 70, 20, "Solo Rush"));
            this.buttonList.add(this.swSoloSlime = new GuiButton(1009, this.width - 72, 86, 70, 20, "Solo Slime"));

            // skywars labs team
            this.buttonList.add(this.swTeamTNT = new GuiButton(1010, this.width - 72, 44, 70, 20, "Team TNT"));
            this.buttonList.add(this.swTeamRush = new GuiButton(1011, this.width - 72, 65, 70, 20, "Team Rush"));
            this.buttonList.add(this.swTeamSlime = new GuiButton(1012, this.width - 72, 86, 70, 20, "Team Slime"));

            // bedwars
            this.buttonList.add(this.bwLobby = new GuiButton(1013, this.width - 72, 23, 70, 20, "\u00a7eBW Lobby"));
            this.buttonList.add(this.bwEightOne = new GuiButton(1014, this.width - 72, 44, 70, 20, "Solo"));
            this.buttonList.add(this.bwEightTwo = new GuiButton(1015, this.width - 72, 65, 70, 20, "Doubles"));
            this.buttonList.add(this.bwFourThree = new GuiButton(1016, this.width - 72, 86, 70, 20, "3v3v3v3"));
            this.buttonList.add(this.bwFourFour = new GuiButton(1017, this.width - 72, 107, 70, 20, "4v4v4v4"));

            // prototype
            this.buttonList.add(this.ptlLobby = new GuiButton(1018, this.width - 72, 23, 70, 20, "\u00a7ePTL Lobby"));
            this.buttonList.add(this.ptlMurder = new GuiButton(1019, this.width - 72, 44, 70, 20, "Murder"));

            for (GuiButton button : this.buttonList)
            {
                if (!button.getClass().equals(GuiDropdownElement.class) && !(button.id >= 0 && button.id <= 102))
                {
                    button.visible = false;
                }
            }
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        if (InfoUtil.INSTANCE.isHypixel())
        {
            boolean clicked = !this.lobbyOptions.dropdownClicked;

            switch (GuiNewChatUtil.gameTypeSelected)
            {
            case 0:
                this.swLobby.visible = clicked;
                this.swSoloNormal.visible = clicked;
                this.swSoloInsane.visible = clicked;
                this.swRankedMode.visible = clicked;
                break;
            case 1:
                this.swLobby.visible = clicked;
                this.swTeamNormal.visible = clicked;
                this.swTeamInsane.visible = clicked;
                this.swMegaMode.visible = clicked;
                break;
            case 2:
                this.swLobby.visible = clicked;
                this.swSoloTNT.visible = clicked;
                this.swSoloRush.visible = clicked;
                this.swSoloSlime.visible = clicked;
                break;
            case 3:
                this.swLobby.visible = clicked;
                this.swTeamTNT.visible = clicked;
                this.swTeamRush.visible = clicked;
                this.swTeamSlime.visible = clicked;
                break;
            case 4:
                this.bwLobby.visible = clicked;
                this.bwEightOne.visible = clicked;
                this.bwEightTwo.visible = clicked;
                this.bwFourThree.visible = clicked;
                this.bwFourFour.visible = clicked;
                break;
            case 5:
                this.ptlLobby.visible = clicked;
                this.ptlMurder.visible = clicked;
                break;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1)
        {
            this.mc.displayGuiScreen((GuiScreen)null);
        }
        else if (keyCode != 28 && keyCode != 156)
        {
            super.keyTyped(typedChar, keyCode);
        }
        else
        {
            String text = this.inputField.getText().trim();

            if (!text.isEmpty())
            {
                this.sendChatMessage(text);
            }
            this.mc.displayGuiScreen((GuiScreen)null);
        }
        ExtendedConfig.save();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (ConfigManager.enableCPS && ExtendedConfig.CPS_POSITION.equalsIgnoreCase("custom"))
        {
            String space = ConfigManager.enableRCPS ? " " : "";
            int minX = ExtendedConfig.CPS_X_OFFSET;
            int minY = ExtendedConfig.CPS_Y_OFFSET;
            int maxX = ExtendedConfig.CPS_X_OFFSET + this.fontRendererObj.getStringWidth(HUDInfo.getCPS() + space + HUDInfo.getRCPS()) + 4;
            int maxY = ExtendedConfig.CPS_Y_OFFSET + 12;

            if (mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY)
            {
                this.isDragging = true;
                this.lastPosX = mouseX;
                this.lastPosY = mouseY;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (ConfigManager.enableCPS && ExtendedConfig.CPS_POSITION.equalsIgnoreCase("custom"))
        {
            if (state == 0 && this.isDragging)
            {
                this.isDragging = false;
            }
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        if (ConfigManager.enableCPS && ExtendedConfig.CPS_POSITION.equalsIgnoreCase("custom"))
        {
            if (this.isDragging)
            {
                ExtendedConfig.CPS_X_OFFSET += mouseX - this.lastPosX;
                ExtendedConfig.CPS_Y_OFFSET += mouseY - this.lastPosY;
                this.lastPosX = mouseX;
                this.lastPosY = mouseY;
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        switch (button.id)
        {
        case 0:
            ScaledResolution res = new ScaledResolution(this.mc);
            ExtendedConfig.CPS_X_OFFSET = res.getScaledWidth() / 2 - 36;
            ExtendedConfig.CPS_Y_OFFSET = res.getScaledHeight() / 2 - 5;
            ExtendedConfig.save();
            break;
        case 100:
            this.mc.player.sendChatMessage("/chat a");
            this.mc.player.sendMessage(IndicatiaMod.json.text("Reset Hypixel Chat"));
            break;
        case 101:
            this.mc.player.sendChatMessage("/chat p");
            this.mc.player.sendMessage(IndicatiaMod.json.text("Set chat mode to Hypixel Party Chat"));
            break;
        case 102:
            this.mc.player.sendChatMessage("/chat g");
            this.mc.player.sendMessage(IndicatiaMod.json.text("Set chat mode to Hypixel Guild Chat"));
            break;
        case 1000:
            this.mc.player.sendChatMessage("/lobby sw");
            break;
        case 1001:
            this.mc.player.sendChatMessage("/play solo_normal");
            break;
        case 1002:
            this.mc.player.sendChatMessage("/play solo_insane");
            break;
        case 1003:
            this.mc.player.sendChatMessage("/play ranked_normal");
            break;
        case 1004:
            this.mc.player.sendChatMessage("/play teams_normal");
            break;
        case 1005:
            this.mc.player.sendChatMessage("/play teams_insane");
            break;
        case 1006:
            this.mc.player.sendChatMessage("/play mega_normal");
            break;
        case 1007:
            this.mc.player.sendChatMessage("/play solo_insane_tnt_madness");
            break;
        case 1008:
            this.mc.player.sendChatMessage("/play solo_insane_rush");
            break;
        case 1009:
            this.mc.player.sendChatMessage("/play solo_insane_slime");
            break;
        case 1010:
            this.mc.player.sendChatMessage("/play teams_insane_tnt_madness");
            break;
        case 1011:
            this.mc.player.sendChatMessage("/play teams_insane_rush");
            break;
        case 1012:
            this.mc.player.sendChatMessage("/play teams_insane_slime");
            break;
        case 1013:
            this.mc.player.sendChatMessage("/lobby bw");
            break;
        case 1014:
            this.mc.player.sendChatMessage("/play bedwars_eight_one");
            break;
        case 1015:
            this.mc.player.sendChatMessage("/play bedwars_eight_two");
            break;
        case 1016:
            this.mc.player.sendChatMessage("/play bedwars_four_three");
            break;
        case 1017:
            this.mc.player.sendChatMessage("/play bedwars_four_four");
            break;
        case 1018:
            this.mc.player.sendChatMessage("/lobby ptl");
            break;
        case 1019:
            this.mc.player.sendChatMessage("/play prototype_murder_mystery");
            break;
        }
    }

    @Override
    public void onSelectionChanged(GuiDropdownElement dropdown, int selection)
    {
        GuiNewChatUtil.gameTypeSelected = selection;
    }

    @Override
    public int getInitialSelection(GuiDropdownElement dropdown)
    {
        return GuiNewChatUtil.gameTypeSelected;
    }

    @Override
    protected void handleComponentHover(ITextComponent component, int mouseX, int mouseY)
    {
        if (component != null && component.getStyle().getHoverEvent() != null)
        {
            HoverEvent hover = component.getStyle().getHoverEvent();

            if (hover.getAction() == HoverEvent.Action.SHOW_ITEM)
            {
                ItemStack itemStack = ItemStack.EMPTY;

                try
                {
                    NBTBase base = JsonToNBT.getTagFromJson(hover.getValue().getUnformattedText());

                    if (base instanceof NBTTagCompound)
                    {
                        itemStack = new ItemStack((NBTTagCompound)base);
                    }
                }
                catch (NBTException e) {}

                if (itemStack.isEmpty())
                {
                    this.drawCreativeTabHoveringText(TextFormatting.RED + "Invalid Item!", mouseX, mouseY);
                }
                else
                {
                    this.renderToolTip(itemStack, mouseX, mouseY);
                }
            }
            else if (hover.getAction() == HoverEvent.Action.SHOW_ENTITY)
            {
                if (this.mc.gameSettings.advancedItemTooltips)
                {
                    try
                    {
                        NBTTagCompound compound = JsonToNBT.getTagFromJson(hover.getValue().getUnformattedText());
                        List<String> list1 = new ArrayList<>();
                        String name = compound.getString("name");

                        for (String hide : HideNameData.getHideNameList())
                        {
                            if (name.contains(hide))
                            {
                                name = name.replace(hide, TextFormatting.OBFUSCATED + hide + TextFormatting.RESET);
                            }
                        }

                        list1.add(name);

                        if (compound.hasKey("type", 8))
                        {
                            String s = compound.getString("type");
                            list1.add("Type: " + s);
                        }
                        list1.add(compound.getString("id"));
                        this.drawHoveringText(list1, mouseX, mouseY);
                    }
                    catch (NBTException e)
                    {
                        this.drawCreativeTabHoveringText(TextFormatting.RED + "Invalid Entity!", mouseX, mouseY);
                    }
                }
            }
            else if (hover.getAction() == HoverEvent.Action.SHOW_TEXT)
            {
                this.drawHoveringText(NEWLINE_SPLITTER.splitToList(hover.getValue().getFormattedText()), mouseX, mouseY);
            }
            else if (hover.getAction() == HoverEvent.Action.SHOW_ACHIEVEMENT)
            {
                StatBase base = StatList.getOneShotStat(hover.getValue().getUnformattedText());

                if (base != null)
                {
                    ITextComponent itextcomponent = base.getStatName();
                    ITextComponent itextcomponent1 = new TextComponentTranslation("stats.tooltip.type." + (base.isAchievement() ? "achievement" : "statistic"));
                    itextcomponent1.getStyle().setItalic(Boolean.valueOf(true));
                    String s1 = base instanceof Achievement ? ((Achievement)base).getDescription() : null;
                    List<String> list = Lists.newArrayList(new String[] {itextcomponent.getFormattedText(), itextcomponent1.getFormattedText()});

                    if (s1 != null)
                    {
                        list.addAll(this.fontRendererObj.listFormattedStringToWidth(s1, 150));
                    }
                    this.drawHoveringText(list, mouseX, mouseY);
                }
                else
                {
                    this.drawCreativeTabHoveringText(TextFormatting.RED + "Invalid statistic/achievement!", mouseX, mouseY);
                }
            }
            GlStateManager.disableLighting();
        }
    }
}