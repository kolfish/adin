# Adin

A Fabric client for Minecraft 26.2 on Java 25.

## Setup

See the [Fabric documentation](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up)
for IDE setup. Run `./gradlew runClient`, enter a world, and press **Right Shift** to open the ClickGUI.
Press the bind again or **Escape** to close it. Rebind it under
**Options → Controls → Key Binds → Adin → Open ClickGUI**.

## Layout

```
dev.koifih
├── Adin                         main entrypoint, mod id, Identifier helper
└── client
    ├── AdinClient               client entrypoint
    ├── keybind/Keybinds         key mapping registration and ClickGUI activation
    ├── event/                   EventBus with typed Listener subscriptions; TickEvent
    ├── feature/                 Feature base (enabled, keybind, hold, settings), Category,
    │   │                        Features registry and config snapshot/apply
    │   ├── setting/             Setting types: Bool, Slider, Range, Enum, Multi, Color, Entity, Block
    │   └── movement/Sprint      auto sprint with an omni-sprint setting
    ├── config/                  Config model and ConfigStore (load, save, apply)
    ├── mixin/                   accessors for GuiGraphicsExtractor, KeyMapping, FallingBlockEntity,
    │                            and the ClientInput hook for omni sprint
    ├── gui
    │   ├── Theme                palette, light/dark modes and the accent, blended per frame
    │   ├── Lang                 translation lookup
    │   ├── animation/           Easing curves and Transition
    │   ├── catalog/             Catalog interface with the entity and block catalogs
    │   ├── component/           Control and Clickable bases plus every widget
    │   └── clickgui
    │       ├── ClickGui         the screen: panel chrome, drag, open/close, input routing
    │       ├── PanelLayout      panel geometry, shared by everything inside it
    │       ├── WidgetHost       how sections register widgets and ask for a rebuild
    │       ├── Sidebar          brand, category buttons, selection pill and the gear
    │       ├── SettingsMenu     the client settings popup
    │       ├── SelectionPill    the sliding sidebar pill
    │       └── page/            Page interface with ModulesPage and ConfigsPage
    ├── utils/Colors             lerp, hex, opaque, alpha and darken helpers
    └── rendering
        ├── Pipelines            every RenderPipeline and vertex format
        ├── Scissor              clip propagation into custom render states
        ├── Opacity              nested alpha for fades
        ├── Draw                 pose helpers (translated, scaled, rotated, popIn) and bordered boxes
        ├── GuiRenderQueue       submits render states to the GUI renderer
        ├── RectRenderer         rounded rectangles, hue bar and saturation square
        ├── TextRenderer         Comfortaa Bold MSDF text
        ├── IconRenderer         Material Icons MSDF glyphs
        ├── Previews             spinning entity and block models
        ├── font/                MsdfFont loader and the Fonts registry
        └── state/               RectRenderState and TextRenderState
```

Shaders live in `assets/adin/shaders/core/`: `rect.{vsh,fsh}` draws the
signed-distance rounded rectangles, `hue_bar.fsh` and `saturation_value.fsh` reuse the rect
vertex shader for the color picker, and `text.{vsh,fsh}` draws text and icons. The shared
rounded-rectangle coverage lives in `shaders/include/rect_coverage.glsl`.
Font atlases and their regeneration scripts are documented in
[tools/fonts/README.md](tools/fonts/README.md) and [tools/icons/README.md](tools/icons/README.md).

## ClickGUI

The panel is centered, scales down on small windows, and can be dragged by its top bar.
The sidebar lists the feature categories (Combat, Movement, Render, Player, Misc) and a Configs
tab, with a gear at the bottom that pops up a small settings menu holding a light/dark theme
switch, a language dropdown (English, Russian, Polish, French, Croatian, always listed in
English; strings live in `assets/adin/translations/`), accent presets with a custom picker,
and the ClickGUI keybind.

Each category page lists its features as rows with a master toggle and a gear that opens a
settings box over the rows. The box always starts with the feature's keybind and toggle/hold
mode, then one row per declared setting; the widget is picked from the setting type, so a
feature only declares `add(new BoolSetting(...))` and the GUI does the rest.

Features live in `feature/` and subscribe to the event bus while enabled; subscriptions are
dropped automatically on disable. The only feature so far is Sprint: it sprints whenever you
move forward and are allowed to, and its Omni sprint setting hooks the client input's
forward-impulse check so vanilla accepts sprinting sideways and backwards too.

The Configs page lists saved configs from `config/adin/configs/*.json`. Make config opens a
dialog for a name, description and what to include (colors, settings or both); each card shows the
Minecraft username that made it and its include type under the name, and a + row below the list
starts another. Load applies whatever the config contains. Feature settings are saved by feature
id and setting id, so configs survive features being added or reordered.

Everything animates on a wall-clock `Transition`: the panel fades and scales in and out,
category switches fade the rows in, the settings box and dialogs pop in, dropdowns unfold with a
rotating chevron, slider knobs glide, toggles and labels crossfade their colors, theme and accent
changes blend every color, check marks fade and scale, the accent ring and entity group highlight
slide, the entity list scrolls smoothly and config cards rise in.

Tab and the arrow keys move between widgets, Enter or Space activates, and Escape closes an
open popup or keybind capture before navigating back.

## License

CC0-1.0. See [LICENSE](LICENSE).
