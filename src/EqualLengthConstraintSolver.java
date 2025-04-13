import java.util.*;

public class EqualLengthConstraintSolver {
    private List<LineObject> selectedLines; 
    private ConstraintSolverManager solverManager; 

    public EqualLengthConstraintSolver(List<LineObject> selectedLines, ConstraintSolverManager solverManager) {
        this.selectedLines = selectedLines; 
        this.solverManager = solverManager; 
    } 

    // Helper class for pairing a point with its polar angle (for sorting)
    private static class PointAngle {
        public PointObject pt;
        public double angle;
        public PointAngle(PointObject pt, double angle) {
            this.pt = pt;
            this.angle = angle;
        }
    }

    // iteratively adjust selected lines until the endpoints converge to poisitons 
    // that roughly give each line the same length 
    public void finalizeConstraint() {
        // 1. Compute the global average length from all selected lines.
        double totalGlobal = 0;
        int count = 0;
        for (LineObject line : selectedLines) {
            totalGlobal += line.getLength();
            count++;
        }
        final double globalAvgLength = totalGlobal / count;
        System.out.println("Global average length: " + globalAvgLength);

        // 2. Build the incidence map and vertex set from selected lines.
        // (We use identity–based sets/maps so that keys remain valid even if positions change.)
        Set<PointObject> vertexSet = Collections.newSetFromMap(new IdentityHashMap<>());
        Map<PointObject, Set<LineObject>> incidence = new IdentityHashMap<>();
        for (LineObject line : selectedLines) {
            PointObject A = line.getStartPoint();
            PointObject B = line.getEndPoint();
            vertexSet.add(A);
            vertexSet.add(B);
            incidence.computeIfAbsent(A, k -> new HashSet<>()).add(line);
            incidence.computeIfAbsent(B, k -> new HashSet<>()).add(line);
        }

        // 3. Find connected components based only on the selected lines.
        List<Set<PointObject>> components = findConnectedComponents(vertexSet, incidence);

        final int maxIterations = 50;
        final double tolerance = 0.5; // in pixels

        // 4. If selected lines come from more than one connected component, process them together.
        if (components.size() > 1) { 
            System.out.println("applying equal length to multiple components");
            // TODO: this doesn not work yet!! 
            for (int iter = 0; iter < maxIterations; iter++) { 
                double globalMaxMovement = 0;
                // Use a map to accumulate corrections for each point.
                Map<PointObject, double[]> corrections = new IdentityHashMap<>();
                // Process each selected line globally.
                for (LineObject line : selectedLines) {
                    PointObject A = line.getStartPoint();
                    PointObject B = line.getEndPoint();
                    double currentLen = line.getLength();
                    if (currentLen == 0) continue;
                    double dx = (B.getX() - A.getX()) / currentLen;
                    double dy = (B.getY() - A.getY()) / currentLen;
                    // Compute error and distribute correction equally.
                    double error = currentLen - globalAvgLength;
                    double corrAx = -0.5 * error * dx;
                    double corrAy = -0.5 * error * dy;
                    double corrBx = 0.5 * error * dx;
                    double corrBy = 0.5 * error * dy;
                    corrections.computeIfAbsent(A, k -> new double[]{0, 0, 0});
                    double[] aArr = corrections.get(A);
                    aArr[0] += corrAx; aArr[1] += corrAy; aArr[2] += 1;
                    corrections.computeIfAbsent(B, k -> new double[]{0, 0, 0});
                    double[] bArr = corrections.get(B);
                    bArr[0] += corrBx; bArr[1] += corrBy; bArr[2] += 1;
                }
                // Apply the average correction to each point.
                for (Map.Entry<PointObject, double[]> entry : corrections.entrySet()) {
                    PointObject pt = entry.getKey();
                    double[] arr = entry.getValue();
                    double corrX = arr[0] / arr[2];
                    double corrY = arr[1] / arr[2];
                    double movement = Math.hypot(corrX, corrY);
                    globalMaxMovement = Math.max(globalMaxMovement, movement);
                    pt.setX((int) Math.round(pt.getX() + corrX));
                    pt.setY((int) Math.round(pt.getY() + corrY));
                    solverManager.updatePoint(pt);
                }
                solverManager.solve();
                // repaint();
                if (globalMaxMovement < tolerance)
                    break;
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("applying equal length to one component");
            // 5. Otherwise, only one connected component exists.
            // Process each component separately using the original strategy.
            for (int iter = 0; iter < maxIterations; iter++) {
                double globalMaxMovement = 0;
                for (Set<PointObject> comp : components) {
                    // Gather all lines in this component.
                    Set<LineObject> compLines = new HashSet<>();
                    for (PointObject pt : comp) {
                        Set<LineObject> incSet = incidence.get(pt);
                        if (incSet != null) {
                            compLines.addAll(incSet);
                        }
                    }
                    if (compLines.isEmpty())
                        continue;
                    // Determine if the component forms a closed polygon: every vertex has degree 2.
                    boolean closed = true;
                    for (PointObject pt : comp) {
                        Set<LineObject> inc = incidence.get(pt);
                        if (inc == null || inc.size() != 2) {
                            closed = false;
                            break;
                        }
                    }
                    if (closed && comp.size() >= 3) {
                        // Process closed polygon: Reproject vertices onto a circle.
                        double sumX = 0, sumY = 0;
                        for (PointObject pt : comp) {
                            sumX += pt.getX();
                            sumY += pt.getY();
                        }
                        double centerX = sumX / comp.size();
                        double centerY = sumY / comp.size();
                        List<PointAngle> paList = new ArrayList<>();
                        for (PointObject pt : comp) {
                            double angle = Math.atan2(pt.getY() - centerY, pt.getX() - centerX);
                            paList.add(new PointAngle(pt, angle));
                        }
                        Collections.sort(paList, (p1, p2) -> Double.compare(p1.angle, p2.angle));
                        int n = comp.size();
                        // For a regular n-sided polygon, R = L / (2*sin(PI/n)).
                        double R = globalAvgLength / (2 * Math.sin(Math.PI / n));
                        double baseAngle = paList.get(0).angle;
                        for (int i = 0; i < n; i++) {
                            double targetAngle = baseAngle + 2 * Math.PI * i / n;
                            PointObject pt = paList.get(i).pt;
                            double targetX = centerX + R * Math.cos(targetAngle);
                            double targetY = centerY + R * Math.sin(targetAngle);
                            double dx = targetX - pt.getX();
                            double dy = targetY - pt.getY();
                            double movement = Math.hypot(dx, dy);
                            globalMaxMovement = Math.max(globalMaxMovement, movement);
                            pt.setX((int) Math.round(targetX));
                            pt.setY((int) Math.round(targetY));
                            solverManager.updatePoint(pt);
                        }
                    } else {
                        // Process open or non-closed component using open branch logic.
                        Map<PointObject, double[]> corrections = new IdentityHashMap<>();
                        for (LineObject line : compLines) {
                            PointObject A = line.getStartPoint();
                            PointObject B = line.getEndPoint();
                            double currentLen = line.getLength();
                            if (currentLen == 0)
                                continue;
                            int degreeA = (incidence.get(A) != null ? incidence.get(A).size() : 0);
                            int degreeB = (incidence.get(B) != null ? incidence.get(B).size() : 0);
                            double dx = (B.getX() - A.getX()) / currentLen;
                            double dy = (B.getY() - A.getY()) / currentLen;
                            if (degreeA > 1 && degreeB == 1) {
                                double targetX = A.getX() + globalAvgLength * dx;
                                double targetY = A.getY() + globalAvgLength * dy;
                                double corrBx = targetX - B.getX();
                                double corrBy = targetY - B.getY();
                                corrections.computeIfAbsent(B, k -> new double[]{0, 0, 0});
                                double[] arr = corrections.get(B);
                                arr[0] += corrBx;
                                arr[1] += corrBy;
                                arr[2] += 1;
                            } else if (degreeB > 1 && degreeA == 1) {
                                double targetX = B.getX() - globalAvgLength * dx;
                                double targetY = B.getY() - globalAvgLength * dy;
                                double corrAx = targetX - A.getX();
                                double corrAy = targetY - A.getY();
                                corrections.computeIfAbsent(A, k -> new double[]{0, 0, 0});
                                double[] arr = corrections.get(A);
                                arr[0] += corrAx;
                                arr[1] += corrAy;
                                arr[2] += 1;
                            } else {
                                double error = currentLen - globalAvgLength;
                                double corrAx = -0.5 * error * dx;
                                double corrAy = -0.5 * error * dy;
                                double corrBx = 0.5 * error * dx;
                                double corrBy = 0.5 * error * dy;
                                corrections.computeIfAbsent(A, k -> new double[]{0, 0, 0});
                                double[] aArr = corrections.get(A);
                                aArr[0] += corrAx;
                                aArr[1] += corrAy;
                                aArr[2] += 1;
                                corrections.computeIfAbsent(B, k -> new double[]{0, 0, 0});
                                double[] bArr = corrections.get(B);
                                bArr[0] += corrBx;
                                bArr[1] += corrBy;
                                bArr[2] += 1;
                            }
                        }
                        for (Map.Entry<PointObject, double[]> entry : corrections.entrySet()) {
                            PointObject pt = entry.getKey();
                            double[] arr = entry.getValue();
                            double corrX = arr[0] / arr[2];
                            double corrY = arr[1] / arr[2];
                            double movement = Math.hypot(corrX, corrY);
                            globalMaxMovement = Math.max(globalMaxMovement, movement);
                            pt.setX((int) Math.round(pt.getX() + corrX));
                            pt.setY((int) Math.round(pt.getY() + corrY));
                            solverManager.updatePoint(pt);
                        }
                    }
                }
                solverManager.solve();
                if (globalMaxMovement < tolerance)
                    break;
            }
        }
        selectedLines.clear();
        System.out.println("Equal-length constraint applied using global average length.");
    }

    // Helper: Given the set of vertices and an incidence map (each point with its lines),
    // find connected components (each component is a set of points).
    private List<Set<PointObject>> findConnectedComponents(Set<PointObject> vertices,
                                                        Map<PointObject, Set<LineObject>> incidence) {
        List<Set<PointObject>> components = new ArrayList<>();
        Set<PointObject> visited = new HashSet<>();
        for (PointObject pt : vertices) {
            if (!visited.contains(pt)) {
                Set<PointObject> comp = new HashSet<>();
                Queue<PointObject> queue = new LinkedList<>();
                queue.add(pt);
                visited.add(pt);
                while (!queue.isEmpty()) {
                    PointObject cur = queue.poll();
                    comp.add(cur);
                    for (LineObject line : incidence.get(cur)) {
                        PointObject other = line.getStartPoint().equals(cur) ? line.getEndPoint() : line.getStartPoint();
                        if (!visited.contains(other)) {
                            visited.add(other);
                            queue.add(other);
                        }
                    }
                }
                components.add(comp);
            }
        }
        return components;
    }

}
