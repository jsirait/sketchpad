import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Queue;
import java.util.HashSet;
import java.util.LinkedList; 

public class DrawingCanvas extends JPanel implements MouseListener, MouseMotionListener {
    public enum Mode {
        NONE, POINT, LINE, ARC, DELETE, 
        EQUAL_LENGTH, 
        MOVE 
    } 

    private ConstraintSolverManager solverManager = new ConstraintSolverManager(); 

    // reference line for equal length 
    // private LineObject referenceLineForEqLength = null; 
    private List<LineObject> selectedEqualLengthLines = new ArrayList<>();
    
    
    private Mode currentMode = Mode.NONE;
    private List<GeometricObject> objects = new ArrayList<>();

    // zooming 
    private double scale = 1.0; 

    // make drawing a line be like dragging a rubber band 
    private int startX, startY; 
    private int currentX, currentY;
    private PointObject currentStartPoint = null;  // for a line 
    private boolean isDragging = false; 

    // field to hold the selected point for dragging 
    private PointObject selectedPoint = null; 

    // flick detection 
    private int lastX, lastY; 
    private long lastTime;  // in milliseconds 
    // threshold speed in pixels per milliseconds 
    private static final double FLICK_THRESHOLD = 2.0; 

    // for arc draw 
    // arc stage defined 
    // 0 = not started 
    // 1 = center defined 
    // 2 = radius defined and now defining angle 
    private int arcCenterX, arcCenterY; 
    private int arcRadius; 
    private double arcStartAngle, arcSweepAngle; 
    private int arcStage = 0;

    public DrawingCanvas() {
        addMouseListener(this);
        addMouseMotionListener(this);
        // addMouseWheelListener(this);
    }

    public Mode getMode() {
        return this.currentMode; 
    }
    
    public void setMode(Mode mode) {
        this.currentMode = mode; 
        isDragging = false; 
        arcStage = 0; 
        if (mode != Mode.EQUAL_LENGTH) {
            // reset 
            // referenceLineForEqLength = null;  
            selectedEqualLengthLines.clear(); 
        }
        repaint(); 
        System.out.println("Mode changed to: " + mode); 
    } 

    public void zoomIn()  { scale *= 1.1;  repaint(); } 
    public void zoomOut()  { scale /= 1.1; repaint(); }

    /** 
    * POINT
    */ 
    private void cleanupLeftoverPoints() {
        // create a set of points that are used by any line 
        Set <PointObject> usedPoints = new HashSet<>(); 
        for (GeometricObject obj : objects) {
            if (obj instanceof LineObject) {
                LineObject line = (LineObject) obj; 
                usedPoints.add(line.getStartPoint()); 
                usedPoints.add(line.getEndPoint()); 
            }
        } 
        // remove points that are not used by any line 
        objects.removeIf(obj -> (obj instanceof PointObject) && !usedPoints.contains((obj))); 
    }

    /** 
     * LINE 
     */
    private void updateRubberBand(MouseEvent e) {
        if (currentMode == Mode.LINE && isDragging) {
            int x = (int) (e.getX() / scale); 
            int y = (int) (e.getY() / scale); 
            currentX = x;
            currentY = y; 
            long currentTime =  System.currentTimeMillis(); 
            long dt = currentTime - lastTime; 
            if (dt > 0) {
                double dx = currentX - lastX; 
                double dy = currentY - lastY; 
                double distance = Math.sqrt(dx*dx + dy*dy); 
                double speed = distance / dt;  // speed in pixels per millisec 
                if (speed > FLICK_THRESHOLD) {
                    finalizeLine(); 
                    return; 
                }
            }
            lastX = currentX; 
            lastY = currentY; 
            lastTime = currentTime; 
            repaint(); 
        }
    } 


    private void finalizeLine() {
        // use the already-selected starting point (currentStartPoint) 
        // and try to find an existing point for the end 
        PointObject endPt = findNearbyPoint(currentX, currentY); 
        if (endPt == null) {
            endPt = new PointObject(currentX, currentY); 
            objects.add(endPt); 
        }
        // create the line using the shared currentStartPoint and the endPt 
        objects.add(new LineObject(currentStartPoint, endPt)); 
        isDragging = false; 
        repaint(); 
        System.out.println("Line is finalized from (" + startX + ", " + startY + ") to (" + currentX + ", " + currentY + ")");
    } 

