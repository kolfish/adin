package dev.koifih.client.gui.catalog;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.List;

public interface Catalog {
    interface Group {
        String label();
    }

    record Entry(String id, String name, Object subject) {}

    Group[] groups();

    List<Entry> entries(Group group);

    List<Entry> all();

    void drawPreview(GuiGraphicsExtractor graphics, Entry entry, int x0, int y0, int x1, int y1, float yawDegrees);

    boolean canPreview(Entry entry);
}
