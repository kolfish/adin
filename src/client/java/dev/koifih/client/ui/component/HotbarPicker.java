package dev.koifih.client.ui.component;

import dev.koifih.client.render.Text;
import dev.koifih.client.setting.HotbarSetting;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

public final class HotbarPicker extends Control {
    private static final float GAP = 2f;
    private static final float RADIUS = 3f;
    private static final float TEXT_SIZE = 6.5f;
    private static final float ITEM_FRACTION = 0.75f;
    private static final float ITEM_PIXELS = 16f;
    private static final float HOVER_BLEND = 0.08f;

    private final IntPredicate has;
    private final IntConsumer toggle;

    public HotbarPicker(int x, int y, int width, int height, float scale, Component label,
                        IntPredicate has, IntConsumer toggle) {
        super(x, y, width, height, scale, label);
        this.has = has;
        this.toggle = toggle;
    }

    public static int preferredWidth(int boxSize, float scale) {
        return Math.round(HotbarSetting.SLOTS * boxSize + (HotbarSetting.SLOTS - 1) * GAP * scale);
    }

    private float boxSize() {
        return (getWidth() - (HotbarSetting.SLOTS - 1) * px(GAP)) / HotbarSetting.SLOTS;
    }

    private float boxX(int slot) {
        return getX() + slot * (boxSize() + px(GAP));
    }

    private int slotAt(double pointX, double pointY) {
        if (pointY < getY() || pointY >= getY() + getHeight()) return -1;
        float stride = boxSize() + px(GAP);
        float offset = (float) (pointX - getX());
        int slot = (int) Math.floor(offset / stride);
        if (slot < 0 || slot >= HotbarSetting.SLOTS || offset - slot * stride >= boxSize()) return -1;
        return slot;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        LocalPlayer player = Minecraft.getInstance().player;
        int hovered = isHovered() ? slotAt(mouseX, mouseY) : -1;
        float box = boxSize();
        for (int slot = 0; slot < HotbarSetting.SLOTS; slot++) {
            boolean selected = has.test(slot);
            int fill = selected ? Theme.CONTROL_ACTIVE : Theme.CONTROL;
            if (slot == hovered) fill = Colors.lerp(fill, Theme.TEXT, HOVER_BLEND);
            float x = boxX(slot);
            rect(graphics, x, getY(), box, box, px(RADIUS), fill);
            ItemStack stack = player == null ? ItemStack.EMPTY : player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                Text.drawCenteredX(graphics, String.valueOf(slot + 1), x + box * 0.5f, getY() + box * 0.5f,
                        px(TEXT_SIZE), selected ? Theme.TEXT : Theme.DIM);
            } else {
                item(graphics, stack, x, box);
            }
        }
    }

    private void item(GuiGraphicsExtractor graphics, ItemStack stack, float x, float box) {
        float size = box * ITEM_FRACTION;
        float factor = size / ITEM_PIXELS;
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(x + (box - size) * 0.5f, getY() + (box - size) * 0.5f).scale(factor, factor);
            graphics.item(stack, 0, 0);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int slot = slotAt(event.x(), event.y());
        if (slot >= 0) toggle.accept(slot);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        StringBuilder chosen = new StringBuilder();
        for (int slot = 0; slot < HotbarSetting.SLOTS; slot++) {
            if (!has.test(slot)) continue;
            if (!chosen.isEmpty()) chosen.append(", ");
            chosen.append(slot + 1);
        }
        output.add(NarratedElementType.TITLE, Component.literal(getMessage().getString() + ": " + chosen));
    }
}
