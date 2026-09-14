package dev.koifih.client.render.cape;

import com.mojang.blaze3d.platform.NativeImage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CapeImage {
    private static final int FALLBACK_BACKGROUND = 0xFF0F0F0F;
    private static final int BORDER_SAMPLES = 32;

    public static NativeImage toCape(NativeImage source) {
        int scale = CapeLayout.scaleFor(source);
        int background = borderColor(source);
        NativeImage cape = CapeLayout.canvas(scale, background);
        try (NativeImage face = fit(source, CapeLayout.faceWidth(scale), CapeLayout.faceHeight(scale), background)) {
            CapeLayout.stamp(cape, face, scale);
        }
        return cape;
    }

    private static NativeImage fit(NativeImage source, int faceWidth, int faceHeight, int background) {
        NativeImage face = new NativeImage(faceWidth, faceHeight, true);
        for (int y = 0; y < faceHeight; y++) {
            for (int x = 0; x < faceWidth; x++) face.setPixel(x, y, background);
        }
        float faceAspect = faceWidth / (float) faceHeight;
        float sourceAspect = source.getWidth() / (float) source.getHeight();
        boolean wide = sourceAspect > faceAspect;
        int width = wide ? faceWidth : Math.max(1, Math.round(faceHeight * sourceAspect));
        int height = wide ? Math.max(1, Math.round(faceWidth / sourceAspect)) : faceHeight;
        int offsetX = (faceWidth - width) / 2;
        int offsetY = (faceHeight - height) / 2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float sx = (x + 0.5f) / width * source.getWidth();
                float sy = (y + 0.5f) / height * source.getHeight();
                face.setPixel(offsetX + x, offsetY + y, blend(background, sample(source, sx, sy)));
            }
        }
        return face;
    }

    private static int borderColor(NativeImage image) {
        long r = 0, g = 0, b = 0, count = 0;
        int stepX = Math.max(1, image.getWidth() / BORDER_SAMPLES);
        int stepY = Math.max(1, image.getHeight() / BORDER_SAMPLES);
        for (int x = 0; x < image.getWidth(); x += stepX) {
            for (int y : new int[] {0, image.getHeight() - 1}) {
                int pixel = image.getPixel(x, y);
                r += (pixel >> 16) & 255;
                g += (pixel >> 8) & 255;
                b += pixel & 255;
                count++;
            }
        }
        for (int y = 0; y < image.getHeight(); y += stepY) {
            for (int x : new int[] {0, image.getWidth() - 1}) {
                int pixel = image.getPixel(x, y);
                r += (pixel >> 16) & 255;
                g += (pixel >> 8) & 255;
                b += pixel & 255;
                count++;
            }
        }
        if (count == 0) return FALLBACK_BACKGROUND;
        return 0xFF000000 | (int) (r / count) << 16 | (int) (g / count) << 8 | (int) (b / count);
    }

    private static int sample(NativeImage image, float x, float y) {
        int x0 = Math.clamp((int) Math.floor(x - 0.5f), 0, image.getWidth() - 1);
        int y0 = Math.clamp((int) Math.floor(y - 0.5f), 0, image.getHeight() - 1);
        int x1 = Math.min(x0 + 1, image.getWidth() - 1);
        int y1 = Math.min(y0 + 1, image.getHeight() - 1);
        float fx = Math.clamp(x - 0.5f - x0, 0f, 1f);
        float fy = Math.clamp(y - 0.5f - y0, 0f, 1f);
        int p00 = image.getPixel(x0, y0);
        int p10 = image.getPixel(x1, y0);
        int p01 = image.getPixel(x0, y1);
        int p11 = image.getPixel(x1, y1);
        int color = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            float top = ((p00 >> shift) & 255) * (1 - fx) + ((p10 >> shift) & 255) * fx;
            float bottom = ((p01 >> shift) & 255) * (1 - fx) + ((p11 >> shift) & 255) * fx;
            color |= Math.clamp(Math.round(top * (1 - fy) + bottom * fy), 0, 255) << shift;
        }
        return color;
    }

    private static int blend(int background, int color) {
        float alpha = ((color >>> 24) & 255) / 255f;
        if (alpha >= 1f) return color | 0xFF000000;
        int r = Math.round(((color >> 16) & 255) * alpha + ((background >> 16) & 255) * (1 - alpha));
        int g = Math.round(((color >> 8) & 255) * alpha + ((background >> 8) & 255) * (1 - alpha));
        int b = Math.round((color & 255) * alpha + (background & 255) * (1 - alpha));
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
}
