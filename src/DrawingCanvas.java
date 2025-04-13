import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class DrawingCanvas extends JPanel implements MouseListener, MouseMotionListener {
    public enum Mode {
        NONE, POINT, LINE, ARC, DELETE, 
        EQUAL_LENGTH, 
        MOVE, GROUP_SELECT
    } 

    private ConstraintSolverManager solverManager = new ConstraintSolverManager(); 

    // lines for equal length 
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
    private static final double FLICK_THRESHOLD = 3.0; 

    // for arc draw 
    // arc stage defined 
    // 0 = not started 
    // 1 = center defined 
    // 2 = radius defined and now defining angle 
    private int arcCenterX, arcCenterY; 
    private int arcRadius; 
    private double arcStartAngle, arcSweepAngle; 
    private int arcStage = 0;
    private double accumulatedSweepAngle = 0; 
    private Double previousAngle = null; // enable null checking as object 

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
                if (pt.contains(x,y))  { return pt; }
            }
        } 
        return null; 
    } 

    /**
     * EQUAL LENGTH LINES
     */
    public void finalizeEqualLengthConstraint() {
        EqualLengthConstraintSolver solver = new EqualLengthConstraintSolver(selectedEqualLengthLines, solverManager); 
        solver.finalizeConstraint(); 
        repaint(); 
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
            // if this is the first movement event to draw an arc, initialize previousAngle 
            if (previousAngle == null) {
                previousAngle = currentAngle; 
            } 
            // compute the incremental change in angle 
            double delta = currentAngle - previousAngle; 
            // normalization 
            if (delta < -180) {
                delta += 360;
            } else if (delta > 180) {
                delta -= 360; 
            } 
            accumulatedSweepAngle += delta; 
            previousAngle = currentAngle; 

            // arcSweepAngle = currentAngle - arcStartAngle; 
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

            arcSweepAngle = accumulatedSweepAngle; 
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
        previousAngle = null; 
        accumulatedSweepAngle = 0; 
        repaint(); 
        System.out.println("arc drawn"); 
    }

    /**
     * Merges points that are extremely close together (within a threshold).
     * After merging, all lines that referenced the removed point are updated.
     * Also updates the constraint solver by removing the merged point.
     */
    private void mergeClosePoints() {
        final double mergeThreshold = 5.0; // in pixels
        
        // Gather all PointObjects.
        List<PointObject> points = new ArrayList<>();
        for (GeometricObject obj : objects) {
            if (obj instanceof PointObject) {
                points.add((PointObject) obj);
            }
        }
        
        for (int i = 0; i < points.size(); i++) {
            PointObject p1 = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                PointObject p2 = points.get(j);
                double dx = p1.getX() - p2.getX();
                double dy = p1.getY() - p2.getY();
                if (Math.hypot(dx, dy) < mergeThreshold) {
                    // Merge p2 into p1 by averaging positions.
                    int avgX = (p1.getX() + p2.getX()) / 2;
                    int avgY = (p1.getY() + p2.getY()) / 2;
                    p1.setX(avgX);
                    p1.setY(avgY);
                    
                    // Update all LineObjects that reference p2.
                    for (GeometricObject obj : objects) {
                        if (obj instanceof LineObject) {
                            LineObject line = (LineObject) obj;
                            if (line.getStartPoint().equals(p2)) {
                                line.setStartPoint(p1);
                            }
                            if (line.getEndPoint().equals(p2)) {
                                line.setEndPoint(p1);
                            }
                        }
                    }
                    
                    // Remove p2 from the constraint solver.
                    solverManager.removePoint(p2);
                    // Remove p2 from the objects list.
                    objects.remove(p2);
                    // Also remove from local points list.
                    points.remove(j);
                    j--; // Adjust index after removal.
                }
            }
        }
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
        } else if (currentMode == Mode.ARC && isDragging) {
            updateArcRubberBand(e);
        } else if (currentMode == Mode.MOVE){
            mergeClosePoints(); 
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