    private PointObject findNearbyPoint(int x, int y) {
        // look through all objects for a point close to (x,y) 
        for (GeometricObject obj : objects) {
            if (obj instanceof PointObject) {
                PointObject pt = (PointObject) obj; 
                if (pt.contains(x,y)) {
                    return pt; 
                }
            }
        } 
        return null; 
    } 

    // --- Iterative enforcement of equal lengths on selected lines ---
    // This method adjusts the endpoints for all selected lines so that their lengths converge to a common average.
    public void enforceEqualLengthConstraintOnSelectedLines() {
        // First, compute the average length of the selected lines.
        int n = selectedEqualLengthLines.size();
        if (n == 0) return;
        double total = 0;
        for (LineObject L : selectedEqualLengthLines) {
            total += L.getLength();
        }
        double avgLength = total / n;
        
        // Map each unique vertex to a list of desired target positions.
        Map<PointObject, List<PointObject>> corrections = new HashMap<>();
        
        for (LineObject L : selectedEqualLengthLines) {
            PointObject A = L.getStartPoint();
            PointObject B = L.getEndPoint();
            double currentLength = L.getLength();
            if (currentLength == 0) continue;
            double error = currentLength - avgLength;
            // Compute unit vector from A to B.
            double dx = B.getX() - A.getX();
            double dy = B.getY() - A.getY();
            double ux = dx / currentLength;
            double uy = dy / currentLength;
            // We decide to adjust both endpoints by half the correction.
            // For A: new target = A - (error/2)*u, for B: new target = B + (error/2)*u.
            double damping = 0.5;  // or try 0.3, etc.
            double corr = damping * (error / 2.0);
            PointObject targetA = new PointObject((int) (A.getX() - corr * ux), (int) (A.getY() - corr * uy));
            PointObject targetB = new PointObject((int) (B.getX() + corr * ux), (int) (B.getY() + corr * uy));
            
            corrections.computeIfAbsent(A, k -> new ArrayList<>()).add(targetA);
            corrections.computeIfAbsent(B, k -> new ArrayList<>()).add(targetB);
        }
        // For each vertex, average all target positions and update it.
        for (Map.Entry<PointObject, List<PointObject>> entry : corrections.entrySet()) {
            PointObject pt = entry.getKey();
            List<PointObject> targets = entry.getValue();
            double sumX = 0, sumY = 0;
            for (PointObject target : targets) {
                sumX += target.getX();
                sumY += target.getY();
            }
            double newX = sumX / targets.size();
            double newY = sumY / targets.size();
            pt.setX((int)Math.round(newX));
            pt.setY((int)Math.round(newY));
            solverManager.updatePoint(pt);
        }
        solverManager.solve();
        repaint();
    }
    
    // Finalize equal-length constraint iteratively until convergence.
    // Helper class for pairing a point with its polar angle (for sorting)
    private static class PointAngle {
        public PointObject pt;
        public double angle;
        public PointAngle(PointObject pt, double angle) {
            this.pt = pt;
            this.angle = angle;
        }
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
                    // For each line incident to cur, add the other endpoint.
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

