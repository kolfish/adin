import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

public final class AdinIconAtlas {
    private static final float RANGE = 4f;
    private static final double CANVAS = 500;
    private static final double STROKE = 44;
    private static final double GAP = 12;
    private static final double TILT = -22;
    private static final int ICONS_PER_ROW = 3;

    private AdinIconAtlas() {}

    public static void main(String[] args) throws Exception {
        int cell = Integer.parseInt(args[2]);
        int padding = cell / 8;
        int em = cell - 2 * padding;
        Area[][] icons = {combat(), movement(), render(), hud(), player(), misc(), friends(), configs(),
                settings(), language(), size(), units(), tooltips(), preview(), keybinds(),
                light(), dark(), play(), toggle(), hold(),
                idle(), walk(), sneak(), swim(), fly()};
        int rows = (icons.length + ICONS_PER_ROW - 1) / ICONS_PER_ROW;
        BufferedImage atlas = new BufferedImage(cell * 3 * ICONS_PER_ROW, cell * rows, BufferedImage.TYPE_INT_ARGB);
        List<String> glyphs = new ArrayList<>();
        double plane = (double) padding / em;
        for (int icon = 0; icon < icons.length; icon++) {
            AffineTransform place = place(icons[icon], cell, em);
            int top = icon / ICONS_PER_ROW * cell;
            for (int layer = 0; layer < 3; layer++) {
                int left = (icon % ICONS_PER_ROW * 3 + layer) * cell;
                Area area = icons[icon][layer].createTransformedArea(place);
                paint(atlas, area, left, top, cell);
                glyphs.add(String.format(Locale.ROOT,
                        "{\"unicode\":%d,\"advance\":1,\"planeBounds\":{\"left\":%s,\"top\":%s,\"right\":%s,\"bottom\":%s},"
                                + "\"atlasBounds\":{\"left\":%d,\"top\":%d,\"right\":%d,\"bottom\":%d}}",
                        icon * 3 + layer + 1, -plane, -plane, 1 + plane, 1 + plane,
                        left, top, left + cell, top + cell));
            }
        }
        ImageIO.write(atlas, "png", new File(args[0], "textures/font/" + args[1] + ".png"));
        String json = String.format(Locale.ROOT,
                "{\"atlas\":{\"type\":\"msdf\",\"distanceRange\":%s,\"size\":%d,\"width\":%d,\"height\":%d,\"yOrigin\":\"top\"},\"glyphs\":[%s]}",
                RANGE, em, atlas.getWidth(), atlas.getHeight(), String.join(",", glyphs));
        Files.writeString(new File(args[0], "msdf/" + args[1] + ".json").toPath(), json);
    }

    private static AffineTransform place(Area[] layers, int cell, int em) {
        Rectangle2D bounds = null;
        for (Area layer : layers) {
            Rectangle2D b = layer.getBounds2D();
            if (b.isEmpty()) continue;
            bounds = bounds == null ? b : bounds.createUnion(b);
        }
        double scale = em / CANVAS;
        AffineTransform place = AffineTransform.getTranslateInstance(cell / 2.0, cell / 2.0);
        place.scale(scale, scale);
        place.translate(-bounds.getCenterX(), -bounds.getCenterY());
        return place;
    }

    private static void paint(BufferedImage atlas, Area area, int left, int top, int cell) {
        List<double[]> segments = segments(area);
        for (int y = 0; y < cell; y++) {
            for (int x = 0; x < cell; x++) {
                double px = x + 0.5;
                double py = y + 0.5;
                double distance = distance(segments, px, py);
                if (!area.contains(px, py)) distance = -distance;
                int value = (int) Math.round(Math.clamp(0.5 + distance / RANGE, 0.0, 1.0) * 255);
                atlas.setRGB(left + x, top + y, 0xFF000000 | (value << 16) | (value << 8) | value);
            }
        }
    }

    private static Area[] combat() {
        Path2D ticks = new Path2D.Double();
        for (int i = 0; i < 4; i++) {
            double a = Math.toRadians(i * 90);
            ticks.append(new Line2D.Double(Math.cos(a) * 92, Math.sin(a) * 92, Math.cos(a) * 196, Math.sin(a) * 196), false);
        }
        Area ring = stroked(new Ellipse2D.Double(-128, -128, 256, 256));
        ring.subtract(gap(ticks));
        ring.add(stroked(ticks));
        Area[] parts = split(ring, 0, 0);
        return new Area[] {parts[0], circle(0, 0, 50), parts[1]};
    }

