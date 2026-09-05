package dev.koifih.client.gui.component;

import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class Dropdown extends OptionPopup {
    private final IntSupplier get;
    private final IntConsumer set;

    public Dropdown(int x, int y, int width, int height, float scale, Component label,
                    String[] options, IntSupplier get, IntConsumer set) {
        super(x, y, width, height, scale, label, options);
        this.get = get;
        this.set = set;
    }

    @Override
    protected void onOpen() {
        highlighted = get.getAsInt();
    }

    @Override
    protected boolean isChosen(int option) {
        return get.getAsInt() == option;
    }

    @Override
    protected void choose(int option) {
        set.accept(option);
        dismiss();
    }

    @Override
    protected String value() {
        return options[get.getAsInt()];
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(getMessage().getString() + ": " + value()));
        output.add(NarratedElementType.USAGE, Component.literal("Enter opens the list. Arrow keys move, Enter selects."));
    }
}