    public void finalizeEqualLengthConstraint() {
        // Build an incidence map: for each point, which selected lines touch it.
        Set<PointObject> vertexSet = new HashSet<>();
        Map<PointObject, Set<LineObject>> incidence = new HashMap<>();
        for (LineObject line : selectedEqualLengthLines) {
            PointObject A = line.getStartPoint();
            PointObject B = line.getEndPoint();
            vertexSet.add(A);
            vertexSet.add(B);
            incidence.computeIfAbsent(A, k -> new HashSet<>()).add(line);
            incidence.computeIfAbsent(B, k -> new HashSet<>()).add(line);
        }
        
        // Compute connected components from the vertex set.
        List<Set<PointObject>> components = findConnectedComponents(vertexSet, incidence);
        
        // Iterate over each connected component.
        for (Set<PointObject> comp : components) {
            // Collect all lines for this component.
            Set<LineObject> compLines = new HashSet<>();
            for (PointObject pt : comp) {
                if (incidence.containsKey(pt)) {
                    compLines.addAll(incidence.get(pt));
                }
            }
            // Compute the average side length for the component.
            double total = 0;
            for (LineObject line : compLines) {
                total += line.getLength();
            }
            double avgLength = total / compLines.size();
            
            // Check if the component forms a closed polygon:
            // every vertex should have exactly degree 2 and at least 3 points must be involved.
            boolean closed = true;
            for (PointObject pt : comp) {
                if (incidence.get(pt).size() != 2) {
                    closed = false;
                    break;
                }
            }
            if (closed && comp.size() >= 3) {
                // --- Closed Polygon: Project vertices onto a circle to form a regular polygon ---
                
                // Compute the centroid.
                double sumX = 0, sumY = 0;
                for (PointObject pt : comp) {
                    sumX += pt.getX();
                    sumY += pt.getY();
                }
                double centerX = sumX / comp.size();
                double centerY = sumY / comp.size();
                
                // Order the vertices in angular order around the centroid.
                List<PointAngle> paList = new ArrayList<>();
                for (PointObject pt : comp) {
                    double angle = Math.atan2(pt.getY() - centerY, pt.getX() - centerX);
                    paList.add(new PointAngle(pt, angle));
                }
                Collections.sort(paList, (p1, p2) -> Double.compare(p1.angle, p2.angle));
                
                // For an n-sided regular polygon, the side length L relates to the circumradius R via:
                // L = 2 * R * sin(PI/n)   so R = L / (2*sin(PI/n))
                int n = comp.size();
                double R = avgLength / (2 * Math.sin(Math.PI / n));
                
                // Use the angle of the first vertex as a base.
                double baseAngle = paList.get(0).angle;
                for (int i = 0; i < n; i++) {
                    double targetAngle = baseAngle + 2 * Math.PI * i / n;
                    PointObject pt = paList.get(i).pt;
                    double targetX = centerX + R * Math.cos(targetAngle);
                    double targetY = centerY + R * Math.sin(targetAngle);
                    pt.setX((int) Math.round(targetX));
                    pt.setY((int) Math.round(targetY));
                    solverManager.updatePoint(pt);
                }
            } else {
                // --- Open or non-closed components ---
                // For each selected line, compute the correction needed so that the line’s length moves
                // toward the average. Distribute the correction (half to one endpoint, half to the other).
                
                // Use a map to accumulate corrections for each point:
                // The value is an array of [sumXCorrection, sumYCorrection, count].
                Map<PointObject, double[]> corrections = new HashMap<>();
                for (LineObject line : compLines) {
                    PointObject A = line.getStartPoint();
                    PointObject B = line.getEndPoint();
                    double currentLen = line.getLength();
                    if (currentLen == 0) continue;
                    double error = currentLen - avgLength;
                    double dx = (B.getX() - A.getX()) / currentLen;
                    double dy = (B.getY() - A.getY()) / currentLen;
                    // Correction: move A opposite the direction and B in the direction by half of the error.
                    double corrAx = -0.5 * error * dx;
                    double corrAy = -0.5 * error * dy;
                    double corrBx = 0.5 * error * dx;
                    double corrBy = 0.5 * error * dy;
                    
                    corrections.computeIfAbsent(A, k -> new double[]{0, 0, 0});
                    double[] aCorr = corrections.get(A);
                    aCorr[0] += corrAx;
                    aCorr[1] += corrAy;
                    aCorr[2] += 1;
                    
                    corrections.computeIfAbsent(B, k -> new double[]{0, 0, 0});
                    double[] bCorr = corrections.get(B);
                    bCorr[0] += corrBx;
                    bCorr[1] += corrBy;
                    bCorr[2] += 1;
                }
                // Apply average correction for each point.
                for (Map.Entry<PointObject, double[]> entry : corrections.entrySet()) {
                    PointObject pt = entry.getKey();
                    double[] arr = entry.getValue();
                    double avgCorrX = arr[0] / arr[2];
                    double avgCorrY = arr[1] / arr[2];
                    pt.setX((int) Math.round(pt.getX() + avgCorrX));
                    pt.setY((int) Math.round(pt.getY() + avgCorrY));
                    solverManager.updatePoint(pt);
                }
            }
        }
        // Tell the solver to update all point positions.
        solverManager.solve();
        selectedEqualLengthLines.clear();
        repaint();
        System.out.println("Equal length constraint applied.");
    }



