package dev.koifih.client.ui.component;

import com.mojang.blaze3d.platform.InputConstants;
import dev.koifih.client.render.Text;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.animation.Easing;
import dev.koifih.client.ui.animation.Transition;
import dev.koifih.client.util.Colors;
import dev.koifih.client.util.Lang;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Keybind extends Control {
    private final Supplier<InputConstants.Key> get;
    private final Consumer<InputConstants.Key> set;
    private boolean capturing;
    private boolean clearable = true;
    private final Transition listening = new Transition(0f, 120, Easing.EASE_OUT_CUBIC);

    public Keybind(int x, int y, int width, int height, float scale, Component label,
                   Supplier<InputConstants.Key> get, Consumer<InputConstants.Key> set) {
        super(x, y, width, height, scale, label);
        this.get = get;
        this.set = set;
    }

    public void setClearable(boolean clearable) {
        this.clearable = clearable;
    }

    @Override
    public boolean capturesInput() {
        return active && visible && capturing;
    }

    @Override
    public void dismiss() {
        capturing = false;
    }

    public boolean captureMouse(MouseButtonEvent event) {
        if (!capturesInput()) return false;
        set.accept(InputConstants.Type.MOUSE.getOrCreate(event.button()));
        capturing = false;
        return true;
    }

    private String display() {
        if (capturing) {
            int dots = 1 + (int) ((System.nanoTime() / 350_000_000L) % 3);
            return ".".repeat(dots);
        }
        InputConstants.Key key = get.get();
        return key == InputConstants.UNKNOWN ? Lang.get("none") : key.getDisplayName().getString();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        listening.set(capturing ? 1f : 0f);
        float amount = listening.value();
        rect(graphics, getX(), getY(), getWidth(), getHeight(), px(3),
                Colors.lerp(Theme.CONTROL, Theme.CONTROL_ACTIVE, amount));
        float size = px(7);
        String label = display();
        float labelWidth = Text.width(label, size);
        float available = getWidth() - px(8);
        int color = Colors.lerp(Theme.TEXT, Theme.ACCENT, amount);
        if (labelWidth <= available) {
            text(graphics, label, getX() + (getWidth() - labelWidth) / 2, centerY(), size, color);
        } else {
            float edge = getRight() - px(4);
            Text.drawFaded(graphics, label, getX() + px(4), Text.centeredBaseline(label, size, centerY()),
                    size, color, edge, edge - px(12));
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        capturing = true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!active || !isFocused()) return false;
        if (capturing) {
            int key = event.key();
            boolean clear = key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE;
            if (!clear) set.accept(InputConstants.Type.KEYSYM.getOrCreate(key));
            else if (clearable) set.accept(InputConstants.UNKNOWN);
            capturing = false;
            return true;
        }
        if (event.isSelection()) {
            capturing = true;
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(getMessage().getString() + ": " + display()));
        output.add(NarratedElementType.USAGE, Component.literal(
                "Enter starts capture. Escape or Backspace clears the binding."));
    }
}
