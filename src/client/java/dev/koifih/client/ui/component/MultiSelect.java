package dev.koifih.client.gui.component;

import dev.koifih.client.util.Lang;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import java.util.Set;
import java.util.function.Supplier;

public final class MultiSelect extends OptionPopup {
    private final Supplier<Set<Integer>> selected;

    public MultiSelect(int x, int y, int width, int height, float scale, Component label,
                       String[] options, Supplier<Set<Integer>> selected) {
        super(x, y, width, height, scale, label, options);
        this.selected = selected;
    }

    @Override
    protected void onOpen() {
        highlighted = 0;
    }

    @Override
    protected boolean isChosen(int option) {
        return selected.get().contains(option);
    }

    @Override
    protected void choose(int option) {
        Set<Integer> set = selected.get();
        if (!set.remove(option)) set.add(option);
    }

    @Override
    protected String value() {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < options.length; i++) {
            if (!selected.get().contains(i) || !isOptionVisible(i)) continue;
            if (!names.isEmpty()) names.append(", ");
            names.append(options[i]);
        }
        return names.isEmpty() ? Lang.get("none") : names.toString();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(getMessage().getString() + ": " + value()));
        output.add(NarratedElementType.USAGE, Component.literal("Enter opens the list. Arrow keys move, Enter toggles."));
    }
}