    private static Area[] movement() {
        Area front = stroked(poly(30, -78, -10, 36));
        front.add(stroked(poly(26, -64, 90, -18, 150, -48)));
        front.add(stroked(poly(-10, 36, 72, 80, 58, 176)));
        Area back = stroked(poly(26, -64, -42, -32, -88, -86));
        back.add(stroked(poly(-10, 36, -68, 114, -152, 100)));
        back.subtract(circle(26, -64, STROKE / 2 + GAP));
        back.subtract(circle(-10, 36, STROKE / 2 + GAP));
        return new Area[] {back, circle(66, -150, 48), front};
    }

    private static Area[] render() {
        Path2D almond = new Path2D.Double();
        almond.moveTo(-172, 0);
        almond.quadTo(0, -212, 172, 0);
        almond.quadTo(0, 212, -172, 0);
        almond.closePath();
        Area[] parts = split(stroked(almond), 0, 0);
        Area iris = circle(0, 0, 84);
        iris.intersect(new Area(almond));
        iris.subtract(gap(almond));
        iris.subtract(circle(0, 0, 30));
        return new Area[] {parts[0], iris, parts[1]};
    }

    private static Area[] hud() {
        Area[] parts = split(stroked(new RoundRectangle2D.Double(-196, -150, 392, 300, 96, 96)), 0, 0);
        Path2D bars = new Path2D.Double();
        double[] lengths = {170, 116, 62};
        for (int i = 0; i < lengths.length; i++) {
            double y = -64 + i * 64;
            bars.append(new Line2D.Double(118 - lengths[i], y, 118, y), false);
        }
        return new Area[] {parts[0], stroked(bars), parts[1]};
    }

    private static Area[] player() {
        Area[] parts = split(stroked(new Arc2D.Double(-132, 66, 264, 220, 0, 180, Arc2D.OPEN)), 0, 176);
        return new Area[] {parts[0], circle(0, -52, 80), parts[1]};
    }

    private static Area[] misc() {
        Path2D diamond = poly(111, -192, 192, -111, 111, -30, 30, -111);
        diamond.closePath();
        Area hero = new Area(diamond);
        hero.add(new Area(stroke(36).createStrokedShape(diamond)));
        Area front = stroked(new RoundRectangle2D.Double(-180, 42, 138, 138, 70, 70));
        front.add(stroked(new RoundRectangle2D.Double(42, 42, 138, 138, 70, 70)));
        return new Area[] {stroked(new RoundRectangle2D.Double(-180, -180, 138, 138, 70, 70)), hero, front};
    }

    private static Area[] friends() {
        Shape frontShoulders = new Arc2D.Double(-68, 48, 262, 250, 0, 180, Arc2D.OPEN);
        Area back = circle(-77, -112, 58);
        back.add(stroked(new Arc2D.Double(-190, -6, 226, 220, 0, 180, Arc2D.OPEN)));
        Area occluder = circle(63, -66, 68 + GAP);
        occluder.add(new Area(new Rectangle2D.Double(17, -66, 92, 140)));
        occluder.add(new Area(new Arc2D.Double(-68, 48, 262, 250, 0, 180, Arc2D.PIE)));
        occluder.add(gap(frontShoulders));
        back.subtract(occluder);
        return new Area[] {back, circle(63, -66, 68), stroked(frontShoulders)};
    }

    private static Area[] configs() {
        Path2D backPanel = poly(-186, 150, -186, -150, -74, -150, -40, -116, 186, -116, 186, 150);
        backPanel.closePath();
        Shape frontPanel = new RoundRectangle2D.Double(-196, 6, 392, 144, 64, 64);
        Area cover = new Area(frontPanel);
        cover.add(gap(frontPanel));
        Area back = stroked(backPanel);
        back.subtract(cover);
        Area hero = new Area(new RoundRectangle2D.Double(-122, -80, 244, 150, 36, 36));
        hero.subtract(cover);
        return new Area[] {back, hero, stroked(frontPanel)};
    }

    private static Area[] settings() {
        Path2D gear = new Path2D.Double();
        for (int tooth = 0; tooth < 6; tooth++) {
            double center = -90 + tooth * 60;
            double[][] corners = {{center - 21, 146}, {center - 12, 196}, {center + 12, 196}, {center + 21, 146}};
            for (double[] corner : corners) {
                double a = Math.toRadians(corner[0]);
                if (tooth == 0 && corner == corners[0]) gear.moveTo(Math.cos(a) * corner[1], Math.sin(a) * corner[1]);
                else gear.lineTo(Math.cos(a) * corner[1], Math.sin(a) * corner[1]);
            }
            for (int step = 1; step <= 4; step++) {
                double a = Math.toRadians(center + 21 + 18 * step / 4.0);
                gear.lineTo(Math.cos(a) * 146, Math.sin(a) * 146);
            }
        }
        gear.closePath();
        Area[] parts = split(stroked(gear), 0, 0);
        return new Area[] {parts[0], circle(0, 0, 50), parts[1]};
    }

