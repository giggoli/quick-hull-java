import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HullPanel extends JPanel {
    private static final Color COL_BACKGROUND = new Color(250, 250, 252);
    private static final Color COL_GRID = new Color(220, 220, 225);
    private static final Color COL_BASELINE = new Color(160, 160, 170);
    private static final Color COL_POINT = new Color(30, 30, 30);
    private static final Color COL_EXTREME = new Color(210, 100, 10);
    private static final Color COL_CANDIDATE = new Color(230, 140, 0);
    private static final Color COL_DISCARDED = new Color(180, 180, 185);
    private static final Color COL_FARTHEST = new Color(20, 160, 60);
    private static final Color COL_WORKING_LINE = new Color(40, 90, 210);
    private static final Color COL_HULL_EDGE = new Color(210, 30, 30);
    private static final Color COL_TRIANGLE = new Color(255, 230, 0, 55);

    private static final int MARGIN = 55;   // px around the drawing area
    private static final float LOGICAL_MAX = 10f;  // logical coordinate range

    private List<Point> points = new ArrayList<>();
    private List<Point> fastHull = new ArrayList<>();
    private double area = 0.0;

    private QuickHullAnimator animator = null;
    private javax.swing.Timer animTimer = null;
    private boolean animDone = false;

    public HullPanel() {
        setBackground(COL_BACKGROUND);
    }

    public void generatePoints(int count) {
        stopAnimation();
        points.clear();
        fastHull.clear();
        area = 0.0;
        animDone = false;
        animator = null;

        Random rng = new Random();
        for (int i = 0; i < count; i++) {
            points.add(new Point(0.8 + rng.nextDouble() * 8.4, 0.8 + rng.nextDouble() * 8.4));
        }
        repaint();
    }

    public void clear() {
        stopAnimation();
        fastHull.clear();
        area = 0.0;
        animDone = false;
        animator = null;
        repaint();
    }

    public void startAnimation(int delayMs) {
        if (points.isEmpty()) return;
        stopAnimation();
        fastHull.clear();
        area = 0.0;
        animDone = false;
        animator = new QuickHullAnimator(points);

        animTimer = new javax.swing.Timer(delayMs, e -> {
            boolean finished = animator.nextStep();
            if (finished) {
                stopAnimation();
                animDone = true;
                area = PolygonArea.computeArea(animator.getHullPoints());
            }
            repaint();
        });
        animTimer.start();
        repaint();
    }

    public void computeFast() {
        stopAnimation();
        animator = null;
        animDone = false;
        fastHull = QuickHull.computeHull(points);
        area = PolygonArea.computeArea(fastHull);

        repaint();
    }

    public double getArea() {
        return area;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawGrid(g2);

        if (animator != null) {
            drawAnimationState(g2);
        } else if (!fastHull.isEmpty()) {
            drawFastHull(g2);
        }

        drawAllPoints(g2);
        drawInfoOverlay(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(COL_GRID);
        g2.setStroke(new BasicStroke(0.5f));
        for (int i = 0; i <= 10; i++) {
            g2.drawLine(sx(i), sy(0), sx(i), sy(10));
            g2.drawLine(sx(0), sy(i), sx(10), sy(i));
        }
    }

    private void drawAnimationState(Graphics2D g2) {

        Point pMinX = animator.minX;
        Point pMaxX = animator.maxX;

        if (!animDone && pMinX != null && pMaxX != null) {
            g2.setColor(COL_BASELINE);
            g2.setStroke(dashed(1.5f, 7f, 4f));
            g2.drawLine(sx(pMinX.x), sy(pMinX.y), sx(pMaxX.x), sy(pMaxX.y));
            g2.setStroke(solid(1f));
        }

        Point ca = animator.getCurrentA();
        Point cb = animator.getCurrentB();
        Point cf = animator.getCurrentFarthest();
        if (!animDone && ca != null && cb != null && cf != null) {
            int[] xs = {sx(ca.x), sx(cf.x), sx(cb.x)};
            int[] ys = {sy(ca.y), sy(cf.y), sy(cb.y)};
            g2.setColor(COL_TRIANGLE);
            g2.fillPolygon(xs, ys, 3);
        }

        g2.setColor(COL_DISCARDED);
        for (Point p : animator.getDiscardedPoints()) {
            drawCircle(g2, p, 4);
        }

        g2.setColor(COL_CANDIDATE);
        for (Point p : animator.getCandidatePoints()) {
            if (p != cf) drawCircle(g2, p, 5);
        }

        g2.setColor(COL_HULL_EDGE);
        g2.setStroke(solid(2.5f));
        for (Point[] edge : animator.getHullEdges()) {
            g2.drawLine(sx(edge[0].x), sy(edge[0].y), sx(edge[1].x), sy(edge[1].y));
        }

        if (!animDone && ca != null && cb != null) {
            g2.setColor(COL_WORKING_LINE);
            g2.setStroke(dashed(2f, 9f, 5f));
            g2.drawLine(sx(ca.x), sy(ca.y), sx(cb.x), sy(cb.y));
            g2.setStroke(solid(1f));
        }

        if (pMinX != null) drawLabelledPoint(g2, pMinX, COL_EXTREME, 7, "min");
        if (pMaxX != null) drawLabelledPoint(g2, pMaxX, COL_EXTREME, 7, "max");

        if (!animDone && cf != null) {
            drawLabelledPoint(g2, cf, COL_FARTHEST, 9, "P");
        }

        if (animDone) {
            drawClosedHull(g2, animator.getHullPoints());
        }
    }

    private void drawFastHull(Graphics2D g2) {
        drawClosedHull(g2, fastHull);
    }

    private void drawClosedHull(Graphics2D g2, List<Point> hull) {
        if (hull.size() < 2) return;
        g2.setColor(COL_HULL_EDGE);
        g2.setStroke(solid(2.5f));
        for (int i = 0; i < hull.size(); i++) {
            Point a = hull.get(i);
            Point b = hull.get((i + 1) % hull.size());
            g2.drawLine(sx(a.x), sy(a.y), sx(b.x), sy(b.y));
        }
        g2.setStroke(solid(1f));
    }

    private void drawAllPoints(Graphics2D g2) {
        g2.setColor(COL_POINT);
        for (Point p : points) drawCircle(g2, p, 4);
    }

    private void drawInfoOverlay(Graphics2D g2) {
        int x = 12, y = 22;
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));

        if (animator != null && !animDone) {
            g2.setColor(COL_WORKING_LINE);
            g2.drawString(animator.getPhase(), x, y);
            y += 20;
        } else if (animDone || !fastHull.isEmpty()) {
            g2.setColor(new Color(30, 130, 30));
            g2.drawString("Complete", x, y);
            y += 20;
        }


        if (area > 0) {
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.drawString(String.format("Area (Shoelace): %.4f", area), x, y);
            y += 20;
        }


        g2.setColor(Color.GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("Points: " + points.size(), x, y);

        drawLegend(g2);
    }

    private void drawLegend(Graphics2D g2) {
        int x = 12;
        int y0 = getHeight() - 120;
        int dy = 18;

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));

        Object[][] entries = {{COL_HULL_EDGE, "Finalized hull edge"}, {COL_WORKING_LINE, "Current line a→b"}, {COL_FARTHEST, "Farthest point P"}, {COL_CANDIDATE, "Candidate points"}, {COL_DISCARDED, "Discarded (inside triangle)"}, {COL_EXTREME, "Extreme points (min/max X)"},};

        for (Object[] entry : entries) {
            Color col = (Color) entry[0];
            String label = (String) entry[1];
            g2.setColor(col);
            g2.fillOval(x, y0 - 9, 10, 10);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(label, x + 15, y0);
            y0 += dy;
        }
    }

    private void drawCircle(Graphics2D g2, Point p, int r) {
        g2.fill(new Ellipse2D.Double(sx(p.x) - r, sy(p.y) - r, 2 * r, 2 * r));
    }

    private void drawLabelledPoint(Graphics2D g2, Point p, Color col, int r, String label) {
        g2.setColor(col);
        drawCircle(g2, p, r);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString(label, sx(p.x) + r + 2, sy(p.y) - r);
    }


    private int sx(double x) {
        return (int) (MARGIN + x * (getWidth() - 2.0 * MARGIN) / LOGICAL_MAX);
    }


    private int sy(double y) {
        return (int) (getHeight() - MARGIN - y * (getHeight() - 2.0 * MARGIN) / LOGICAL_MAX);
    }

    private static BasicStroke solid(float w) {
        return new BasicStroke(w);
    }

    private static BasicStroke dashed(float w, float on, float off) {
        return new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{on, off}, 0f);
    }

    private void stopAnimation() {
        if (animTimer != null) {
            animTimer.stop();
            animTimer = null;
        }
    }
}