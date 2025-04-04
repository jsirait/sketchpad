import java.awt.Color;
import java.awt.Graphics2D;

public class LineObject extends GeometricObject{
    // store ref to two point objects instead of raw 
    // private int x1, x2, y1, y2; 
    private PointObject startPoint; 
    private PointObject endPoint; 

    public LineObject(PointObject startPoint, PointObject endPoint) {
        this.startPoint = startPoint; 
        this.endPoint = endPoint; 
    } 

    public PointObject getStartPoint() { return this.startPoint; } 
    public PointObject getEndPoint() { return this.endPoint; } 

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLUE);  // probably should be uniform 
        g2d.drawLine(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY()); 
    }

    @Override
    public boolean contains(int px, int py) {
        final double tolerance = 10.0; // increased tolerance
        int x1 = startPoint.getX(), y1 = startPoint.getY(); 
        int x2 = endPoint.getX(), y2 = endPoint.getY(); 
        double dx = x2 - x1, dy = y2 - y1;
        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0) return false;
        double t = ((px - x1) * dx + (py - y1) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * dx;
        double projY = y1 + t * dy;
        double distance = Math.sqrt(Math.pow(px - projX, 2) + Math.pow(py - projY, 2));
        return distance <= tolerance;
    }
    
    public double getLength() {
        double dx = endPoint.getX() - startPoint.getX(); 
        double dy = endPoint.getY() - startPoint.getY(); 
        return Math.sqrt(dx*dx + dy*dy); 
    } 

    // keeps the starting point (x1, y1) fixed and scales the direction vector.
    public void setLength(double newLength) {
        int x1 = startPoint.getX();
        int y1 = startPoint.getY();
        int currentX2 = endPoint.getX();
        int currentY2 = endPoint.getY();
        double dx = currentX2 - x1;
        double dy = currentY2 - y1;
        double currentLength = Math.sqrt(dx * dx + dy * dy);
        if (currentLength == 0) return;
        double scaleFactor = newLength / currentLength;
        int newX2 = x1 + (int) Math.round(dx * scaleFactor);
        int newY2 = y1 + (int) Math.round(dy * scaleFactor);
        // Detach the endpoint: create a new PointObject for this line.
        this.endPoint = new PointObject(newX2, newY2);
    }
    
    @Override
    public String toString() {
        return "Line[(" + startPoint + ") -> (" + endPoint + ")]";
    }

}
