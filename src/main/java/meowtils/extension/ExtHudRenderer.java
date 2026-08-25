package meowtils.extension;

import java.util.Collections;
import java.util.List;

import meowtils.extension.font.FontManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.GuiUtil;
import wtf.tatp.meowtils.gui.hudeditor.HudEntry;

public class ExtHudRenderer {

    private final ExtHud extension;
    private final ExtHudLogic logic;
    private final ExtAnimations animations;

    private int lastMaxWidth = -1;

    public ExtHudRenderer(ExtHud extension, ExtHudLogic logic) {
        this.extension = extension;
        this.logic = logic;
        this.animations = new ExtAnimations();
    }

    public int[] getHudBounds() {
        List<String> lines = logic.getDisplayLines();
        String longestText = logic.getLongestText(lines);
        int lineCount = logic.getLineCount(lines);

        return GuiUtil.getHudBounds(longestText, lineCount, 1.0f);
    }

    public List<HudEntry> getHudEntries() {
        return Collections.singletonList(
            new HudEntry("ExtHud", extension, "posX", "posY", this::getHudBounds)
        );
    }

    private int getStringWidth(String text) {
        if (!extension.fontMode.equals("Default") && FontManager.fonts.containsKey(extension.fontMode)) {
            return FontManager.fonts.get(extension.fontMode).getStringWidth(text);
        }
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
    }

    private int getFontHeight() {
        if (!extension.fontMode.equals("Default") && FontManager.fonts.containsKey(extension.fontMode)) {
            return FontManager.fonts.get(extension.fontMode).getHeight();
        }
        return Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
    }

    public void render() {
        List<String> targetLines = logic.getDisplayLines();
        
        List<String> renderList = animations.updateAndGetRenderList(
            targetLines, 
            extension.animations, 
            8.0f, 
            (a, b) -> {
                boolean aDebug = a.startsWith("Enabled extensions:");
                boolean bDebug = b.startsWith("Enabled extensions:");
                if (aDebug && !bDebug) return -1;
                if (!aDebug && bDebug) return 1;
                return Integer.compare(getStringWidth(b), getStringWidth(a));
            }
        );

        if (renderList.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        
        int fontHeight = getFontHeight();
        int lineHeight = fontHeight + 2; 

        String longestText = logic.getLongestText(renderList);
        int currentMaxWidth = getStringWidth(longestText);

        ScaledResolution sr = new ScaledResolution(mc);
        boolean isRightSide = extension.posX > (sr.getScaledWidth() / 2);

        if (lastMaxWidth != -1 && currentMaxWidth != lastMaxWidth) {
            if (isRightSide) {
                extension.posX -= (currentMaxWidth - lastMaxWidth);
            }
        }
        lastMaxWidth = currentMaxWidth;

        int bgAlphaBase = (int) ((extension.bgOpacity / 100.0f) * 255.0f);
        bgAlphaBase = Math.max(0, Math.min(255, bgAlphaBase));

        for (int i = 0; i < renderList.size(); i++) {
            String text = renderList.get(i);
            int textWidth = getStringWidth(text);
            
            float easeProgress = animations.getEaseProgress(text);
            int slideOffset = (int) ((textWidth + 10) * (1.0f - easeProgress));

            int drawX;

            if (isRightSide) {
                int xOffset = currentMaxWidth - textWidth;
                drawX = extension.posX + xOffset + slideOffset;
            } else {
                drawX = extension.posX - slideOffset;
            }

            int drawY = extension.posY + (i * lineHeight);

            int bgAlpha = (int) (bgAlphaBase * easeProgress);
            bgAlpha = Math.max(0, Math.min(255, bgAlpha));
            
            int bgColor = ((bgAlpha & 0xFF) << 24)
                    | ((extension.bgRed & 0xFF) << 16)
                    | ((extension.bgGreen & 0xFF) << 8)
                    | (extension.bgBlue & 0xFF);

            int barAlpha = (int) (255 * easeProgress);
            barAlpha = Math.max(0, Math.min(255, barAlpha));
            
            int barColor = ((barAlpha & 0xFF) << 24)
                    | ((extension.barRed & 0xFF) << 16)
                    | ((extension.barGreen & 0xFF) << 8)
                    | (extension.barBlue & 0xFF);

            int textAlpha = (int) (255 * easeProgress);
            textAlpha = Math.max(0, Math.min(255, textAlpha));
            int textColor = (textAlpha << 24) | 0xFFFFFF;

            if (bgAlpha > 0) {
                Gui.drawRect(
                    drawX - 2,
                    drawY - 1,
                    drawX + textWidth + 2,
                    drawY + fontHeight + 1, 
                    bgColor
                );
            }

            if (extension.showBar && barAlpha > 0) {
                if (isRightSide) {
                    Gui.drawRect(
                        drawX + textWidth + 2,
                        drawY - 1,
                        drawX + textWidth + 4,
                        drawY + fontHeight + 1,
                        barColor
                    );
                } else {
                    Gui.drawRect(
                        drawX - 4,
                        drawY - 1,
                        drawX - 2,
                        drawY + fontHeight + 1,
                        barColor
                    );
                }
            }

            if (!extension.fontMode.equals("Default") && FontManager.fonts.containsKey(extension.fontMode)) {
                FontManager.fonts.get(extension.fontMode).drawStringWithShadow(text, drawX, drawY + 1, textColor);
            } else {
                Meowtils.drawString(text, drawX, drawY + 1, 1, textColor);
            }
        }
    }
}