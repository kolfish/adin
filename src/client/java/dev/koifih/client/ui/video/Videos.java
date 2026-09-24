package dev.koifih.client.ui.video;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.koifih.Adin;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Videos {
    public record Clip(String id, String label) {}

    private static final String DIRECTORY = "/assets/adin/video/";
    private static final Map<String, List<Clip>> BY_MODULE = load();

    public static List<Clip> of(String moduleId) {
        return BY_MODULE.getOrDefault(moduleId, List.of());
    }

    public static VideoPlayer open(Clip clip) {
        try (InputStream stream = Videos.class.getResourceAsStream(DIRECTORY + clip.id() + ".avi")) {
            if (stream == null) throw new IllegalStateException("missing " + clip.id() + ".avi");
            return new VideoPlayer(clip.id(), AviVideo.read(stream));
        } catch (Exception exception) {
            Adin.LOGGER.error("Cannot open video {}", clip.id(), exception);
            return null;
        }
    }

    private static Map<String, List<Clip>> load() {
        Map<String, List<Clip>> result = new HashMap<>();
        try (Reader reader = new InputStreamReader(Videos.class.getResourceAsStream(DIRECTORY + "index.json"), StandardCharsets.UTF_8)) {
            JsonObject clips = new Gson().fromJson(reader, JsonObject.class).getAsJsonObject("clips");
            for (String id : clips.keySet()) {
                JsonObject clip = clips.getAsJsonObject(id);
                String label = clip.get("label").isJsonNull() ? null : clip.get("label").getAsString();
                result.computeIfAbsent(clip.get("module").getAsString(), key -> new ArrayList<>()).add(new Clip(id, label));
            }
        } catch (Exception exception) {
            Adin.LOGGER.error("Cannot load module videos", exception);
        }
        return result;
    }
}
