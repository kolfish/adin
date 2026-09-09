package dev.koifih.client.gui.component;

import dev.koifih.client.gui.Theme;
import dev.koifih.client.render.gui.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TextInput extends Control {
    private static final long BLINK_NANOS = 500_000_000L;

    private final int maxLength;
    private final Supplier<String> get;
    private final Consumer<String> set;
    private int cursor;
    private boolean selectAll;
    private String placeholder = "";

    public TextInput(int x, int y, int width, int height, float scale, Component label,
                     int maxLength, Supplier<String> get, Consumer<String> set) {
        super(x, y, width, height, scale, label);
        this.maxLength = maxLength;
        this.get = get;
        this.set = set;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public boolean capturesInput() {
        return active && visible && isFocused();
    }

    @Override
    public void dismiss() {
        selectAll = false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        String value = get.get();
        float size = px(7);
        float textX = getX() + px(7);
        field(graphics);
        if (value.isEmpty()) text(graphics, fit(placeholder, getWidth() - px(14), size), textX, centerY(), size, Theme.MUTED);
        else text(graphics, fit(value, getWidth() - px(14), size), textX, centerY(), size, selectAll ? Theme.ACCENT : Theme.TEXT);
        boolean blinkVisible = (System.nanoTime() / BLINK_NANOS) % 2 == 0;
        if (isFocused() && blinkVisible) {
            String beforeCursor = value.substring(0, Math.min(cursor, value.length()));
            float cursorX = Math.min(getRight() - px(5), textX + Text.width(beforeCursor, size));
            rect(graphics, cursorX, centerY() - px(4), Math.max(0.5f, scale), px(8), 0, Theme.ACCENT);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        cursor = get.get().length();
        selectAll = false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!active || !isFocused()) return false;
        String value = get.get();
        int key = event.key();
        cursor = Math.clamp(cursor, 0, value.length());
        if (event.isSelectAll()) {
            selectAll = true;
        } else if (event.isCopy()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(value);
        } else if (event.isPaste()) {
            insert(Minecraft.getInstance().keyboardHandler.getClipboard());
        } else if (event.isLeft()) {
            cursor = Math.max(0, cursor - 1);
            selectAll = false;
        } else if (event.isRight()) {
            cursor = Math.min(value.length(), cursor + 1);
            selectAll = false;
        } else if (key == GLFW.GLFW_KEY_HOME) {
            cursor = 0;
        } else if (key == GLFW.GLFW_KEY_END) {
            cursor = value.length();
        } else if (key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE) {
            delete(key == GLFW.GLFW_KEY_BACKSPACE, value);
        } else {
            return super.keyPressed(event);
        }
        return true;
    }

    private void delete(boolean backspace, String value) {
        if (selectAll) {
            set.accept("");
            cursor = 0;
            selectAll = false;
        } else if (backspace && cursor > 0) {
            set.accept(value.substring(0, cursor - 1) + value.substring(cursor));
            cursor--;
        } else if (!backspace && cursor < value.length()) {
            set.accept(value.substring(0, cursor) + value.substring(cursor + 1));
        }
    }

    private void insert(String typed) {
        StringBuilder clean = new StringBuilder();
        typed.codePoints().filter(c -> c >= 32 && c <= 255 && c != 127).forEach(clean::appendCodePoint);
        String value = get.get();
        if (selectAll) {
            value = "";
            cursor = 0;
            selectAll = false;
        }
        cursor = Math.clamp(cursor, 0, value.length());
        String added = clean.substring(0, Math.min(clean.length(), Math.max(0, maxLength - value.length())));
        set.accept(value.substring(0, cursor) + added + value.substring(cursor));
        cursor += added.length();
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!active || !isFocused()) return false;
        insert(event.codepointAsString());
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(getMessage().getString() + ": " + get.get()));
    }
}
