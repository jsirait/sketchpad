import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet; 

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
// Inside DrawingCanvas.java

// Helper class for sorting vertices by angle
private static class PointAngle {
    public PointObject pt;
    public double angle;
    public PointAngle(PointObject pt, double angle) {
        this.pt = pt;
        this.angle = angle;
    }
}

public void finalizeEqualLengthConstraint() {
    // First, check if we have exactly three lines that form a triangle.
    Set<PointObject> triangleVertices = new HashSet<>();
    for (LineObject line : selectedEqualLengthLines) {
        triangleVertices.add(line.getStartPoint());
        triangleVertices.add(line.getEndPoint());
    }
    if (triangleVertices.size() != 3) {
        System.out.println("Please select three lines that form a closed triangle.");
        return;
    }
    
    // Extract the three vertices into a list.
    List<PointObject> triPoints = new ArrayList<>(triangleVertices);
    
    // Iterative projection to an equilateral triangle.
    final int maxIterations = 50;
    final double tolerance = 0.001;
    for (int iter = 0; iter < maxIterations; iter++) {
        // Compute the centroid of the triangle.
        double sumX = 0, sumY = 0;
        for (PointObject pt : triPoints) {
            sumX += pt.getX();
            sumY += pt.getY();
        }
        double centerX = sumX / 3.0;
        double centerY = sumY / 3.0;
        
        // Create a list of points paired with their angle relative to the centroid.
        List<PointAngle> paList = new ArrayList<>();
        for (PointObject pt : triPoints) {
            double angle = Math.atan2(pt.getY() - centerY, pt.getX() - centerX);
            paList.add(new PointAngle(pt, angle));
        }
        // Sort by angle so the vertices are ordered clockwise (or counterclockwise)
        Collections.sort(paList, (p1, p2) -> Double.compare(p1.angle, p2.angle));
        // Reconstruct the sorted list of points.
        for (int i = 0; i < 3; i++) {
            triPoints.set(i, paList.get(i).pt);
        }
        
        // Compute current side lengths.
        PointObject A = triPoints.get(0);
        PointObject B = triPoints.get(1);
        PointObject C = triPoints.get(2);
        double L_AB = Math.hypot(B.getX() - A.getX(), B.getY() - A.getY());
        double L_BC = Math.hypot(C.getX() - B.getX(), C.getY() - B.getY());
        double L_CA = Math.hypot(A.getX() - C.getX(), A.getY() - C.getY());
        double avgSide = (L_AB + L_BC + L_CA) / 3.0;
        // In an equilateral triangle, the distance from the centroid to a vertex is L/√3.
        double R_ideal = avgSide / Math.sqrt(3);
        
        // Use the angle of the first vertex as a base.
        double baseAngle = Math.atan2(triPoints.get(0).getY() - centerY, triPoints.get(0).getX() - centerX);
        double[] targetAngles = new double[] {
            baseAngle,
            baseAngle + 2 * Math.PI / 3,
            baseAngle + 4 * Math.PI / 3
        };
        
        // Update each vertex toward its target location.
        double maxMovement = 0;
        for (int i = 0; i < 3; i++) {
            double targetX = centerX + R_ideal * Math.cos(targetAngles[i]);
            double targetY = centerY + R_ideal * Math.sin(targetAngles[i]);
            PointObject pt = triPoints.get(i);
            double dx = targetX - pt.getX();
            double dy = targetY - pt.getY();
            double movement = Math.hypot(dx, dy);
            maxMovement = Math.max(maxMovement, movement);
            // Update the point. (Here, we update directly then inform the Cassowary-based manager.)
            pt.setX((int)Math.round(targetX));
            pt.setY((int)Math.round(targetY));
            solverManager.updatePoint(pt);
        }
        solverManager.solve();
        if (maxMovement < tolerance) {
            break;
        }
    }
    
    // After convergence, clear the selection and repaint the canvas.
    selectedEqualLengthLines.clear();
    repaint();
    System.out.println("Equilateral triangle constraint applied.");
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