    /** 
     * ARC 
     */
    private void updateArcRubberBand(MouseEvent e) {
        if (currentMode == Mode.ARC && arcStage == 2 && isDragging) { 
            int x = (int) (e.getX() / scale); 
            int y = (int) (e.getY() / scale); 
            currentX = x; 
            currentY = y; 
            // compute current angle from center to current mouse position 
            double currentAngle = Math.toDegrees(Math.atan2(arcCenterY-currentY, currentX-arcCenterX)); 
            arcSweepAngle = currentAngle - arcStartAngle; 
            // flick 
            long currentTime = System.currentTimeMillis(); 
            long dt = currentTime - lastTime; 
            if (dt > 0) {
                double dx = currentX - lastX; 
                double dy = currentY - lastY; 
                double distance = Math.sqrt(dx*dx + dy*dy); 
                double speed = distance / dt; 
                if (speed > FLICK_THRESHOLD) {
                    finalizeArc(); 
                    return; 
                }
            }
            lastX = currentX;
            lastY = currentY; 
            lastTime = currentTime; 
            repaint(); 
        }
    }
    
    private void finalizeArc() {
        // computing bounding box 
        int x = arcCenterX - arcRadius; 
        int y = arcCenterY - arcRadius; 
        int diameter = arcRadius*2; 
        objects.add(new ArcObject(x, y, diameter, diameter, (int)arcStartAngle, (int)arcSweepAngle)); 
        isDragging = false; 
        arcStage = 0; 
        repaint(); 
        System.out.println("arc drawn"); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // use Graphics2D for enhanced drawing 
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(scale, scale); 
        for (GeometricObject obj : objects) {
            obj.draw(g2d);
        }

        // if in LINE mode and is drawing, draw the temporary rubber band line 
        if (currentMode == Mode.LINE && isDragging) {
            g.setColor(Color.GRAY); 
            g.drawLine(startX, startY, currentX, currentY); 
        } 

        // draw temporary arc 
        if (currentMode == Mode.ARC && isDragging) {
            g.setColor(Color.GRAY); 
            if (arcStage == 1) {
                // show center 
                g.fillOval(arcCenterX-3, arcCenterY-3, 6, 6); 
            } else if (arcStage == 2) {
                int x = arcCenterX - arcRadius; 
                int y = arcCenterY - arcRadius; 
                int diameter = arcRadius*2; 
                g.drawArc(x, y, diameter, diameter, (int)arcStartAngle, (int)arcSweepAngle); 
            }
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {}
    
    // Other mouse events (can be expanded as needed)
    @Override 
    public void mousePressed(MouseEvent e) {
        int x = (int) (e.getX() / scale); 
        int y = (int) (e.getY() / scale); 
        if (currentMode == Mode.POINT) {
            objects.add(new PointObject(x, y));
            repaint(); 
        } else if (currentMode == Mode.LINE) {
            if (!isDragging) {
                // first click try to snap to an existing point 
                // initialize the starting point for the rubber band line 
                PointObject pt = findNearbyPoint(x, y); 
                if (pt == null) {
                    pt = new PointObject(x, y);
                    objects.add(pt); 
                }
                startX = pt.getX(); 
                startY = pt.getY(); 
                currentX = startX;
                currentY = startY; 
                currentStartPoint = pt; 
                isDragging = true; 
                // initialize flick detection 
                lastX = currentX; 
                lastY = currentY; 
                lastTime = System.currentTimeMillis(); 
            } else {
                // second click try to snap to an existing point 
                PointObject pt = findNearbyPoint(x, y); 
                if (pt == null) {
                    pt = new PointObject(x, y); 
                    objects.add(pt); 
                } 
                // create a line that connects the currentStartPoint and the new pt 
                LineObject line = new LineObject(currentStartPoint, pt); 
                objects.add(line); 
                isDragging = false; 
                currentStartPoint = null; 
                repaint(); 
            }
        } else if (currentMode == Mode.ARC) {
            if (arcStage == 0) {
                // first click is for center 
                arcCenterX = x; 
                arcCenterY = y; 
                arcStage = 1; 
                isDragging = true; 
                System.out.println("arc center set"); 
            } else if (arcStage == 1) {
                // second click to define radius and compute starting angle 
                arcRadius = (int) Math.round(Math.sqrt(Math.pow(x - arcCenterX, 2) + Math.pow(y - arcCenterY, 2)));
                arcStartAngle = Math.toDegrees(Math.atan2(arcCenterY - y, x - arcCenterX));
                arcStage = 2;
                // Initialize flick detection variables for arc dragging.
                lastX = x;
                lastY = y;
                lastTime = System.currentTimeMillis(); 
                System.out.println("arc radius set");
            }
        }else if (currentMode == Mode.DELETE) {
            // iterate in reverse order so that objects on top are checked first 
            for (int i = objects.size() - 1; i>=0; i--) {
                GeometricObject obj = objects.get(i); 
                if (obj.contains(x,y)) { 
                    // logic for line 
                    if (obj instanceof LineObject) {
                        objects.remove(i); 
                        System.out.println("deleted line"); 
                        cleanupLeftoverPoints(); 
                        repaint(); 
                        break; 
                    } else { 
                        // perhaps other logic for other objects 
                        objects.remove(i); 
                        System.out.println("Deleted object"); 
                        repaint(); 
                        break; 
                    }
                }
            }
        // } else if (currentMode == Mode.EQUAL_LENGTH) {
        //     // reverse iteration to check on the top-most object first 
        //     for (int i = objects.size()-1; i>=0; i--) {
        //         GeometricObject obj = objects.get(i); 
        //         if (obj instanceof LineObject && obj.contains(x,y)) {
        //             LineObject line = (LineObject) obj; 
        //             if (referenceLineForEqLength == null) {
        //                 // set as reference 
        //                 referenceLineForEqLength = line; 
        //                 System.out.println("ref line for equal length selected"); 
        //             } else {
        //                 double refLength = referenceLineForEqLength.getLength(); 
        //                 line.setLength(refLength); 
        //                 System.out.println("line length adjusted"); 
        //             }
        //             repaint(); 
        //             break; 
        //         }
        //     }
        } else if (currentMode == Mode.EQUAL_LENGTH) {
            // In EQUAL_LENGTH mode, user clicks on a line to add it to the selection.
            boolean found = false;
            for (int i = objects.size()-1; i>=0; i--) {
                GeometricObject obj = objects.get(i);
                if (obj instanceof LineObject && obj.contains(x, y)) {
                    LineObject line = (LineObject)obj;
                    if (!selectedEqualLengthLines.contains(line)) {
                        selectedEqualLengthLines.add(line); 
                        System.out.println("Selected line: " + line);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("No line found at ("+x+","+y+")");
            }
        } else if (currentMode == Mode.MOVE) {
            PointObject pt = findNearbyPoint(x, y); 
            if (pt != null) {
                selectedPoint = pt; 
            }
        }
    } 

    @Override 
    public void mouseDragged(MouseEvent e) {
        if (currentMode == Mode.LINE && isDragging) {
            updateRubberBand(e); 
        } else if (currentMode == Mode.ARC && isDragging) {
            updateArcRubberBand(e); 
        } else if (currentMode == Mode.MOVE && selectedPoint != null) {
            // update point position 
            int newX = (int) (e.getX() / scale); 
            int newY = (int) (e.getY() / scale); 
            selectedPoint.setX(newX); 
            selectedPoint.setY(newY); 
            repaint(); 
        }
    }


    @Override 
    public void mouseMoved(MouseEvent e) {
        if (currentMode == Mode.LINE && isDragging) {
            updateRubberBand(e); 
        } else if (currentMode == Mode.ARC && isDragging) {
            updateArcRubberBand(e); 
        }
    }


    @Override 
    public void mouseReleased(MouseEvent e) {
        if (currentMode == Mode.LINE && isDragging) {
            finalizeLine(); 
        } else if (currentMode == Mode.MOVE){
            selectedPoint = null; 
        }
    } 

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e. getWheelRotation() < 0) {
            zoomIn();
        } else {
            zoomOut(); 
        }
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
