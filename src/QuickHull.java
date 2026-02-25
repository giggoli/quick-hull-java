import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QuickHull {
    public static List<Point> computeHull(List<Point> points) {
        if (points.size() < 3) return new ArrayList<>(points);

        List<Point> hull = new ArrayList<>();

        // Step 1: find the extreme points on the x-axis
        Point minX = findMinX(points);
        Point maxX = findMaxX(points);

        // Step 2: process upper half (points left of minX→maxX)
        //         and lower half (points left of maxX→minX)
        findHull(points, minX, maxX, hull);  // upper hull
        findHull(points, maxX, minX, hull);  // lower hull

        // Step 3: sort counter-clockwise so the Shoelace formula works correctly
        sortCounterClockwise(hull);
        return hull;
    }


    private static void findHull(List<Point> points, Point a, Point b, List<Point> hull) {

        // Collect only the points that lie strictly to the LEFT of a→b
        List<Point> leftOfAB = new ArrayList<>();
        Point farthest = null;
        double maxDist = 0.0;

        for (Point p : points) {
            double dist = cross(a, b, p);   // positive left of a to b
            if (dist > 0) {
                leftOfAB.add(p);
                if (dist > maxDist) {
                    maxDist = dist;
                    farthest = p;
                }
            }
        }

        // Base case: no points left of a to b so a is a hull vertex
        if (farthest == null) {
            if (!hull.contains(a)) hull.add(a);
            return;
        }

        // Recursive case:
        //   farthest is a hull vertex.
        //   Points inside triangle(a, farthest, b) cannot be hull vertices discard them.
        //   Only points left of a to farthest or left of farthest to b can still be hull vertices.
        findHull(leftOfAB, a, farthest, hull);
        findHull(leftOfAB, farthest, b, hull);
    }


    public static double cross(Point a, Point b, Point c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    }


    public static Point findMinX(List<Point> points) {
        return points.stream().min(Comparator.comparingDouble(p -> p.x)).orElseThrow();
    }


    public static Point findMaxX(List<Point> points) {
        return points.stream().max(Comparator.comparingDouble(p -> p.x)).orElseThrow();
    }


    public static void sortCounterClockwise(List<Point> hull) {
        if (hull.size() < 3) return;

        double cx = hull.stream().mapToDouble(p -> p.x).average().orElse(0);
        double cy = hull.stream().mapToDouble(p -> p.y).average().orElse(0);

        hull.sort(Comparator.comparingDouble(p -> Math.atan2(p.y - cy, p.x - cx)));
    }
}