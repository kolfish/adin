package dev.koifih.client.ui.clickgui;

import dev.koifih.client.config.Config;
import dev.koifih.client.config.ConfigStore;
import dev.koifih.client.render.Draw;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.Text;
import dev.koifih.client.render.Transform;
import dev.koifih.client.ui.Theme;
import dev.koifih.client.ui.Transition;
import dev.koifih.client.ui.component.Button;
import dev.koifih.client.ui.component.Control;
import dev.koifih.client.ui.component.Segmented;
import dev.koifih.client.ui.component.TextInput;
import dev.koifih.client.util.Lang;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class ConfigsPage implements Page {
    private record Area(float scale, int x, int y, int width, int height, int padding) {
        int scaled(float units) {
            return Math.round(units * scale);
        }

        int rowX() {
            return x + padding;
        }

        int rowWidth() {
            return width - 2 * padding;
        }
    }

    private static final int CARD_HEIGHT = 40;
    private static final int CARD_STRIDE = 46;
    private static final int GAP = 6;
    private static final int DIALOG_INSET = 14;
    private static final int DIALOG_PADDING = 10;
    private static final int DIALOG_TITLE = 16;
    private static final int DIALOG_ROW = 22;
    private static final int DELETE_ICON = 0xe872;
    private static final int ADD_ICON = 0xe145;
    private static final int ADD_HEIGHT = 22;

    private final ClickGui gui;
    private final Transition dialogReveal = new Transition(0f, 150);
    private final Transition listReveal = new Transition(0f, 200);
    private final List<Control> cardControls = new ArrayList<>();
    private final List<Control> createControls = new ArrayList<>();
    private List<Config> configs = new ArrayList<>();
    private Area area;
    private boolean shown;
    private boolean interactive;
    private Button makeButton;
    private Button addButton;
    private TextInput nameInput;
    private TextInput descriptionInput;
    private Segmented scopeInput;
    private Button createButton;
    private String name = "";
    private String description = "";
    private int scope = Config.Scope.BOTH.ordinal();
    private boolean dialogOpen;

    private <T extends AbstractWidget> T add(T widget) {
        return gui.add(widget);
    }

    @Override
    public void relayout(PanelLayout layout) {
        this.area = new Area(layout.scale(), layout.contentX(), layout.contentY(),
                layout.contentWidth(), layout.contentHeight(), layout.padding());
    }

    @Override
    public void init(PanelLayout layout) {
        relayout(layout);
        configs = ConfigStore.list();
        dialogOpen = false;
        dialogReveal.snap(0f);
        listReveal.snap(0f);
        listReveal.set(1f);
        cardControls.clear();
        createControls.clear();
        float scale = area.scale();
        int buttonHeight = Math.max(1, area.scaled(18));

        String makeLabel = Lang.get("configs.make");
        int makeWidth = Button.preferredWidth(makeLabel, scale);
        makeButton = add(new Button(area.x() + (area.width() - makeWidth) / 2, area.y() + area.height() / 2 + area.scaled(8),
                makeWidth, buttonHeight, scale, Component.literal(makeLabel), this::openCreate));

        String loadLabel = Lang.get("configs.load");
        int loadWidth = Button.preferredWidth(loadLabel, scale);
        int deleteSize = Math.max(1, area.scaled(16));
        int cardHeight = Math.max(1, area.scaled(CARD_HEIGHT));
        for (int i = 0; i < visibleCount(); i++) {
            Config config = configs.get(i);
            int cardY = cardY(i);
            int deleteX = area.rowX() + area.rowWidth() - area.padding() - deleteSize;
            int loadX = deleteX - area.scaled(GAP) - loadWidth;
            cardControls.add(add(new Button(loadX, cardY + (cardHeight - buttonHeight) / 2, loadWidth, buttonHeight, scale,
                    Component.literal(loadLabel), () -> load(config))));
            cardControls.add(add(Button.icon(deleteX, cardY + (cardHeight - deleteSize) / 2, deleteSize, scale, DELETE_ICON,
                    Component.literal(Lang.get("configs.delete")), () -> delete(config))));
        }
        addButton = add(Button.tile(area.rowX(), cardY(visibleCount()), area.rowWidth(), Math.max(1, area.scaled(ADD_HEIGHT)), scale,
                Component.literal(makeLabel), ADD_ICON, this::openCreate));

        int rowHeight = Math.max(1, area.scaled(DIALOG_ROW));
        int inputHeight = Math.max(1, area.scaled(16));
        int dialogX = createDialogX();
        int dialogWidth = createDialogWidth();
        int dialogY = createDialogY();
        int controlX = dialogX + dialogWidth / 2;
        int controlWidth = dialogX + dialogWidth - area.scaled(DIALOG_PADDING) - controlX;
        nameInput = add(new TextInput(controlX, dialogRowY(dialogY, 0) + (rowHeight - inputHeight) / 2, controlWidth, inputHeight, scale,
                Component.literal(Lang.get("configs.name")), 24, () -> name, value -> name = value));
        nameInput.setPlaceholder(Lang.get("configs.name"));
        descriptionInput = add(new TextInput(controlX, dialogRowY(dialogY, 1) + (rowHeight - inputHeight) / 2, controlWidth, inputHeight, scale,
                Component.literal(Lang.get("configs.description")), 48, () -> description, value -> description = value));
        descriptionInput.setPlaceholder(Lang.get("configs.description"));
        Segmented.Segment[] scopes = {Segmented.Segment.of(Lang.get("configs.scope.colors")), Segmented.Segment.of(Lang.get("configs.scope.settings")), Segmented.Segment.of(Lang.get("configs.scope.both"))};
        int labelWidth = (int) Math.ceil(Text.width(Lang.get("configs.include"), 7 * scale));
        int scopeRight = dialogX + dialogWidth - area.scaled(DIALOG_PADDING);
        int scopeMax = scopeRight - (dialogX + area.scaled(DIALOG_PADDING) + labelWidth + area.scaled(GAP));
        int scopeWidth = Math.min(scopeMax, Segmented.preferredWidth(scopes, scale));
        scopeInput = add(new Segmented(scopeRight - scopeWidth, dialogRowY(dialogY, 2) + (rowHeight - inputHeight) / 2, scopeWidth, inputHeight, scale,
                Component.literal(Lang.get("configs.include")), scopes, () -> scope, value -> scope = value));
        String createLabel = Lang.get("configs.create");
        int createWidth = Button.preferredWidth(createLabel, scale);
        createButton = add(new Button(dialogX + dialogWidth - area.scaled(DIALOG_PADDING) - createWidth,
                dialogRowY(dialogY, 3) + (rowHeight - buttonHeight) / 2, createWidth, buttonHeight, scale,
                Component.literal(createLabel), this::create));
        createControls.addAll(List.of(nameInput, descriptionInput, scopeInput, createButton));

        updateInputStates();
    }

    private int visibleCount() {
        int fit = Math.max(0, (area.height() - 2 * area.padding() - area.scaled(ADD_HEIGHT)) / Math.max(1, area.scaled(CARD_STRIDE)));
        return Math.min(configs.size(), fit);
    }

    private int cardY(int index) {
        return area.y() + area.padding() + area.scaled(index * CARD_STRIDE);
    }

    private int createDialogX() {
        return area.x() + area.scaled(DIALOG_INSET);
    }

    private int createDialogWidth() {
        return area.width() - 2 * area.scaled(DIALOG_INSET);
    }

    private int createDialogHeight() {
        return area.scaled(2 * DIALOG_PADDING + DIALOG_TITLE + 4 * DIALOG_ROW);
    }

    private int createDialogY() {
        return area.y() + (area.height() - createDialogHeight()) / 2;
    }

    private int dialogRowY(int dialogY, int row) {
        return dialogY + area.scaled(DIALOG_PADDING + DIALOG_TITLE) + area.scaled(row * DIALOG_ROW);
    }

    private boolean insideDialog(double pointX, double pointY) {
        return pointX >= createDialogX() && pointX < createDialogX() + createDialogWidth()
                && pointY >= createDialogY() && pointY < createDialogY() + createDialogHeight();
    }

    private void openCreate() {
        name = "";
        description = "";
        scope = Config.Scope.BOTH.ordinal();
        dialogOpen = true;
        dialogReveal.set(1f);
        updateInputStates();
    }

    public boolean closeDialog() {
        if (!dialogOpen) return false;
        for (Control control : createControls) control.dismiss();
        dialogOpen = false;
        dialogReveal.set(0f);
        updateInputStates();
        return true;
    }

    private void create() {
        if (name.isBlank()) return;
        ConfigStore.create(name, description, Config.Scope.values()[scope]);
        closeDialog();
        gui.requestRebuild();
    }

    private void load(Config config) {
        ConfigStore.apply(config);
        gui.requestRebuild();
    }

    private void delete(Config config) {
        ConfigStore.delete(config);
        gui.requestRebuild();
    }

    @Override
    public void reset() {
        closeDialog();
    }

    @Override
    public void setState(boolean shown, boolean interactive) {
        this.shown = shown;
        this.interactive = interactive;
        updateInputStates();
    }

    private void updateInputStates() {
        float reveal = dialogReveal.value();
        boolean dialogHidden = !dialogOpen && reveal == 0f;
        boolean pageReady = shown && interactive && dialogHidden;
        makeButton.visible = shown && configs.isEmpty();
        makeButton.active = pageReady && configs.isEmpty();
        addButton.visible = shown && !configs.isEmpty();
        addButton.active = pageReady && !configs.isEmpty();
        for (Control control : cardControls) {
            control.visible = shown;
            control.active = pageReady;
        }
        boolean dialogReady = shown && interactive && dialogOpen && reveal == 1f;
        for (Control control : createControls) {
            control.visible = shown && reveal > 0f;
            control.active = dialogReady;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!shown || !interactive || !dialogOpen) return false;
        if (insideDialog(event.x(), event.y())) return false;
        closeDialog();
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return shown && event.key() == GLFW.GLFW_KEY_ESCAPE && closeDialog();
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        updateInputStates();
        float reveal = dialogReveal.value();
        Opacity.with(1f - 0.6f * reveal, () -> {
            if (configs.isEmpty()) drawEmpty(graphics, mouseX, mouseY, delta);
            else drawCards(graphics, mouseX, mouseY, delta);
        });
        if (reveal > 0f) drawDialog(graphics, reveal, mouseX, mouseY, delta);
    }

    private void drawEmpty(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float scale = area.scale();
        float centerX = area.x() + area.width() * 0.5f;
        float centerY = area.y() + area.height() * 0.5f;
        String[] lines = {Lang.get("configs.empty.1"), Lang.get("configs.empty.2")};
        for (int i = 0; i < lines.length; i++) {
            float size = 7.5f * scale;
            Text.drawCenteredX(graphics, lines[i], centerX,
                    centerY - (26 - i * 12) * scale, size, i == 0 ? Theme.TEXT : Theme.MUTED);
        }
        makeButton.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private static String scopeText(Config config) {
        return switch (config.scope) {
            case COLORS -> Lang.get("configs.scope.colors");
            case SETTINGS -> Lang.get("configs.scope.settings");
            case BOTH -> Lang.get("configs.scope.both");
        };
    }

    private void drawCards(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float reveal = listReveal.value();
        Transform.translated(graphics, 0f, 6 * area.scale() * (1f - reveal),
                () -> Opacity.with(reveal, () -> drawCardList(graphics, mouseX, mouseY, delta)));
    }

    private void drawCardList(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float scale = area.scale();
        int cardHeight = Math.max(1, area.scaled(CARD_HEIGHT));
        int radius = Math.max(1, area.scaled(6));
        for (int i = 0; i < visibleCount(); i++) {
            Config config = configs.get(i);
            int y = cardY(i);
            Draw.rect(graphics, area.rowX(), y, area.rowWidth(), cardHeight, radius, Theme.ROW);
            Control loadButton = cardControls.get(i * 2);
            float textX = area.rowX() + 10 * scale;
            float textWidth = loadButton.getX() - GAP * scale - textX;
            String title = Text.fit(config.name, textWidth, 8.5f * scale);
            String author = config.author == null ? "" : config.author;
            String details = (author.isBlank() ? "" : author + " · ") + scopeText(config);
            String subtitle = Text.fit(config.description.isBlank() ? details : config.description + " · " + details,
                    textWidth, 6.8f * scale);
            Text.drawCentered(graphics, title, textX, y + 13 * scale, 8.5f * scale, Theme.TEXT);
            Text.drawCentered(graphics, subtitle, textX, y + 27 * scale, 6.8f * scale, Theme.MUTED);
        }
        for (Control control : cardControls) control.extractRenderState(graphics, mouseX, mouseY, delta);
        addButton.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawDialog(GuiGraphicsExtractor graphics, float reveal, int mouseX, int mouseY, float delta) {
        graphics.nextStratum();
        int x = createDialogX();
        int y = createDialogY();
        int width = createDialogWidth();
        int height = createDialogHeight();
        Transform.popIn(graphics, x + width * 0.5f, y + height * 0.5f, reveal, 0.96f, () -> {
            float scale = area.scale();
            int radius = Math.max(1, area.scaled(6));
            Draw.bordered(graphics, x, y, width, height, radius, Theme.OVERLAY, Theme.POPUP_BORDER);
            float textX = x + DIALOG_PADDING * scale;
            float titleY = y + (DIALOG_PADDING + DIALOG_TITLE * 0.5f) * scale;
            Text.drawCentered(graphics, Lang.get("configs.new"), textX, titleY, 8 * scale, Theme.TEXT);
            String[] labels = {Lang.get("configs.name"), Lang.get("configs.description"), Lang.get("configs.include")};
            for (int row = 0; row < labels.length; row++) {
                Text.drawCentered(graphics, labels[row], textX,
                        dialogRowY(y, row) + DIALOG_ROW * 0.5f * scale, 7 * scale, Theme.TEXT);
            }
            for (Control control : createControls) control.extractRenderState(graphics, mouseX, mouseY, delta);
        });
    }
}
