package meowtils.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import meowtils.extension.font.FontManager;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.extension.ExtensionManager;
import wtf.tatp.meowtils.gui.Module;

public class ExtHudLogic {

    private final ExtHud extension;

    public ExtHudLogic(ExtHud extension) {
        this.extension = extension;
    }

    public List<String> getEnabledExtensions() {
        List<String> extensions = new ArrayList<String>();

        for (Module module : ExtensionManager.EXTENSION_MODULES) {
            if (module == null) {
                continue;
            }

            if (module == extension) {
                continue;
            }

            if (!module.getState()) {
                continue;
            }

            extensions.add(module.getName());
        }

        // sort by actual pixel width depending on current font mode
        Collections.sort(extensions, (a, b) -> Integer.compare(
            getStringWidth(b),
            getStringWidth(a)
        ));

        return extensions;
    }

    public List<String> getDisplayLines() {
        List<String> lines = new ArrayList<String>();

        List<String> extensions = getEnabledExtensions();

        if (extension.debug) {
            lines.add("Enabled extensions: " + extensions.size());
        }

        lines.addAll(extensions);

        return lines;
    }

    public String getLongestText(List<String> lines) {
        String longestText = "";

        for (String line : lines) {
            if (getStringWidth(line) > getStringWidth(longestText)) {
                longestText = line;
            }
        }

        if (longestText.length() == 0) {
            longestText = "ExtHud";
        }

        return longestText;
    }

    public int getLineCount(List<String> lines) {
        return Math.max(1, lines.size());
    }

    private int getStringWidth(String text) {
        // only use custom font map if it's not "Default" and actually exists in memory
        if (!extension.fontMode.equals("Default") && FontManager.fonts.containsKey(extension.fontMode)) {
            return FontManager.fonts.get(extension.fontMode).getStringWidth(text);
        }
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
    }
}