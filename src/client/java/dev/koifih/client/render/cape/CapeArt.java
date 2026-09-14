package dev.koifih.client.render.cape;

import com.mojang.blaze3d.platform.NativeImage;
import dev.koifih.client.render.font.Font;
import dev.koifih.client.render.font.Fonts;
import dev.koifih.client.render.font.GlyphImage;
import dev.koifih.client.ui.Transition.Easing;
import dev.koifih.client.util.Colors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CapeArt {
    public static final int SCALE = 16;
    public static final int BACKGROUND = 0xFF0F0F0F;

    private static final int BAND_BACK = 0xFFA3A3A3;
    private static final int BAND_FRONT = 0xFFEAEAEA;
    private static final int TEXT_COLOR = 0xFFEAEAEA;
    private static final String NAME = "adin";
    private static final String SUFFIX = ".lol";
    private static final int STAR_GLYPH = 1;
    private static final int BAND_BACK_GLYPH = 2;
    private static final int BAND_FRONT_GLYPH = 3;

    private static final float LOGO_SHARE = 0.56f;
    private static final float LOGO_CENTER = 0.36f;
    private static final float TEXT_SHARE = 0.155f;
    private static final float TEXT_GAP = 1.15f;
    private static final float TEXT_RISE = 0.3f;

    private static final float BAND_END = 1.7f;
    private static final float STAR_START = 0.95f;
    private static final float STAR_DURATION = 0.85f;
    private static final float STAR_FADE_RATE = 2.5f;
    private static final float STAR_MIN_SCALE = 0.3f;
    private static final float TEXT_START = 1.6f;
    private static final float TEXT_STAGGER = 0.18f;
    private static final float TEXT_DURATION = 0.45f;
    private static final float FADE_START = 7.65f;
    private static final float FADE_DURATION = 0.35f;
    private static final float LOOP_SECONDS = 8f;
    private static final float STATIC_SECONDS = 4f;
    private static final float HOLD_START = 3.4f;
    private static final float SWEEP_START = (float) Math.toRadians(150);
    private static final float SWEEP_SOFT = (float) Math.toRadians(20);
    private static final int FRAME_RATE = 30;
    private static final long HOLD_KEY = 0xFFFFFEL;
    private static final long STATIC_KEY = 0xFFFFFFL;

    private static NativeImage face;
    private static NativeImage logoAtlas;
    private static NativeImage textAtlas;

    public static float seconds(boolean animated) {
        if (!animated) return STATIC_SECONDS;
        return (System.currentTimeMillis() % (long) (LOOP_SECONDS * 1000)) / 1000f;
    }

    public static long frame(boolean animated, float seconds) {
        if (!animated) return STATIC_KEY;
        return seconds >= HOLD_START && seconds < FADE_START ? HOLD_KEY : (long) (seconds * FRAME_RATE);
    }

    public static NativeImage paint(float seconds, int accent) {
        int width = CapeLayout.faceWidth(SCALE);
        int height = CapeLayout.faceHeight(SCALE);
        if (face == null) {
            logoAtlas = GlyphImage.atlas("/assets/adin/textures/font/logo.png");
            textAtlas = GlyphImage.atlas("/assets/adin/textures/font/comfortaa-bold.png");
            face = new NativeImage(width, height, true);
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) face.setPixel(x, y, BACKGROUND);
        }
        float logoSize = width * LOGO_SHARE;
        float logoX = (width - logoSize) / 2f;
        float logoY = height * LOGO_CENTER - logoSize / 2f;
        paintLogo(seconds, accent, logoX, logoY, logoSize);
        paintText(seconds, accent, width, height * LOGO_CENTER + logoSize / 2f);
        fade(seconds, width, height);
        return face;
    }

    private static void paintLogo(float seconds, int accent, float x, float y, float size) {
        Font logo = Fonts.LOGO;
        GlyphImage.Mask band = sweep(Math.clamp(seconds / BAND_END, 0f, 1f));
        GlyphImage.draw(face, logo, logoAtlas, logo.glyph(BAND_BACK_GLYPH), x, y, size, BAND_BACK, 1f, 1f, band);
        float star = (seconds - STAR_START) / STAR_DURATION;
        if (star > 0) {
            float alpha = Easing.SMOOTHSTEP.at(star * STAR_FADE_RATE);
            float scale = Math.max(STAR_MIN_SCALE, STAR_MIN_SCALE + (1f - STAR_MIN_SCALE) * Easing.EASE_OUT_BACK.at(star));
            GlyphImage.draw(face, logo, logoAtlas, logo.glyph(STAR_GLYPH), x, y, size, accent, alpha, scale, null);
        }
        GlyphImage.draw(face, logo, logoAtlas, logo.glyph(BAND_FRONT_GLYPH), x, y, size, BAND_FRONT, 1f, 1f, band);
    }

    private static void paintText(float seconds, int accent, int width, float logoBottom) {
        Font font = Fonts.COMFORTAA_BOLD;
        float emPx = width * TEXT_SHARE;
        String text = NAME + SUFFIX;
        float penX = (width - GlyphImage.width(font, text, emPx)) / 2f;
        float baseline = logoBottom + emPx * TEXT_GAP;
        int previous = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Font.Glyph glyph = font.glyph(c);
            penX += font.kerning(previous, c) * emPx;
            float progress = (seconds - (TEXT_START + i * TEXT_STAGGER)) / TEXT_DURATION;
            if (progress > 0) {
                float ease = Easing.EASE_OUT_CUBIC.at(progress);
                int color = i >= NAME.length() ? accent : TEXT_COLOR;
                GlyphImage.draw(face, font, textAtlas, glyph, penX, baseline + (1 - ease) * emPx * TEXT_RISE,
                        emPx, color, ease, 1f, null);
            }
            penX += glyph.advance() * emPx;
            previous = c;
        }
    }

    private static void fade(float seconds, int width, int height) {
        float fade = Easing.SMOOTHSTEP.at((seconds - FADE_START) / FADE_DURATION);
        if (fade <= 0) return;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                face.setPixel(x, y, Colors.lerp(face.getPixel(x, y), BACKGROUND, fade));
            }
        }
    }

    private static GlyphImage.Mask sweep(float progress) {
        float cut = progress * (float) (2 * Math.PI + SWEEP_SOFT);
        return (x, y) -> {
            float angle = (float) Math.atan2(y - 0.5f, x - 0.5f) - SWEEP_START;
            while (angle < 0) angle += (float) (2 * Math.PI);
            return Easing.SMOOTHSTEP.at((cut - angle) / SWEEP_SOFT);
        };
    }
}
