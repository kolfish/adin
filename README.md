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
    ├── event/                   EventBus with typed Listener subscriptions; TickEvent,
    │                            FeatureToggleEvent, plus
    │                            WorldExtractEvent and ScreenExtractEvent, which share the
    │                            LevelFrameEvent entity interpolation helpers
    ├── feature/                 Feature base (enabled, keybind, hold, settings), Category, and
    │   │                        FeatureManager: registration, lookup by id or class, category
    │   │                        and enabled lists, keybind ticking, toggle events, disable-all
    │   │                        and config snapshot/apply
    │   ├── setting/             Setting types: Bool, Slider, Range, Enum, Multi, Color, Entity, Block;
    │   │                        visibleWhen hides a setting until its condition holds and a
    │   │                        Slider's Measure formats its value in the chosen units
    │   ├── movement/Sprint      auto sprint with an omni-sprint setting
    │   └── render/Esp           see-through 3D boxes or 2D screen rectangles around chosen entities
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
    │       ├── PreviewWindow    the side window that previews a feature's look on your player model
    │       ├── SelectionPill    the sliding sidebar pill
    │       └── page/            Page interface with ModulesPage and ConfigsPage
    ├── utils
    │   ├── Colors               lerp, hex, opaque, alpha and darken helpers
    │   └── MathHelper           clamp, lerp, remap, lengths, angles and epsilon comparisons
    └── rendering
        ├── Pipelines            every RenderPipeline and vertex format
        ├── Scissor              clip propagation into custom render states
        ├── Opacity              nested alpha for fades
        ├── Draw                 pose helpers (translated, scaled, rotated, popIn) and bordered boxes
        ├── GuiRenderQueue       submits render states to the GUI renderer
        ├── RectRenderer         rounded rectangles, hue bar and saturation square
        ├── TextRenderer         Comfortaa Bold MSDF text
        ├── IconRenderer         Material Icons MSDF glyphs
        ├── Previews             spinning entity and block models, and the player model with any skin
        ├── SkinLookup           username to skin through Mojang's services, cached per name
        ├── font/                MsdfFont loader and the Fonts registry
        ├── state/               RectRenderState and TextRenderState
        ├── world/               WorldRenderer, ShapeCollector, BoxStyle, BoxGeometry and
        │                        WorldRenderTypes for see-through 3D shapes
        └── screen/              OverlayRenderer, W2S, Projections, OverlayCollector, RectStyle,
                                 HealthBar and the screen-space render states for 2D shapes over
                                 the HUD
```

Shaders live in `assets/adin/shaders/core/`: `rect.{vsh,fsh}` draws the
signed-distance rounded rectangles, `shape.{vsh,fsh}` draws anti-aliased screen-space fills and
border rings for the 2D world overlay, `text.{vsh,fsh}` draws text and icons, and `fill.{vsh,fsh}`
draws the entity chams. The color picker reuses `rect` with a different `RECT_MODE` define.
Screen-space effects live in `shaders/post/`. Shared GLSL goes in `shaders/include/`, which
Minecraft requires: `#moj_import <adin:rect_coverage.glsl>` resolves to
`shaders/include/rect_coverage.glsl`.

MSDF atlases are a `.json` metrics file beside a `.png` of the same name, grouped into
`assets/adin/fonts/`, `assets/adin/icons/` and `assets/adin/brand/`. `Font.load` takes one
name, such as `icons/material`, and derives both paths from it.

## ClickGUI

The panel is centered, scales down on small windows, and can be dragged by its top bar.
The sidebar lists the feature categories (Combat, Movement, Render, Player, Misc) and a Configs
tab, with a gear at the bottom that pops up a small settings menu holding a light/dark theme
switch, a language dropdown (English, Russian, Polish, French, Croatian, always listed in
English; strings live in `assets/adin/translations/`), a size dropdown (100% to 200%, applied
the next time the GUI opens), a units dropdown (metric or imperial, used wherever a slider
shows a distance), accent presets with a custom picker, and the ClickGUI keybind.

Each category page lists its features as rows with a master toggle and a gear that opens a
settings box over the rows. The box always starts with the feature's keybind and toggle/hold
mode, then one row per declared setting; the widget is picked from the setting type, so a
feature only declares `add(new BoolSetting(...))` and the GUI does the rest. A setting can
call `visibleWhen` with a condition and the box hides it and reflows until the condition holds.

Features live in `feature/` and subscribe to the event bus while enabled; subscriptions are
dropped automatically on disable. Sprint sprints whenever you move forward and are allowed
to, and its Omni sprint setting hooks the client input's forward-impulse check so vanilla
accepts sprinting sideways and backwards too. ESP draws boxes through walls around entities within its distance: a mode dropdown (3D boxes in
the world or 2D rectangles on the screen), a 2D-only Type dropdown (Full box or Cornered
brackets), its own color picker, a Show multi-select of Box (2D only), Fill, Outline and Health with
a Fill opacity slider and a 2D-only Health position dropdown appearing beneath it as needed, a
Targets multi-select of Self, Players and Entities (2D player boxes are padded slightly so the arms
and head fit inside), and an entity picker that appears only while Entities is selected. Lines are
2px in 3D (full width within 10 blocks, shrinking with distance so far boxes stay crisp) and a 1px
anti-aliased line with a thin black ring in 2D. The health bar is black-bordered, colored from red
through yellow to green by the target's health, and sits on the left in 3D. A Preview row opens a
second window beside the panel with a back arrow in its top bar, showing a player model with the
box drawn exactly as the ESP would draw it in the current mode; drag to spin the model, click to
step it 45 degrees, or use the arrow keys. A username field at the top loads that player's skin
through Mojang's own profile and skin services (fetched off-thread and cached, with a loading or
not-found note beneath the field); clear it to see your own skin.

## World rendering

`rendering/world` is the 3D counterpart of the GUI renderers. Each frame `WorldRenderer`
posts a `WorldExtractEvent` from Fabric's level extraction hook with the level, camera, delta
tracker and a `ShapeCollector`; features add shapes with a `BoxStyle` (fill, stroke, stroke
width, and an optional wider outline drawn behind the stroke) and the event helps with per-entity partial ticks and interpolated bounding boxes.
During submit collection the renderer turns the collected boxes into camera-relative quads and
lines through `submitCustomGeometry` using two render types built from the vanilla filled-box
and lines pipeline snippets with depth testing disabled, so shapes show through terrain.

## Screen rendering

`rendering/screen` is the 2D counterpart. `OverlayRenderer` registers a HUD element that runs
after the vanilla HUD is extracted, captures a `W2S` (world to screen) for the frame and posts a
`ScreenExtractEvent` with an `OverlayCollector`. `W2S` holds the exact view-projection the level
is drawn with, rebuilt by `Projections` from the camera render state including view bobbing and
hurt tilt, plus the camera origin, the GUI-scaled viewport and the size of a physical pixel;
it projects world points to screen coordinates and reports points behind the near plane.
`Projections.bounds` projects a bounding box to a screen rectangle, clipping each edge against
the near plane so boxes that straddle the camera still get a correct rectangle. Features add
rectangles with a `RectStyle` (same fill, stroke and outline model as `BoxStyle`, widths in
physical pixels) or plain lines, and the renderer submits them to the GUI render state as
`ScreenRectRenderState` and `ScreenLineRenderState` elements. They draw through the `shape`
pipeline, a signed-distance shader (`shape.{vsh,fsh}`, sharing the rect vertex format and
`rect_coverage.glsl`) that anti-aliases fills and border rings, so a box keeps an even edge as it
glides across sub-pixel positions instead of its lines popping between pixel widths.

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
