import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public final class LogoAtlas {
    private static final float RANGE = 4f;

    private LogoAtlas() {}

    public static void main(String[] args) throws Exception {
        int cell = Integer.parseInt(args[2]);
        int padding = cell / 8;
        int em = cell - 2 * padding;
        double mark = em / 14.0 * 0.027;
        double cx = padding + em / 2.0;
        double cy = padding + em / 2.0 + 0.4 / 14.0 * em;
        Shape sharp = star(cx, cy, 136 * mark, 58 * mark, -18);
        Area star = new Area(sharp);
        star.add(new Area(stroke(30 * mark).createStrokedShape(sharp)));
        AffineTransform tilt = AffineTransform.getRotateInstance(Math.toRadians(-22), cx, cy);
        Shape orbit = tilt.createTransformedShape(new Arc2D.Double(cx - 215 * mark, cy - 78 * mark, 430 * mark, 156 * mark, 20, 290, Arc2D.OPEN));
        Area lower = new Area(tilt.createTransformedShape(new Rectangle2D.Double(cx - 400 * mark, cy, 800 * mark, 400 * mark)));
        Area band = new Area(stroke(30 * mark).createStrokedShape(orbit));
        Area back = new Area(band);
        back.subtract(lower);
        Area front = new Area(band);
        front.intersect(lower);
        Area cut = new Area(stroke(46 * mark).createStrokedShape(orbit));
        cut.intersect(lower);
        star.subtract(cut);
        Area[] layers = {star, back, front};

        BufferedImage atlas = new BufferedImage(cell * layers.length, cell, BufferedImage.TYPE_INT_ARGB);
        StringBuilder glyphs = new StringBuilder();
        for (int i = 0; i < layers.length; i++) {
            List<double[]> segments = segments(layers[i]);
            for (int y = 0; y < cell; y++) {
                for (int x = 0; x < cell; x++) {
                    double px = x + 0.5;
                    double py = y + 0.5;
                    double distance = distance(segments, px, py);
                    if (!layers[i].contains(px, py)) distance = -distance;
                    int value = (int) Math.round(Math.clamp(0.5 + distance / RANGE, 0.0, 1.0) * 255);
                    atlas.setRGB(i * cell + x, y, 0xFF000000 | (value << 16) | (value << 8) | value);
                }
            }
            double left = -(double) padding / em;
            double right = (double) (cell - padding) / em;
            if (i > 0) glyphs.append(",");
            glyphs.append(String.format(java.util.Locale.ROOT,
                    "{\"unicode\":%d,\"advance\":1,\"planeBounds\":{\"left\":%s,\"top\":%s,\"right\":%s,\"bottom\":%s},"
                            + "\"atlasBounds\":{\"left\":%d,\"top\":0,\"right\":%d,\"bottom\":%d}}",
                    i + 1, left, left, right, right, i * cell, (i + 1) * cell, cell));
        }
        File textures = new File(args[0], "textures/font");
        File metrics = new File(args[0], "msdf");
        ImageIO.write(atlas, "png", new File(textures, args[1] + ".png"));
        String json = String.format(java.util.Locale.ROOT,
                "{\"atlas\":{\"type\":\"msdf\",\"distanceRange\":%s,\"size\":%d,\"width\":%d,\"height\":%d,\"yOrigin\":\"top\"},\"glyphs\":[%s]}",
                RANGE, em, atlas.getWidth(), atlas.getHeight(), glyphs);
        Files.writeString(new File(metrics, args[1] + ".json").toPath(), json);
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

    private static BasicStroke stroke(double width) {
        return new BasicStroke((float) width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private static Path2D star(double cx, double cy, double outer, double inner, double rotation) {
        Path2D.Double path = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double radius = i % 2 == 0 ? outer : inner;
            double angle = Math.toRadians(-90 + i * 36 + rotation);
            double x = cx + Math.cos(angle) * radius;
            double y = cy + Math.sin(angle) * radius;
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.closePath();
        return path;
    }
}
