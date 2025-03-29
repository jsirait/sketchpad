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
    private Point startPoint; // Used for line creation

    public DrawingCanvas() {
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    public void setMode(Mode mode) {
        this.currentMode = mode;
        System.out.println("Mode changed to: " + mode);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Use Graphics2D for enhanced drawing
        Graphics2D g2d = (Graphics2D) g;
        for (GeometricObject obj : objects) {
            obj.draw(g2d);
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (currentMode == Mode.POINT) {
            objects.add(new PointObject(x, y));
        } else if (currentMode == Mode.LINE) {
            if (startPoint == null) {
                startPoint = new Point(x, y);
            } else {
                objects.add(new LineObject(startPoint.x, startPoint.y, x, y));
                startPoint = null;
            }
        } else if (currentMode == Mode.ARC) {
            // For simplicity, we create an arc with a fixed radius and angles.
            objects.add(new ArcObject(x, y, 50, 0, 180));
        }
        repaint();
    }
    
    // Other mouse events (can be expanded as needed)
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}
