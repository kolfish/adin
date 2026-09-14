package dev.koifih.client.render.cape;

import com.mojang.blaze3d.platform.NativeImage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CapeLayout {
    public static final int UNITS_WIDTH = 64;
    public static final int UNITS_HEIGHT = 32;
    private static final int FACE_UNITS_X = 1;
    private static final int FACE_UNITS_Y = 1;
    private static final int BACK_UNITS_X = 12;
    private static final int FACE_UNITS_WIDTH = 10;
    private static final int FACE_UNITS_HEIGHT = 16;

    public static final float FACE_U0 = FACE_UNITS_X / (float) UNITS_WIDTH;
    public static final float FACE_V0 = FACE_UNITS_Y / (float) UNITS_HEIGHT;
    public static final float FACE_U_SIZE = FACE_UNITS_WIDTH / (float) UNITS_WIDTH;
    public static final float FACE_V_SIZE = FACE_UNITS_HEIGHT / (float) UNITS_HEIGHT;
    public static final float FACE_U1 = FACE_U0 + FACE_U_SIZE;
    public static final float FACE_V1 = FACE_V0 + FACE_V_SIZE;

    public static boolean isCape(NativeImage image) {
        return image.getWidth() == 2 * image.getHeight() && image.getWidth() % UNITS_WIDTH == 0;
    }

    public static int scaleFor(NativeImage source) {
        float byHeight = source.getHeight() / (float) FACE_UNITS_HEIGHT;
        float byWidth = source.getWidth() / (float) FACE_UNITS_WIDTH;
        return Math.clamp(Math.round(Math.max(byHeight, byWidth)), 1, 32);
    }

    public static int faceWidth(int scale) {
        return FACE_UNITS_WIDTH * scale;
    }

    public static int faceHeight(int scale) {
        return FACE_UNITS_HEIGHT * scale;
    }

    public static NativeImage canvas(int scale, int background) {
        NativeImage cape = new NativeImage(UNITS_WIDTH * scale, UNITS_HEIGHT * scale, true);
        for (int y = 0; y < cape.getHeight(); y++) {
            for (int x = 0; x < cape.getWidth(); x++) cape.setPixel(x, y, background);
        }
        return cape;
    }

    public static void stamp(NativeImage cape, NativeImage face, int scale) {
        int width = faceWidth(scale);
        int height = faceHeight(scale);
        int faceX = FACE_UNITS_X * scale;
        int faceY = FACE_UNITS_Y * scale;
        int backX = BACK_UNITS_X * scale;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = face.getPixel(x, y);
                cape.setPixel(faceX + x, faceY + y, color);
                cape.setPixel(backX + width - 1 - x, faceY + y, color);
            }
        }
    }
}
