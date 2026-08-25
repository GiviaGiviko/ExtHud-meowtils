package meowtils.extension;

import java.util.List;

import meowtils.extension.font.FontManager;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.RenderGameOverlayEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.extension.Extension;
import wtf.tatp.meowtils.gui.GuiUtil;
import wtf.tatp.meowtils.gui.hudeditor.HudEntry;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.values.ExpandValue;

public class ExtHud extends Extension {

    @Config public boolean enabled = false;
    @Config public int key = 0;
    @Config public int posX = 4;
    @Config public int posY = 3;
    @Config public boolean debug = false;
    @Config public boolean animations = true;
    
    @Config public String fontMode = "Default";

    @Config public int bgRed = 0;
    @Config public int bgGreen = 0;
    @Config public int bgBlue = 0;
    @Config public float bgOpacity = 20.0f;

    @Config public boolean showBar = true;
    @Config public int barRed = 255;
    @Config public int barGreen = 255;
    @Config public int barBlue = 255;

    private final ExtHudLogic logic;
    private final ExtHudRenderer renderer;

    public ExtHud() {
        super("ExtHud", "Givia");

        logic = new ExtHudLogic(this);
        renderer = new ExtHudRenderer(this, logic);

        tooltip("Displays enabled Meowtils extensions.");

        mode("Font", FontManager.fontNames, "fontMode");
        
        toggle("Animations", "animations");

        expand("Background", (ExpandValue e) -> {
            ColorLink bgColor = linkColor("bgRed", "bgGreen", "bgBlue");
            e.color("Color", bgColor);
            e.saturation(bgColor);
            e.brightness(bgColor);
            e.opacity("Opacity", "bgOpacity");
        });

        expand("Side Bar", (ExpandValue e) -> {
            e.toggle("Show Bar", "showBar");
            ColorLink barColor = linkColor("barRed", "barGreen", "barBlue");
            e.color("Color", barColor);
            e.saturation(barColor);
            e.brightness(barColor);
        });

        toggle("Debug", "debug");
        button("Reset Position", 5, () -> {
            posX = 4;
            posY = 3;
        });
    }

    @Override
    public List<HudEntry> hudEditor() {
        return renderer.getHudEntries();
    }

    @Override
    public void onReset() {
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (event.getPhase() != ClientTickEvent.Phase.POST) {
            return;
        }

        FontManager.checkDebugState(debug, fontMode);
    }

    @EventTarget
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (mc.currentScreen != null && !GuiUtil.inEditor()) {
            return;
        }

        if (!enabled && !GuiUtil.inEditor()) {
            return;
        }

        renderer.render();
    }
}