package meowtils.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import meowtils.extension.font.FontManager;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.extension.ExtensionManager;
import wtf.tatp.meowtils.gui.ClickGUI;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.util.DelayedTask;

public class ExtHudLogic {

    private final ExtHud extension;
    private final Map<String, ExtHud.ModuleFilter> moduleFilters = new HashMap<>();

    public ExtHudLogic(ExtHud extension) {
        this.extension = extension;
    }

    public void setupExtensionFilters(ExpandValue e) {
        new DelayedTask(() -> {
            for (Module module : ExtensionManager.EXTENSION_MODULES) {
                if (module == null || module == extension || "ExtHud".equalsIgnoreCase(module.getName())) {
                    continue;
                }
                ExtHud.ModuleFilter filter = moduleFilters.computeIfAbsent(module.getName(), k -> new ExtHud.ModuleFilter());
                e.addCheck(new CheckValue(module.getName(), "hidden", filter));
            }
            
            ClickGUI gui = Meowtils.getClickGUI();
            if (gui != null) {
                gui.rebuildExtensionsFrame();
            }
        }, 20);
    }

    public boolean isModuleHidden(Module module) {
        if (module == null) {
            return false;
        }
        ExtHud.ModuleFilter filter = moduleFilters.get(module.getName());
        return filter != null && filter.hidden;
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

            if (isModuleHidden(module)) {
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