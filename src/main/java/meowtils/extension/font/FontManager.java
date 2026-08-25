package meowtils.extension.font;

import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.Meowtils;

public class FontManager {

    public static final Map<String, FontRenderer> fonts = new HashMap<>();
    public static final List<String> fontNames = new ArrayList<>();
    
    public static String statusMessage = "";
    private static String lastFontMode = "Default";
    private static boolean announcedFontStatus = false;

    public static void init() {
        fontNames.add("Default");

        try {
            Map<String, byte[]> foundFontFiles = loadAllFontBytes();

            for (Map.Entry<String, byte[]> entry : foundFontFiles.entrySet()) {
                String fontName = entry.getKey();
                byte[] bytes = entry.getValue();

                try (InputStream is = new ByteArrayInputStream(bytes)) {
                    // back to 18f so Java actually has enough pixels to draw the letters !
                    Font awtFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(18f);
                    
                    String lowerName = fontName.toLowerCase();
                    boolean isPixelFont = lowerName.contains("minecraft") || lowerName.contains("pixel") || lowerName.contains("bit");
                    
                    boolean antiAlias = !isPixelFont; // false for pixel fonts to keep them crisp
                    
                    // this MUST be true otherwise the letters collapse into each other
                    boolean fractionalMetrics = true; 
                    
                    FontRenderer renderer = new FontRenderer(awtFont, antiAlias, fractionalMetrics);
                    
                    fonts.put(fontName, renderer);
                    fontNames.add(fontName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (fonts.isEmpty()) {
                statusMessage = "§eNo custom fonts found in extension archive";
            } else {
                statusMessage = "§aLoaded " + fonts.size() + " custom font(s) from project assets!";
            }

        } catch (Exception e) {
            statusMessage = "§cFailed to scan for fonts: " + e.getMessage();
        }
    }

    private static Map<String, byte[]> loadAllFontBytes() {
        Map<String, byte[]> foundFonts = new HashMap<>();
        File mcDir = Minecraft.getMinecraft().mcDataDir;
        String[] searchPaths = {"meowtils/extensions", "Meowtils/Extensions", "meowtils", "extensions"};

        for (String relativePath : searchPaths) {
            File dir = new File(mcDir, relativePath);
            if (!dir.isDirectory()) {
                continue;
            }

            File[] extensionFiles = dir.listFiles((d, name) -> name.endsWith(".meowtils") || name.endsWith(".jar"));
            if (extensionFiles == null) {
                continue;
            }

            for (File file : extensionFiles) {
                extractFontsFromZip(file, foundFonts);
            }
        }

        return foundFonts;
    }

    private static void extractFontsFromZip(File zipFile, Map<String, byte[]> foundFonts) {
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.getName().toLowerCase().endsWith(".ttf")) {
                    String rawName = entry.getName();
                    
                    int slashIdx = rawName.lastIndexOf('/');
                    if (slashIdx != -1) {
                        rawName = rawName.substring(slashIdx + 1);
                    }
                    
                    String fontName = rawName.substring(0, rawName.toLowerCase().lastIndexOf(".ttf"));
                    fontName = fontName.substring(0, 1).toUpperCase() + fontName.substring(1);

                    if (!foundFonts.containsKey(fontName)) {
                        try (InputStream is = zip.getInputStream(entry)) {
                            foundFonts.put(fontName, readAllBytes(is));
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static byte[] readAllBytes(InputStream is) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    public static void checkDebugState(boolean debug, String currentFontMode) {
        if (Minecraft.getMinecraft().thePlayer == null) {
            return;
        }

        if (!announcedFontStatus) {
            if (debug) {
                Meowtils.addMessage(statusMessage);
            }
            announcedFontStatus = true;
        }

        if (debug) {
            if (!currentFontMode.equals(lastFontMode)) {
                boolean isLoaded = currentFontMode.equals("Default") || fonts.containsKey(currentFontMode);
                Meowtils.addMessage("[ExtHud Debug] Font mode set to: " + currentFontMode + " (Loaded: " + isLoaded + ")");
                
                if (!currentFontMode.equals("Default")) {
                    Meowtils.addMessage("§aActive Font: " + currentFontMode);
                }
                
                lastFontMode = currentFontMode;
            }
        } else {
            lastFontMode = currentFontMode;
        }
    }
}
