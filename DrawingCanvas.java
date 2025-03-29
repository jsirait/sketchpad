import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class DrawingCanvas extends JPanel implements MouseListener, MouseMotionListener {
    public enum Mode {
        NONE, POINT, LINE, ARC
    }
    
    private Mode currentMode = Mode.NONE;
    private List<GeometricObject> objects = new ArrayList<>();
    // private Point startPoint;  // Used for line creation

    // make drawing a line be like dragging a rubber band 
    private int startX, startY, currentX, currentY;
    private boolean isDragging = false; 

    public DrawingCanvas() {
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    public void setMode(Mode mode) {
        this.currentMode = mode; 
        System.out.println("Mode changed to: " + mode); 
        isDragging = false; 
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // use Graphics2D for enhanced drawing 
        Graphics2D g2d = (Graphics2D) g;
        for (GeometricObject obj : objects) {
            obj.draw(g2d);
        }

        // if in LINE mode and is drawing, draw the temporary rubber band line 
        if (currentMode == Mode.LINE && isDragging) {
            g.setColor(Color.GRAY); 
            g.drawLine(startX, startY, currentX, currentY); 
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
        int x = e.getX(), y = e.getY(); 
        if (currentMode == Mode.POINT) {
            objects.add(new PointObject(x, y));
            repaint(); 
        } else if (currentMode == Mode.LINE) {
            // rubber band 
            startX = x; 
            startY = y; 
            isDragging = true; 
        }
    } 

    @Override 
    public void mouseDragged(MouseEvent e) {
        if (currentMode == Mode.LINE && isDragging) {
            // update 
            currentX = e.getX();
            currentY = e.getY();
            repaint(); 
        }
    }


    @Override 
    public void mouseReleased(MouseEvent e) {
        if (currentMode == Mode.LINE && isDragging) {
            currentX = e.getX();
            currentY = e.getY(); 
            objects.add(new LineObject(startX, startY, currentX, currentY)); 
            isDragging = false; 
            repaint(); 
        }
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}
