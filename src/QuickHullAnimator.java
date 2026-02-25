import java.util.*;


public class QuickHullAnimator {
    public static class Task {
        public final Point a;
        public final Point b;
        public final List<Point> candidates;

        Task(Point a, Point b, List<Point> candidates) {
            this.a = a;
            this.b = b;
            this.candidates = candidates;
        }
    }


    private final List<Point> hullPoints = new ArrayList<>();
    private final List<Point[]> hullEdges = new ArrayList<>();
    private final Queue<Task> queue = new LinkedList<>();
    private final List<Point> upperCandidates;
    public final Point minX;
    public final Point maxX;
    private Point currentA = null;
    private Point currentB = null;
    private Point currentFarthest = null;
    private List<Point> candidatePoints = new ArrayList<>();
    private List<Point> discardedPoints = new ArrayList<>();
    private boolean phase2Started = false;
    private boolean done = false;
    private String phase = "Phase 1 — Lower Hull";


    public QuickHullAnimator(List<Point> inputPoints) {
        if (inputPoints.size() < 3) {
            hullPoints.addAll(inputPoints);
            minX = null;
            maxX = null;
            upperCandidates = Collections.emptyList();
            done = true;
            return;
        }

        // Step 1: find extreme points
        minX = QuickHull.findMinX(inputPoints);
        maxX = QuickHull.findMaxX(inputPoints);

        hullPoints.add(minX);
        hullPoints.add(maxX);

        // Step 2: split remaining points into lower and upper halves
        List<Point> lower = new ArrayList<>();
        List<Point> upper = new ArrayList<>();

        for (Point p : inputPoints) {
            if (p == minX || p == maxX) continue;
            double side = QuickHull.cross(minX, maxX, p);
            if (side < 0) lower.add(p);
            else if (side > 0) upper.add(p);
        }

        queue.add(new Task(maxX, minX, lower));
        upperCandidates = upper;
    }


    public boolean nextStep() {
        if (done) return true;

        if (queue.isEmpty()) {
            if (!phase2Started) {
                phase2Started = true;
                phase = "Phase 2 — Upper Hull";
                currentA = null;
                currentB = null;
                currentFarthest = null;
                candidatePoints = new ArrayList<>();
                discardedPoints = new ArrayList<>();
                queue.add(new Task(minX, maxX, upperCandidates));
                return false;
            }


            QuickHull.sortCounterClockwise(hullPoints);
            done = true;
            currentA = null;
            currentB = null;
            currentFarthest = null;
            candidatePoints = new ArrayList<>();
            discardedPoints = new ArrayList<>();
            return true;
        }

        // Process next task
        Task task = queue.poll();
        currentA = task.a;
        currentB = task.b;
        discardedPoints = new ArrayList<>();

        // Base case: no candidates edge a to b is a hull edge
        if (task.candidates.isEmpty()) {
            currentFarthest = null;
            candidatePoints = new ArrayList<>();
            finalizeEdge(task.a, task.b);
            return false;
        }

        // Find the farthest point
        Point farthest = null;
        double maxDist = 0.0;

        for (Point p : task.candidates) {
            double d = QuickHull.cross(task.a, task.b, p);
            if (d > maxDist) {
                maxDist = d;
                farthest = p;
            }
        }

        // All candidates are on the wrong side
        if (farthest == null) {
            currentFarthest = null;
            candidatePoints = new ArrayList<>(task.candidates);
            finalizeEdge(task.a, task.b);
            return false;
        }

        currentFarthest = farthest;
        if (!hullPoints.contains(farthest)) hullPoints.add(farthest);


        List<Point> leftAF = new ArrayList<>();
        List<Point> leftFB = new ArrayList<>();
        List<Point> remaining = new ArrayList<>();

        for (Point p : task.candidates) {
            if (p == farthest) continue;
            boolean leftOfAP = QuickHull.cross(task.a, farthest, p) > 0;
            boolean leftOfPB = QuickHull.cross(farthest, task.b, p) > 0;

            if (leftOfAP) leftAF.add(p);
            else if (leftOfPB) leftFB.add(p);
            else discardedPoints.add(p);
        }

        candidatePoints = new ArrayList<>(task.candidates);
        candidatePoints.remove(farthest);

        queue.add(new Task(task.a, farthest, leftAF));
        queue.add(new Task(farthest, task.b, leftFB));

        return false;
    }

    private void finalizeEdge(Point a, Point b) {
        for (Point[] e : hullEdges) {
            if ((e[0] == a && e[1] == b) || (e[0] == b && e[1] == a)) return;
        }
        hullEdges.add(new Point[]{a, b});
    }

    public List<Point> getHullPoints() {
        return hullPoints;
    }

    public List<Point[]> getHullEdges() {
        return hullEdges;
    }

    public Point getCurrentA() {
        return currentA;
    }

    public Point getCurrentB() {
        return currentB;
    }

    public Point getCurrentFarthest() {
        return currentFarthest;
    }

    public List<Point> getCandidatePoints() {
        return candidatePoints;
    }

    public List<Point> getDiscardedPoints() {
        return discardedPoints;
    }

    public String getPhase() {
        return phase;
    }

    public boolean isDone() {
        return done;
    }
}