    private static Area[] language() {
        Shape outline = new Ellipse2D.Double(-190, -190, 380, 380);
        Area[] parts = split(stroked(outline), 0, 0);
        Area hero = stroked(new Ellipse2D.Double(-65, -190, 130, 380));
        hero.add(stroked(poly(-190, 0, 190, 0)));
        hero.intersect(new Area(outline));
        hero.subtract(gap(outline));
        return new Area[] {parts[0], hero, parts[1]};
    }

    private static Area[] size() {
        double cx = -38, cy = -38, r = 132, d = Math.sqrt(0.5);
        Area lens = stroked(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        lens.add(stroked(poly(cx + r * d, cy + r * d, 176, 176)));
        Area[] parts = split(lens, cx, cy);
        Area plus = stroked(poly(cx - 56, cy, cx + 56, cy));
        plus.add(stroked(poly(cx, cy - 56, cx, cy + 56)));
        return new Area[] {parts[0], plus, parts[1]};
    }

    private static Area[] units() {
        Area ruler = stroked(new RoundRectangle2D.Double(-204, -104, 408, 208, 60, 60));
        Path2D ticks = new Path2D.Double();
        double[] xs = {-114, -38, 38, 114};
        double[] ends = {20, -26, 20, -26};
        for (int i = 0; i < xs.length; i++) ticks.append(poly(xs[i], -104, xs[i], ends[i]), false);
        ruler.add(stroked(ticks));
        Area[] parts = split(ruler, 0, 0);
        return new Area[] {parts[0], stroked(poly(-150, 58, 150, 58)), parts[1]};
    }

    private static Area[] tooltips() {
        Area[] parts = split(stroked(new Ellipse2D.Double(-190, -190, 380, 380)), 0, 0);
        Area hero = circle(0, -84, 31);
        hero.add(stroked(poly(0, -6, 0, 86)));
        return new Area[] {parts[0], hero, parts[1]};
    }

    private static Area[] preview() {
        Path2D frame = new Path2D.Double();
        frame.moveTo(-24, -168);
        frame.lineTo(-104, -168);
        frame.quadTo(-168, -168, -168, -104);
        frame.lineTo(-168, 104);
        frame.quadTo(-168, 168, -104, 168);
        frame.lineTo(104, 168);
        frame.quadTo(168, 168, 168, 104);
        frame.lineTo(168, 24);
        Area[] parts = split(stroked(frame), 0, 0);
        Area arrow = stroked(poly(64, -176, 176, -176, 176, -64));
        arrow.add(stroked(poly(170, -170, -16, 16)));
        return new Area[] {parts[0], arrow, parts[1]};
    }

    private static Area[] keybinds() {
        Area[] parts = split(stroked(new RoundRectangle2D.Double(-210, -148, 420, 296, 84, 84)), 0, 0);
        Area keys = new Area();
        for (int row = 0; row < 2; row++) {
            for (int key = 0; key < 4; key++) keys.add(circle(-114 + key * 76, -62 + row * 64, 23));
        }
        Area front = parts[1];
        front.add(stroked(poly(-74, 66, 74, 66)));
        return new Area[] {parts[0], keys, front};
    }

    private static Area[] light() {
        Path2D rays = new Path2D.Double();
        for (int ray = 0; ray < 8; ray++) {
            double a = Math.toRadians(-90 + ray * 45);
            rays.append(poly(Math.cos(a) * 150, Math.sin(a) * 150, Math.cos(a) * 196, Math.sin(a) * 196), false);
        }
        Area[] parts = split(stroked(rays), 0, 0);
        return new Area[] {parts[0], circle(0, 0, 92), parts[1]};
    }

    private static Area[] dark() {
        Area moon = circle(-20, 20, 178);
        moon.subtract(circle(92, -86, 142));
        Area[] parts = split(stroked(moon), -20, 20);
        return new Area[] {parts[0], sparkle(118, -118, 62), parts[1]};
    }

    private static Area sparkle(double cx, double cy, double radius) {
        Path2D star = new Path2D.Double();
        for (int point = 0; point < 8; point++) {
            double r = point % 2 == 0 ? radius : radius * 0.32;
            double a = Math.toRadians(-90 + point * 45);
            if (point == 0) star.moveTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r);
            else star.lineTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r);
        }
        star.closePath();
        Area area = new Area(star);
        area.add(new Area(stroke(22).createStrokedShape(star)));
        return area;
    }

    private static Area[] play() {
        Area[] parts = split(stroked(new Ellipse2D.Double(-190, -190, 380, 380)), 0, 0);
        Path2D triangle = poly(-46, -80, 88, 0, -46, 80);
        triangle.closePath();
        Area hero = new Area(triangle);
        hero.add(new Area(stroke(40).createStrokedShape(triangle)));
        return new Area[] {parts[0], hero, parts[1]};
    }

    private static Area[] toggle() {
        Path2D rightHead = poly(104, -140, 170, -74, 104, -8);
        Path2D leftHead = poly(-104, 8, -170, 74, -104, 140);
        Area top = stroked(poly(-170, -74, 170, -74));
        top.subtract(gap(rightHead));
        Area bottom = stroked(poly(170, 74, -170, 74));
        bottom.subtract(gap(leftHead));
        Area heads = stroked(rightHead);
        heads.add(stroked(leftHead));
        return new Area[] {top, heads, bottom};
    }

    private static Area[] hold() {
        Area[] parts = split(stroked(new RoundRectangle2D.Double(-196, 20, 392, 170, 64, 64)), 0, 105);
        Area press = stroked(poly(-150, -90, -104, -58));
        press.add(stroked(poly(150, -90, 104, -58)));
        Area front = parts[1];
        front.add(stroked(poly(0, -200, 0, -40)));
        return new Area[] {parts[0], press, front};
    }

    private static Area[] idle() {
        Area body = stroked(poly(-96, 40, -60, -60, 60, -60, 96, 40));
        body.add(stroked(poly(0, -60, 0, 52)));
        body.add(stroked(poly(-54, 184, 0, 52, 54, 184)));
        Area[] parts = split(body, 0, 0);
        return new Area[] {parts[0], circle(0, -158, 50), parts[1]};
    }

    private static Area[] walk() {
        Area front = stroked(poly(14, -80, 0, 44));
        front.add(stroked(poly(12, -62, 64, -6, 104, 30)));
        front.add(stroked(poly(0, 44, 46, 108, 76, 178)));
        Area back = stroked(poly(12, -62, -40, -10, -70, 36));
        back.add(stroked(poly(0, 44, -44, 112, -92, 172)));
        back.subtract(joint(12, -62));
        back.subtract(joint(0, 44));
        return new Area[] {back, circle(28, -152, 50), front};
    }

    private static Area[] sneak() {
        Area front = stroked(poly(-84, 64, -34, -50));
        front.add(stroked(poly(-38, -36, 60, -26, 146, -38)));
        front.add(stroked(poly(-84, 64, 70, 58, 48, 184)));
        Area back = stroked(poly(-84, 64, 30, 100, -8, 184));
        back.subtract(joint(-84, 64));
        return new Area[] {back, circle(4, -128, 50), front};
    }

    private static Area[] swim() {
        Area front = stroked(wave(64));
        front.add(stroked(poly(-100, -20, 60, -84, 150, -150)));
        return new Area[] {stroked(wave(170)), circle(-120, -110, 50), front};
    }

    private static Area[] fly() {
        Path2D upper = new Path2D.Double();
        upper.moveTo(60, -20);
        upper.quadTo(-60, -200, -196, -150);
        upper.quadTo(-110, -60, -60, -10);
        Path2D lower = new Path2D.Double();
        lower.moveTo(60, 20);
        lower.quadTo(-60, 200, -196, 150);
        lower.quadTo(-110, 60, -60, 10);
        return new Area[] {stroked(upper), circle(140, 0, 52), stroked(lower)};
    }

    private static Area joint(double x, double y) {
        return circle(x, y, STROKE / 2 + GAP);
    }

    private static Path2D wave(double y) {
        Path2D path = new Path2D.Double();
        path.moveTo(-196, y);
        for (int crest = 0; crest < 4; crest++) {
            double x = -196 + 98 * crest;
            path.quadTo(x + 49, y + (crest % 2 == 0 ? -44 : 44), x + 98, y);
        }
        return path;
    }

    private static Area[] split(Area band, double cx, double cy) {
        AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(TILT), cx, cy);
        Area above = new Area(rotate.createTransformedShape(new Rectangle2D.Double(cx - 2000, cy - 2000, 4000, 2000)));
        Area back = new Area(band);
        back.intersect(above);
        Area front = new Area(band);
        front.subtract(above);
        return new Area[] {back, front};
    }

    private static BasicStroke stroke(double width) {
        return new BasicStroke((float) width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private static Area stroked(Shape shape) {
        return new Area(stroke(STROKE).createStrokedShape(shape));
    }

    private static Area gap(Shape shape) {
        return new Area(stroke(STROKE + 2 * GAP).createStrokedShape(shape));
    }

    private static Area circle(double cx, double cy, double r) {
        return new Area(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
    }

    private static Path2D poly(double... points) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) path.lineTo(points[i], points[i + 1]);
        return path;
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
