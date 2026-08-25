package meowtils.extension;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtAnimations {
    
    private long lastFrameTime = System.currentTimeMillis();
    private final Map<String, Float> animationProgress = new HashMap<>();

    public List<String> updateAndGetRenderList(List<String> targetLines, boolean animationsEnabled, float animSpeed, Comparator<String> sorter) {
        long currentTime = System.currentTimeMillis();
        
        // prevent huge delta jumps on lag spikes or startup
        if (lastFrameTime == 0 || currentTime < lastFrameTime) {
            lastFrameTime = currentTime;
        }
        float deltaTime = (currentTime - lastFrameTime) / 1000f;
        lastFrameTime = currentTime;

        // keep updating lastframetime even when empty but return an empty list
        if (targetLines.isEmpty() && animationProgress.isEmpty()) {
            return new ArrayList<>();
        }

        // add new target lines to the tracker
        for (String line : targetLines) {
            if (!animationProgress.containsKey(line)) {
                // if animations are off jump instantly to 1.0
                animationProgress.put(line, animationsEnabled ? 0.0f : 1.0f);
            }
        }

        List<String> renderList = new ArrayList<>();
        List<String> toRemove = new ArrayList<>();
        
        // sort all tracked lines maintaining the specified order
        List<String> allLines = new ArrayList<>(animationProgress.keySet());
        allLines.sort(sorter);

        // update animation values
        for (String line : allLines) {
            float currentProgress = animationProgress.get(line);
            boolean isTarget = targetLines.contains(line);

            if (!animationsEnabled) {
                currentProgress = isTarget ? 1.0f : 0.0f;
            } else {
                if (isTarget) {
                    currentProgress = Math.min(1.0f, currentProgress + deltaTime * animSpeed);
                } else {
                    currentProgress = Math.max(0.0f, currentProgress - deltaTime * animSpeed);
                }
            }

            animationProgress.put(line, currentProgress);

            if (currentProgress > 0.0f) {
                renderList.add(line);
            } else if (!isTarget) {
                toRemove.add(line); // dully hidden and no longer active cull it
            }
        }

        for (String rm : toRemove) {
            animationProgress.remove(rm);
        }

        return renderList;
    }

    public float getEaseProgress(String line) {
        float progress = animationProgress.getOrDefault(line, 0.0f);
        return (float) Math.sin(progress * Math.PI / 2);
    }
}