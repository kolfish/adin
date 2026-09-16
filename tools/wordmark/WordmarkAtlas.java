import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

public final class WordmarkAtlas {
    private static final float RANGE = 4f;
    private static final double EM = 1000;
    private static final String WORD = "adın";
    private static final double TILT = Math.toRadians(-22);
    private static final double STAR_RADIUS = 0.15;
    private static final double STAR_HEIGHT = 0.84;
    private static final double STAR_INNER = 0.43;
    private static final double STAR_ROUNDING = 0.22;
    private static final double STAR_ROTATION = -18;

    private WordmarkAtlas() {}

    public static void main(String[] args) throws Exception {
        int cellHeight = Integer.parseInt(args[2]);
        Area[] layers = layers(new File(args[0], "font/comfortaa-bold.ttf"));
        Rectangle2D ink = null;
        for (Area layer : layers) ink = ink == null ? layer.getBounds2D() : ink.createUnion(layer.getBounds2D());
        int padding = cellHeight / 8;
        int em = cellHeight - 2 * padding;
        double scale = em / ink.getHeight();
        int cellWidth = (int) Math.ceil(ink.getWidth() * scale) + 2 * padding;
        AffineTransform place = AffineTransform.getTranslateInstance(padding, padding);
        place.scale(scale, scale);
        place.translate(-ink.getMinX(), -ink.getMinY());
        BufferedImage atlas = new BufferedImage(cellWidth * layers.length, cellHeight, BufferedImage.TYPE_INT_ARGB);
        List<String> glyphs = new ArrayList<>();
        double left = -(double) padding / em;
        double right = (double) (cellWidth - padding) / em;
        double bottom = (double) (cellHeight - padding) / em;
        for (int i = 0; i < layers.length; i++) {
            paint(atlas, layers[i].createTransformedArea(place), i * cellWidth, cellWidth, cellHeight);
            glyphs.add(String.format(Locale.ROOT,
                    "{\"unicode\":%d,\"advance\":%s,\"planeBounds\":{\"left\":%s,\"top\":%s,\"right\":%s,\"bottom\":%s},"
                            + "\"atlasBounds\":{\"left\":%d,\"top\":0,\"right\":%d,\"bottom\":%d}}",
                    i + 1, right + left, left, left, right, bottom, i * cellWidth, (i + 1) * cellWidth, cellHeight));
        }
        ImageIO.write(atlas, "png", new File(args[0], "textures/font/" + args[1] + ".png"));
        String json = String.format(Locale.ROOT,
                "{\"atlas\":{\"type\":\"msdf\",\"distanceRange\":%s,\"size\":%d,\"width\":%d,\"height\":%d,\"yOrigin\":\"top\"},\"glyphs\":[%s]}",
                RANGE, em, atlas.getWidth(), atlas.getHeight(), String.join(",", glyphs));
        Files.writeString(new File(args[0], "msdf/" + args[1] + ".json").toPath(), json);
    }

    private static Area[] layers(File fontFile) throws Exception {
        Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont((float) EM);
        FontRenderContext context = new FontRenderContext(null, true, true);
        GlyphVector word = font.createGlyphVector(context, WORD);
        Area text = new Area(word.getOutline());
        Rectangle2D dotless = word.getGlyphOutline(2).getBounds2D();
        Rectangle2D capital = font.createGlyphVector(context, "H").getOutline().getBounds2D();
        Rectangle2D bounds = text.getBounds2D();
        double centerX = bounds.getCenterX();
        double centerY = capital.getCenterY();
        Area light = half(centerX, centerY);
        Area back = new Area(text);
        back.subtract(light);
        Area front = new Area(text);
        front.intersect(light);
        return new Area[] {back, star(dotless.getCenterX(), -STAR_HEIGHT * EM, STAR_RADIUS * EM), front};
    }

    private static Area half(double centerX, double centerY) {
        double dx = Math.cos(TILT), dy = Math.sin(TILT), far = 10 * EM;
        double nx = dy, ny = -dx;
        Path2D path = new Path2D.Double();
        path.moveTo(centerX - dx * far, centerY - dy * far);
        path.lineTo(centerX + dx * far, centerY + dy * far);
        path.lineTo(centerX + dx * far + nx * far, centerY + dy * far + ny * far);
        path.lineTo(centerX - dx * far + nx * far, centerY - dy * far + ny * far);
        path.closePath();
        return new Area(path);
    }

    private static Area star(double centerX, double centerY, double radius) {
        Path2D path = new Path2D.Double();
        for (int point = 0; point < 10; point++) {
            double r = point % 2 == 0 ? radius : radius * STAR_INNER;
            double angle = Math.toRadians(-90 + point * 36 + STAR_ROTATION);
            if (point == 0) path.moveTo(centerX + Math.cos(angle) * r, centerY + Math.sin(angle) * r);
            else path.lineTo(centerX + Math.cos(angle) * r, centerY + Math.sin(angle) * r);
        }
        path.closePath();
        Area area = new Area(path);
        area.add(new Area(new BasicStroke((float) (radius * STAR_ROUNDING), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                .createStrokedShape(path)));
        return area;
    }

    private static void paint(BufferedImage atlas, Area area, int left, int width, int height) {
        List<double[]> segments = segments(area);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double px = x + 0.5;
                double py = y + 0.5;
                double distance = distance(segments, px, py);
                if (!area.contains(px, py)) distance = -distance;
                int value = (int) Math.round(Math.clamp(0.5 + distance / RANGE, 0.0, 1.0) * 255);
                atlas.setRGB(left + x, y, 0xFF000000 | (value << 16) | (value << 8) | value);
            }
        }
    }

    private static List<double[]> segments(Shape shape) {
        List<double[]> segments = new ArrayList<>();
        double[] coords = new double[6];
        double startX = 0;
        double startY = 0;
        double lastX = 0;
        double lastY = 0;
        for (PathIterator it = shape.getPathIterator(null, 0.05); !it.isDone(); it.next()) {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO) {
                startX = lastX = coords[0];
                startY = lastY = coords[1];
            } else if (type == PathIterator.SEG_LINETO) {
                segments.add(new double[] {lastX, lastY, coords[0], coords[1]});
                lastX = coords[0];
                lastY = coords[1];
            } else if (type == PathIterator.SEG_CLOSE) {
                segments.add(new double[] {lastX, lastY, startX, startY});
                lastX = startX;
                lastY = startY;
            }
        }
        return segments;
    }

    private static double distance(List<double[]> segments, double px, double py) {
        double best = Double.MAX_VALUE;
        for (double[] s : segments) {
            double dx = s[2] - s[0];
            double dy = s[3] - s[1];
            double length = dx * dx + dy * dy;
            double t = length == 0 ? 0 : Math.clamp(((px - s[0]) * dx + (py - s[1]) * dy) / length, 0.0, 1.0);
            double ex = s[0] + t * dx - px;
            double ey = s[1] + t * dy - py;
            best = Math.min(best, ex * ex + ey * ey);
        }
        return Math.sqrt(best);
    }
}
