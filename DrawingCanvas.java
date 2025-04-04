import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class DrawingCanvas extends JPanel implements MouseListener, MouseMotionListener {
    public enum Mode {
        NONE, POINT, LINE, ARC, DELETE, 
        EQUAL_LENGTH 
    } 

    // reference line for equal length 
    private LineObject referenceLineForEqLength = null; 
    
    private Mode currentMode = Mode.NONE;
    private List<GeometricObject> objects = new ArrayList<>();
    // private Point startPoint;  // Used for line creation 

    // zooming 
    private double scale = 1.0; 

    // make drawing a line be like dragging a rubber band 
    private int startX, startY, currentX, currentY;
    private boolean isDragging = false; 

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
    }
    
    public void setMode(Mode mode) {
        this.currentMode = mode; 
        isDragging = false; 
        arcStage = 0; 
        if (mode != Mode.EQUAL_LENGTH) {
            // reset 
            referenceLineForEqLength = null; 
        }
        repaint(); 
        System.out.println("Mode changed to: " + mode); 
    } 

    public void zoomIn() {
        scale *= 1.1; 
        repaint(); 
    } 

    public void zoomOut() {
        scale /= 1.1; 
        repaint(); 
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
        objects.add(new LineObject(startX, startY, currentX, currentY));
        isDragging = false; 
        repaint(); 
        System.out.println("Line is finalized from (" + startX + ", " + startY + ") to (" + currentX + ", " + currentY + ")");
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
    public void mouseClicked(MouseEvent e) {
        // int x = e.getX();
        // int y = e.getY();
        // if (currentMode == Mode.POINT) {
        //     objects.add(new PointObject(x, y));
        // } else if (currentMode == Mode.LINE) {
        //     if (startPoint == null) {
        //         startPoint = new Point(x, y);
        //     } else {
        //         objects.add(new LineObject(startPoint.x, startPoint.y, x, y));
        //         startPoint = null;
        //     }
        // } else if (currentMode == Mode.ARC) {
        //     // For simplicity, we create an arc with a fixed radius and angles.
        //     objects.add(new ArcObject(x, y, 50, 0, 180));
        // }
        // repaint();
    }
    
    // Other mouse events (can be expanded as needed)
    @Override 
    public void mousePressed(MouseEvent e) {
        int x = (int) (e.getX() / scale); 
        int y = (int) (e.getY() / scale); 
        if (currentMode == Mode.POINT) {
            objects.add(new PointObject(x, y));
            repaint(); 
        } else if (currentMode == Mode.LINE) {
            // // rubber band 
            // startX = x; 
            // startY = y; 
            // isDragging = true; 
            if (!isDragging) {
                // initialize the starting point for the rubber band line 
                startX = x; 
                startY = y; 
                currentX = startX;
                currentY = startY; 
                isDragging = true; 
                // initialize flick detection 
                lastX = currentX; 
                lastY = currentY; 
                lastTime = System.currentTimeMillis(); 
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
                    objects.remove(i); 
                    System.out.println("Deleted object"); 
                    repaint(); 
                    break;                     
                }
            }
        } else if (currentMode == Mode.EQUAL_LENGTH) {
            // reverse iteration to check on the top-most object first 
            for (int i = objects.size()-1; i>=0; i--) {
                GeometricObject obj = objects.get(i); 
                if (obj instanceof LineObject && obj.contains(x,y)) {
                    LineObject line = (LineObject) obj; 
                    if (referenceLineForEqLength == null) {
                        // set as reference 
                        referenceLineForEqLength = line; 
                        System.out.println("ref line for equal length selected"); 
                    } else {
                        double refLength = referenceLineForEqLength.getLength(); 
                        line.setLength(refLength); 
                        System.out.println("line length adjusted"); 
                    }
                    repaint(); 
                    break; 
                }
            }
        }
    } 

    @Override 
    public void mouseDragged(MouseEvent e) {
        if (currentMode == Mode.LINE && isDragging) {
            // // update 
            // currentX = e.getX();
            // currentY = e.getY();
            // repaint(); 
            updateRubberBand(e); 
        } else if (currentMode == Mode.ARC && isDragging) {
            updateArcRubberBand(e); 
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
            int x = (int) (e.getX() / scale); 
            int y = (int) (e.getY() / scale); 
            currentX = x;
            currentY = y; 
            objects.add(new LineObject(startX, startY, currentX, currentY)); 
            isDragging = false; 
            repaint(); 
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
