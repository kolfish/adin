package dev.koifih.client.setting;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.koifih.client.render.AdinIcon;
import net.minecraft.resources.Identifier;
import java.util.List;

@Accessors(fluent = true)
public final class EnumSetting extends Setting<Integer> {
    @Getter
    private final String[] options;
    @Getter
    private List<AdinIcon> icons = List.of();
    @Getter
    private List<Identifier> images = List.of();

    public EnumSetting(String id, int defaultIndex, String... options) {
        super(id, defaultIndex);
        this.options = options;
    }

    public EnumSetting withArt(List<AdinIcon> icons, List<Identifier> images) {
        if (icons.size() != options.length || images.size() != options.length) {
            throw new IllegalArgumentException("Every option of " + id() + " needs an icon and an image");
        }
        this.icons = List.copyOf(icons);
        this.images = List.copyOf(images);
        return this;
    }

    public String selected() {
        return options[value];
    }

    @Override
    public void set(Integer value) {
        super.set(Math.clamp(value, 0, options.length - 1));
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(options[value]);
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) return;
        String name = json.getAsString();
        for (int i = 0; i < options.length; i++) if (options[i].equals(name)) set(i);
    }
}
