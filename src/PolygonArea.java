import java.util.*;

public class PolygonArea {

    private PolygonArea() {
    }

    public static double computeArea(List<Point> input) {
        if (input == null)
            throw new IllegalArgumentException("Point list is null");

        if (input.size() < 3)
            return 0.0;

        List<Point> points = new ArrayList<>(input);

        // Mittelpunkt berechnen
        Point centroid = computeCentroid(points);

        // Nach Polarwinkel sortieren
        points.sort(Comparator.comparingDouble(
                p -> Math.atan2(p.y - centroid.y, p.x - centroid.x)
        ));

        // Shoelace anwenden
        double signedArea = signedShoelace(points);

        // Falls Reihenfolge CW -> positiv machen
        return Math.abs(signedArea);
    }

    private static Point computeCentroid(List<Point> points) {
        double sumX = 0, sumY = 0;
        for (Point p : points) {
            sumX += p.x;
            sumY += p.y;
        }
        return new Point(sumX / points.size(), sumY / points.size());
    }

    private static double signedShoelace(List<Point> polygon) {
        double sum = 0.0;
        int n = polygon.size();

        for (int i = 0; i < n; i++) {
            Point a = polygon.get(i);
            Point b = polygon.get((i + 1) % n);
            sum += (a.x * b.y) - (b.x * a.y);
        }
        return sum / 2.0;
    }

}