package meowtils.extension;

import meowtils.extension.font.FontManager;
import wtf.tatp.meowtils.extension.Extension;

public class Main {

    public static void init() {
        FontManager.init();
        Extension.registerModule(new ExtHud());
    }
}
